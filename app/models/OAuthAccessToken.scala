package models

import java.sql.Timestamp

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime
import slick.driver.MySQLDriver.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery, Tag}

case class OauthAccessToken(
  id: Long,
  accountId: Long,
  oauthClientId: Long,
  accessToken: String,
  refreshToken: String,
  createdAt: DateTime
)

class OauthAccessTokenTableDef(tag: Tag) extends Table[OauthAccessToken](tag, "oauth_access_token") {
  implicit def dateTime =
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.getMillis),
      ts => new DateTime(ts.getTime)
    )

  val accounts: TableQuery[AccountTableDef] = TableQuery[AccountTableDef]
  val clients: TableQuery[OauthClientTableDef] = TableQuery[OauthClientTableDef]

  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def accountId: Rep[Long] = column[Long]("account_id")
  def oauthClientId: Rep[Long] = column[Long]("oauth_client_id")
  def accessToken: Rep[String] = column[String]("access_token")
  def refreshToken: Rep[String] = column[String]("refresh_token")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")

  def account: ForeignKeyQuery[AccountTableDef, Account] = foreignKey("oauth_access_token_account_id_fkey", accountId, accounts)(_.id)
  def client: ForeignKeyQuery[OauthClientTableDef, OauthClient] = foreignKey("oauth_access_token_oauth_client_id_fkey", oauthClientId, clients)(_.id)

  override def * : ProvenShape[OauthAccessToken] = (id, accountId, oauthClientId, accessToken, refreshToken, createdAt) <> ((OauthAccessToken.apply _).tupled, OauthAccessToken.unapply)
}
