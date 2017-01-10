package dao

import javax.inject.{Inject, Singleton}

import models.{Account, AccountTableDef}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by naveenkumar on 10/1/17.
  */
@Singleton
class AccountDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ctx: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val accounts = TableQuery[AccountTableDef]

  def authenticate(email: String, password: String): Future[Option[Account]] = {
    val query:Query[AccountTableDef, Account, Seq] = accounts.filter(account => account.email === email && account.password === password)
    dbConfig.db.run(query.result.headOption).map(account => account)
  }

  def findById(id: Long): Future[Option[Account]] = {
    val query:Query[AccountTableDef, Account, Seq] = accounts.filter(_.id === id)
    dbConfig.db.run(query.result.headOption).map(account => account)
  }
}