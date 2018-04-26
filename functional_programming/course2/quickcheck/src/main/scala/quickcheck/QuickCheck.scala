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

  property("del min") = forAll { (h: H) =>
    if (!isEmpty(h)) {
      val m = findMin(h)
      val h1 = deleteMin(h)
      isEmpty(h1) || m <= findMin(h1)
    }
    else
      true
  }

  property("meld min") = forAll { (h1: H, h2: H) =>
    isEmpty(h1) || isEmpty(h2) ||
      findMin(meld(h1, h2)) == math.min(findMin(h1), findMin(h2))
  }

  property("sorting") = forAll { (h: H) =>
    def isSorted(h1: H, min: Int): Boolean = {
      isEmpty(h1) || ((findMin(h1) >= min) && isSorted(deleteMin(h1), findMin(h1)))
    }
    isSorted(h, Int.MinValue)
  }




  /*
    property("meld empty") = forAll { (h: H) =>
      if(isEmpty(h)) isEmpty(meld(empty, h)) else !isEmpty(meld(empty, h))
    }
  */


}
