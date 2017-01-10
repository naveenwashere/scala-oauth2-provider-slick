package dao

import javax.inject.Inject

import com.github.tototoshi.slick.MySQLJodaSupport._
import models.{OauthAuthorizationCode, OauthAuthorizationCodeTableDef}
import org.joda.time.DateTime
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by naveenkumar on 10/1/17.
  */
class OAuthAuthorizationCodeDao @Inject()(dbConfig: DatabaseConfig[JdbcProfile], oauthcodes: TableQuery[OauthAuthorizationCodeTableDef])(implicit ctx: ExecutionContext) {
  def findByCode(code: String): Future[Option[OauthAuthorizationCode]] = {
    val expireAt = new DateTime().minusMinutes(30)
    val query:Query[OauthAuthorizationCodeTableDef, OauthAuthorizationCode, Seq] = oauthcodes.filter(authcode => (authcode.code === code) && (authcode.createdAt > expireAt))
    dbConfig.db.run(query.result.headOption).map(oauthCode => oauthCode)
  }

  def delete(code: String): Unit = {
    val query:Query[OauthAuthorizationCodeTableDef, OauthAuthorizationCode, Seq] = oauthcodes.filter(_.code === code)
    dbConfig.db.run(query.delete)
  }
}
