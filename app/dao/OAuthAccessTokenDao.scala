package dao

import java.security.SecureRandom
import javax.inject.{Inject, Singleton}

import com.github.tototoshi.slick.MySQLJodaSupport._
import models._
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * Created by naveenkumar on 10/1/17.
  */
@Singleton
class OAuthAccessTokenDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val oauthtokens = TableQuery[OauthAccessTokenTableDef]

  def create(account: Account, client: OauthClient): Future[OauthAccessToken] = {
    def randomString(length: Int) = new Random(new SecureRandom()).alphanumeric.take(length).mkString
    val accessToken = randomString(40)
    val refreshToken = randomString(40)
    val createdAt = new DateTime()

    dbConfig.db.run(DBIO.seq(oauthtokens += OauthAccessToken(0, account.id, client.clientId, accessToken, refreshToken, createdAt))).flatMap(_ => {
      //We will definitely have the access token here!
      findByAccessToken(accessToken).map {accessToken => accessToken match {
        case _ => accessToken.get
      }}
    })
  }

  def delete(account: Account, client: OauthClient): Future[Int] = {
    val query:Query[OauthAccessTokenTableDef, OauthAccessToken, Seq] = oauthtokens.filter(oauth => oauth.accountId === account.id && oauth.oauthClientId === client.clientId)
    dbConfig.db.run(query.delete).map(id => id)
  }

  def refresh(account: Account, client: OauthClient): Future[OauthAccessToken] = {
    delete(account, client)
    create(account, client)
  }

  def findByAccessToken(accessToken: String): Future[Option[OauthAccessToken]] = {
    val query:Query[OauthAccessTokenTableDef, OauthAccessToken, Seq] = oauthtokens.filter(_.accessToken === accessToken)
    dbConfig.db.run(query.result.headOption).map(oauthAccessToken => oauthAccessToken)
  }

  def findByAuthorized(account: Account, clientId: String): Future[Option[OauthAccessToken]] = {
    val query:Query[OauthAccessTokenTableDef, OauthAccessToken, Seq] = oauthtokens.filter(oauth => oauth.accountId === account.id && oauth.oauthClientId === clientId)
    dbConfig.db.run(query.result.headOption).map(oauthAccessToken => oauthAccessToken)
  }

  def findByRefreshToken(refreshToken: String): Future[Option[OauthAccessToken]] = {
    val expireAt = new DateTime().minusMonths(1)
    val query:Query[OauthAccessTokenTableDef, OauthAccessToken, Seq] = oauthtokens.filter(oauth => oauth.refreshToken === refreshToken && oauth.createdAt > expireAt)
    dbConfig.db.run(query.result.headOption).map(oauthAccessToken => oauthAccessToken)
  }
}
