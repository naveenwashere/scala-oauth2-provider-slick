package controllers

import javax.inject.{Inject, Singleton}

import dao.{AccountDao, OAuthAccessTokenDao, OAuthAuthorizationCodeDao, OAuthClientDao}
import models._
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.Future
import scalaoauth2.provider.OAuth2ProviderActionBuilders._
import scalaoauth2.provider._

@Singleton
class OAuthController @Inject()(accountDao: AccountDao,
                                oauthAccessTokenDao: OAuthAccessTokenDao,
                                oauthAuthorizationCodeDao: OAuthAuthorizationCodeDao,
                                oauthClientDao: OAuthClientDao) extends Controller with OAuth2Provider {

  implicit val authInfoWrites = new Writes[AuthInfo[Account]] {
    def writes(authInfo: AuthInfo[Account]): JsObject = {
      Json.obj(
        "account" -> Json.obj(
          "email" -> authInfo.user.email
        ),
        "clientId" -> authInfo.clientId,
        "redirectUri" -> authInfo.redirectUri
      )
    }
  }

  override val tokenEndpoint = new TokenEndpoint {
    override val handlers = Map(
      OAuthGrantType.AUTHORIZATION_CODE -> new AuthorizationCode(),
      OAuthGrantType.REFRESH_TOKEN -> new RefreshToken(),
      OAuthGrantType.CLIENT_CREDENTIALS -> new ClientCredentials(),
      OAuthGrantType.PASSWORD -> new Password()
    )
  }

  def accessToken: Action[AnyContent] = Action.async { implicit request =>
    issueAccessToken(new MyDataHandler())
  }

  def resources: Action[AnyContent] = AuthorizedAction(new MyDataHandler()) { request =>
    Ok(Json.toJson(request.authInfo))
  }

  class MyDataHandler extends DataHandler[Account] {

    // common

    override def validateClient(request: AuthorizationRequest): Future[Boolean] = {
      val clientCredential = request.clientCredential.get
      oauthClientDao.validate(clientCredential.clientId, clientCredential.clientSecret.getOrElse(""), request.grantType).map(success => success)
    }

    override def getStoredAccessToken(authInfo: AuthInfo[Account]): Future[Option[AccessToken]] =
      oauthAccessTokenDao.findByAuthorized(authInfo.user, authInfo.clientId.getOrElse(""))
          .map { optionVal =>
            optionVal.map { accessToken =>
              toAccessToken(accessToken)
            }
          }

    override def createAccessToken(authInfo: AuthInfo[Account]): Future[AccessToken] = {
      val clientId = authInfo.clientId.getOrElse(throw new InvalidClient())
      for {
        oauthClient <- oauthClientDao.findByClientId(clientId)
        oauthAccessToken <- oauthAccessTokenDao.create(authInfo.user, oauthClient.get)
      } yield toAccessToken(oauthAccessToken)
    }

    private val accessTokenExpireSeconds = 3600
    private def toAccessToken(accessToken: OauthAccessToken) = {
      AccessToken(
        accessToken.accessToken,
        Some(accessToken.refreshToken),
        None,
        Some(accessTokenExpireSeconds),
        accessToken.createdAt.toDate
      )
    }

    override def findUser(request: AuthorizationRequest): Future[Option[Account]] = {
      request match {
        case request: PasswordRequest =>
          accountDao.authenticate(request.username, request.password)
        case request: ClientCredentialsRequest =>
          request.clientCredential match {
            case Some(credential) =>
              oauthClientDao.findClientCredentials(credential.clientId, credential.clientSecret.getOrElse(""))
                  .flatMap(oauthClient => accountDao.findById(oauthClient.get.ownerId))
            case None =>
              Future.successful(None)
          }
        case _ =>
          Future.successful(None)
      }
    }

    // Refresh token grant

    override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[Account]]] = {
      val oauthAccessToken = oauthAccessTokenDao.findByRefreshToken(refreshToken)
      oauthAccessToken.flatMap(oauthAccessToken => oauthAccessToken match {
        case Some(oauthAccessToken) => accountDao.findById(oauthAccessToken.accountId).flatMap(account => account match {
          case Some(account) => oauthClientDao.findClientId(oauthAccessToken.oauthClientId).map(client => Some(AuthInfo(
            user = account,
            clientId = Some(client.get.clientId),
            scope = None,
            redirectUri = client.get.redirectUri
          )))
          case None => Future.successful(None)
        })
        case None => Future.successful(None)
      })
    }

    override def refreshAccessToken(authInfo: AuthInfo[Account], refreshToken: String): Future[AccessToken] = {
      for {
        client <- oauthClientDao.findByClientId(authInfo.clientId.get)
        accessToken <- oauthAccessTokenDao.refresh(authInfo.user, client.get)
      } yield {
        toAccessToken(accessToken)
      }
    }

    // Authorization code grant

    override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[Account]]] = {
        val authorization = oauthAuthorizationCodeDao.findByCode(code)
        authorization.flatMap(authorization => authorization match {
          case Some(authorization) => accountDao.findById(authorization.accountId).flatMap(account => account match {
            case Some(account) => oauthClientDao.findClientId(authorization.oauthClientId).map(client => Some(AuthInfo(
              user = account,
              clientId = Some(client.get.clientId),
              scope = None,
              redirectUri = authorization.redirectUri
            )))
            case None => Future.successful(None)
          })
          case None => Future.successful(None)
        })
    }

    override def deleteAuthCode(code: String): Future[Unit] = {
      Future.successful(oauthAuthorizationCodeDao.delete(code))
    }

    // Protected resource

    override def findAccessToken(token: String): Future[Option[AccessToken]] = {
      oauthAccessTokenDao.findByAccessToken(token).map { oauthAccessToken =>
        oauthAccessToken.map {
          accessToken => toAccessToken(accessToken)
        }}
    }

    override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[Account]]] = {
      val token = oauthAccessTokenDao.findByAccessToken(accessToken.token)
      token.flatMap(token => token match {
        case Some(token) => accountDao.findById(token.accountId).flatMap(account => account match {
          case Some(account) => oauthClientDao.findClientId(token.oauthClientId).map(client => Some(AuthInfo(
            user = account,
            clientId = Some(client.get.clientId),
            scope = None,
            redirectUri = None
          )))
          case None => Future.successful(None)
        })
        case None => Future.successful(None)
      })
    }
  }
}
