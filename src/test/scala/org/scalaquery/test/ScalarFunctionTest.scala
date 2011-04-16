package org.scalaquery.test

import org.junit.Test
import org.junit.Assert._
import org.junit.runner.JUnitCore
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.basic.{BasicTable => Table}
import org.scalaquery.ql.extended.{DerbyDriver, AccessDriver}
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.test.util._
import org.scalaquery.test.util.TestDB._

object ScalarFunctionTest extends DBTestObject(H2Mem, SQLiteMem, Postgres, MySQL, DerbyMem, HsqldbMem, MSAccess)

class ScalarFunctionTest(tdb: TestDB) extends DBTest(tdb) {
  import tdb.driver.Implicit._

  @Test def test = db withSession {
    def check[T](q: Query[ColumnBase[T]], exp: T*) = {
      println("Executing: " + q.selectStatement)
      assertEquals(exp.toSet, q.list.toSet)
    }
    def checkIn[T](q: Query[ColumnBase[T]], exp: T*) = {
      println("Executing: " + q.selectStatement)
      val found = q.list.toSet
      assert(found.forall(exp contains _), "all of result "+found+" should be in expected "+exp)
    }

    check(Query("42".asColumnOf[Int]), 42)
    check(Query(ConstColumn("foo").length), 3)
    check(Query(ConstColumn("foo") ++ "bar"), "foobar")
    check(Query(ConstColumn(1) ifNull 42), 1)
    check(Query(ConstColumn[Option[Int]](None) ifNull 42), 42)
    check(Query(ConstColumn("Foo").toUpperCase), "FOO")
    check(Query(ConstColumn("Foo").toLowerCase), "foo")
    check(Query(ConstColumn("  foo  ").ltrim), "foo  ")
    check(Query(ConstColumn("  foo  ").rtrim), "  foo")
    check(Query(ConstColumn("  foo  ").trim), "foo")
    if(tdb.driver != DerbyDriver && tdb.driver != AccessDriver) {
      // Derby and Access do not support {fn database()}
      checkIn(Query(Functions.database.toLowerCase), tdb.dbName.toLowerCase, "")
    }
    if(tdb.driver != AccessDriver) {
      // Access does not support {fn user()}
      checkIn(Query(Functions.user.toLowerCase), tdb.userName.toLowerCase, "")
    }
    check(Query(ConstColumn(8) % 3 ), 2)
    check(Query(ConstColumn(-12.5).abs), 12.5)
    check(Query(ConstColumn(1.9).ceil), 2.0)
    check(Query(ConstColumn(1.5).ceil), 2.0)
    check(Query(ConstColumn(1.4).ceil), 2.0)
    check(Query(ConstColumn(-1.9).ceil), -1.0)
    check(Query(ConstColumn(-1.5).ceil), -1.0)
    check(Query(ConstColumn(-1.4).ceil), -1.0)
    check(Query(ConstColumn(1.5).floor), 1.0)
    check(Query(ConstColumn(1.4).floor), 1.0)
    check(Query(ConstColumn(-1.5).floor), -2.0)
    check(Query(ConstColumn(-10.0).sign), -1)
    check(Query(Functions.pi.toDegrees), 180.0)
    check(Query(Functions.pi.toDegrees.toRadians is Functions.pi), true)
  }
}
