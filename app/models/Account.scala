package models

import java.sql.Timestamp

import org.joda.time.DateTime
import slick.driver.MySQLDriver.api._
import slick.lifted.{ProvenShape, Tag}

case class Account(id: Long, email: String, password: String, createdAt: DateTime)

class AccountTableDef(tag: Tag) extends Table[Account](tag, "account") {
  implicit def dateTime =
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.getMillis),
      ts => new DateTime(ts.getTime)
    )
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def email: Rep[String] = column[String]("email")
  def password: Rep[String] = column[String]("password")
  def createdAt: Rep[DateTime] = column[DateTime]("created_at")

  def * : ProvenShape[Account] = (id, email, password, createdAt) <> ((Account.apply _).tupled, Account.unapply)
}