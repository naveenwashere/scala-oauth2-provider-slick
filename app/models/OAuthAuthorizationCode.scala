package models

import java.sql.Timestamp

import com.github.tototoshi.slick.MySQLJodaSupport._
import org.joda.time.DateTime
import slick.driver.MySQLDriver.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape, TableQuery, Tag}

case class OauthAuthorizationCode(
                                   id: Long,
                                   accountId: Long,
                                   oauthClientId: Long,
                                   code: String,
                                   redirectUri: Option[String],
                                   createdAt: DateTime)

class OauthAuthorizationCodeTableDef(tag: Tag) extends Table[OauthAuthorizationCode](tag, "oauth_authorization_code") {
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
  def code: Rep[String] = column[String]("code")
  def redirectUri: Rep[Option[String]] = column[Option[String]]("redirect_uri")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")

  def account: ForeignKeyQuery[AccountTableDef, Account] = foreignKey("oauth_authorization_owner_id_fkey", accountId, accounts)(_.id)
  def client: ForeignKeyQuery[OauthClientTableDef, OauthClient] = foreignKey("oauth_authorization_client_id_fkey", oauthClientId, clients)(_.id)

  def * : ProvenShape[OauthAuthorizationCode] = (id, accountId, oauthClientId, code, redirectUri, createdAt) <> ((OauthAuthorizationCode.apply _).tupled, OauthAuthorizationCode.unapply)
}
