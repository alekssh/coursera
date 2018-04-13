package recfun

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
  }

  /**
    * Exercise 1
    */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || r == c) 1
    else pascal(c, r - 1) + pascal(c - 1, r - 1)
  }

  /**
    * Exercise 2
    */
  def balance(chars: List[Char]): Boolean = {

    def loop(open: Int, list: List[Char]): Boolean = {
      if (open == 0 && list.isEmpty)
        return true
      if (open < 0)
        return false

      val count = if (list.head == '(') {
        1
      } else {
        if (list.head == ')') -1 else 0
      }
      loop(open + count, list.tail)

    }

    loop(0, chars)

  }

  /**
    * Exercise 3
    */
  def countChange(money: Int, coins: List[Int]): Int = {

    def count(money: Int, coins: List[Int]): Int = {
      if (money <= 0 || coins.isEmpty) return 0
      if (money == coins.head) return 1
      count(money - coins.head, coins) + count(money, coins.tail)
    }

    count(money, coins.sorted)
  }

}
