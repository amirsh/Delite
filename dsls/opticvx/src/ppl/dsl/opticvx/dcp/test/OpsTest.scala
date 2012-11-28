package ppl.dsl.opticvx.dcp.test

import scala.collection.immutable.Seq
import ppl.dsl.opticvx.dcp._

object DCPOpsTestApp extends DCPOpsDefinite {
  def main(args: Array[String]) {
    val n = cvxparam()
    val l = cvxexpr()
    val x = cvxexpr()
    val y = cvxexpr()
    val z = cvxexpr()
    solve(
      params(3 -> n),
      given(double2inputdesc(3.4) -> l),
      over(scalar -> x, vector(n) -> y),
      let(x + x -> z),
      where(
        cfor(n)(i => (y(i) <= x)),
        x >= 0
      ),
      minimize(
        z
      )
    )

    /*
    solve(
      params(),
      given(),
      over(scalar -> x, vector(input.length) -> y),
      let(x + x -> z),
      where(
        cfor(input.length)((n) => (y(n) <= x)),
        x >= 0
      ),
      minimize(
        sum(input.length)((n) => y(n)*input(n)) - x
      )
    )
    */
  }
}
