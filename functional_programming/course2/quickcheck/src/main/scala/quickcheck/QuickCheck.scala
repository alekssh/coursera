package quickcheck

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalacheck._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  lazy val genHeap: Gen[H] =
    Gen.oneOf(Gen.const(empty),
      for {
        n <- arbitrary[Int]
        k <- Gen.oneOf(Gen.const(empty), genHeap)
      } yield insert(n, k)
    )


  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  property("min") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }


  property("elements") = forAll { (a: H, b: H) =>

    def compareMins(melded: H, h1: H, h2: H): Boolean = {
      if (isEmpty(melded))
        true
      else {
        val min = findMin(melded)

        if (!isEmpty(h1) && min == findMin(h1))
          compareMins(deleteMin(melded), deleteMin(h1), h2)
        else if (!isEmpty(h2) && min == findMin(h2))
          compareMins(deleteMin(melded), h1, deleteMin(h2))
        else
          false
      }
    }

    compareMins(meld(a, b), a, b)
  }


}
