package example

import com.wix.mysql.EmbeddedMysql.anEmbeddedMysql
import com.wix.mysql.distribution.Version.v5_7_latest
import java.sql.DriverManager

import com.wix.mysql.EmbeddedMysql

object RawJDBCCancelMain {

  def main(args: Array[String]): Unit = {

    val mysqld: EmbeddedMysql = anEmbeddedMysql(v5_7_latest).addSchema("test").start();



    val con = DriverManager.getConnection(s"jdbc:mysql://localhost:${mysqld.getConfig.getPort}/test",
      mysqld.getConfig.getUsername,
      mysqld.getConfig.getPassword)



    val stmt = con.createStatement()

    val start = System.currentTimeMillis()
    new Thread(() => {
      Thread.sleep(3000)
      stmt.cancel()
    }).start()

    val rs = stmt.executeQuery("select sleep(10)")

    rs.next()

    println(s"${rs.getInt(1)} ${System.currentTimeMillis() - start}")
  }

}
