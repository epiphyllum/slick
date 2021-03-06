package scala.slick.driver

import scala.slick.ql._
import scala.slick.ast._
import scala.slick.util.ValueLinearizer

class H2Driver extends ExtendedDriver { driver =>

  override def createQueryBuilder(node: Node, vl: ValueLinearizer[_]): QueryBuilder = new QueryBuilder(node, vl)

  override def mapTypeName(tmd: TypeMapperDelegate[_]): String = tmd.sqlType match {
    case java.sql.Types.VARCHAR => "VARCHAR"
    case _ => super.mapTypeName(tmd)
  }

  class QueryBuilder(ast: Node, linearizer: ValueLinearizer[_]) extends super.QueryBuilder(ast, linearizer) {
    override protected val mayLimit0 = false
    override protected val concatOperator = Some("||")

    override def expr(n: Node) = n match {
      case Sequence.Nextval(seq) => b += "nextval(schema(), '" += seq.name += "')"
      case Sequence.Currval(seq) => b += "currval(schema(), '" += seq.name += "')"
      case _ => super.expr(n)
    }

    override protected def appendTakeDropClause(take: Option[Int], drop: Option[Int]) = (take, drop) match {
      case (Some(t), Some(d)) => b += " LIMIT " += t += " OFFSET " += d
      case (Some(t), None) => b += " LIMIT " += t
      case (None, Some(d)) => b += " LIMIT -1 OFFSET " += d
      case _ =>
    }
  }
}

object H2Driver extends H2Driver
