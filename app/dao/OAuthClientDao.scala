package dao

import javax.inject.{Inject, Singleton}

import models.{OauthAuthorizationCodeTableDef, OauthClient, OauthClientTableDef}
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by naveenkumar on 10/1/17.
  */
@Singleton
class OAuthClientDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val clients = TableQuery[OauthClientTableDef]

  def validate(clientId: String, clientSecret: String, grantType: String): Future[Boolean] = {
    val query:Query[OauthClientTableDef, OauthClient, Seq] = clients.filter(_.clientId === clientId).filter(_.clientSecret === clientSecret)
    dbConfig.db.run(query.result.headOption).map(client => client match {
      case Some(oauthClient) => (grantType == oauthClient.grantType || grantType == "refresh_token")
      case None => false
    })
  }

  def findByClientId(clientId: String): Future[Option[OauthClient]] = {
    val query:Query[OauthClientTableDef, OauthClient, Seq] = clients.filter(_.clientId === clientId)
    dbConfig.db.run(query.result.headOption).map(oauthClient => oauthClient)
  }

  def findClientCredentials(clientId: String, clientSecret: String): Future[Option[OauthClient]] = {
    val query:Query[OauthClientTableDef, OauthClient, Seq] = clients.filter(_.clientId === clientId).filter(client => client.clientSecret === clientSecret && client.grantType === "client_credentials")
    dbConfig.db.run(query.result.headOption).map(oauthClient => oauthClient)
  }

  def findClientId(id: Long): Future[Option[OauthClient]] = {
    val query:Query[OauthClientTableDef, OauthClient, Seq] = clients.filter(_.id === id)
    dbConfig.db.run(query.result.headOption).map(oauthClient => oauthClient)
  }
}
