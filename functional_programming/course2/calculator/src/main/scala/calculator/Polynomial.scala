package calculator

object Polynomial {
  def computeDelta(a: Signal[Double], b: Signal[Double],
                   c: Signal[Double]): Signal[Double] = {

    Signal {
      val b_val: Double = b()
      b_val * b_val - 4 * a() * c()
    }

  }

  def computeSolutions(a: Signal[Double], b: Signal[Double],
                       c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] = {
    Signal {
      val delta_val: Double = delta()
      val a_val: Double = a()
      val b_val: Double = b()
      Set((-b_val - Math.sqrt(delta_val)) / (2 * a_val), (-b_val + Math.sqrt(delta_val)) / (2 * a_val))
    }
  }
}
