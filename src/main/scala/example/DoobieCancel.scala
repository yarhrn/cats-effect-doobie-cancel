package example


import java.sql.{PreparedStatement, ResultSet}
import java.util.concurrent.TimeUnit

import cats.data.Kleisli
import cats.effect.{Async, IO}
import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.EmbeddedMysql.anEmbeddedMysql
import com.wix.mysql.distribution.Version.v5_7_latest
import doobie._
import doobie.implicits._

import scala.Predef._

object DoobieCancel extends App {


  object Interp extends KleisliInterpreter[IO] {
    val M = implicitly[Async[IO]]

    override lazy val PreparedStatementInterpreter =
      new PreparedStatementInterpreter {
        override def executeQuery = Kleisli[IO, PreparedStatement, ResultSet] {
          ps =>
            IO.cancelable {
              cb =>

                scala.concurrent.ExecutionContext.global.execute(() => {
                  try {
                    cb(Right(ps.executeQuery))
                  } catch {
                    case ex => cb(Left(ex))
                  }

                })
                IO {
                  ps.cancel()
                }
            }
        }
      }

  }

  val mysqld: EmbeddedMysql = anEmbeddedMysql(v5_7_latest).addSchema("test").start();
  val baseXa = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    s"jdbc:mysql://localhost:${mysqld.getConfig.getPort}/test",
    mysqld.getConfig.getUsername,
    mysqld.getConfig.getPassword
  )

  // A transactor that uses our interpreter above
  val xa: Transactor[IO] = Transactor.interpret.set(baseXa, Interp.ConnectionInterpreter)

  val unique = sql"""select md5('when will it end?') from ( select sleep(10)) ko""".query[String].unique
  val start = System.currentTimeMillis()
  val value = unique.transact(xa).unsafeRunCancelable(r => {

    println(s"Done: $r, time: ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}")
  })
  new Thread(() => {
    Thread.sleep(5000)
    value()
  }).start()

  Thread.sleep(100000)


}