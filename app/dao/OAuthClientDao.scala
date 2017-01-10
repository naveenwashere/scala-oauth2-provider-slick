package dao

import javax.inject.Inject

import models.{OauthClient, OauthClientTableDef}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by naveenkumar on 10/1/17.
  */
class OAuthClientDao @Inject()(dbConfig: DatabaseConfig[JdbcProfile], clients: TableQuery[OauthClientTableDef])(implicit ctx: ExecutionContext) {

  def validate(clientId: String, clientSecret: String, grantType: String): Future[Boolean] = {
    val query:Query[OauthClientTableDef, OauthClient, Seq] = clients.filter(_.clientId === clientId).filter(_.clientSecret === clientSecret)
    dbConfig.db.run(query.result.headOption).map(client => grantType == client.get.grantType || grantType == "refresh_token").map(booleanVal => booleanVal)
  }

  def findByClientId(clientId: String): Future[Option[OauthClient]] = {
    val query:Query[OauthClientTableDef, OauthClient, Seq] = clients.filter(_.clientId === clientId)
    dbConfig.db.run(query.result.headOption).map(oauthClient => oauthClient)
  }

  def findClientCredentials(clientId: String, clientSecret: String): Future[Option[OauthClient]] = {
    val query:Query[OauthClientTableDef, OauthClient, Seq] = clients.filter(_.clientId === clientId).filter(client => client.clientSecret === clientSecret && client.grantType === "client_credentials")
    dbConfig.db.run(query.result.headOption).map(oauthClient => oauthClient)
  }
}
