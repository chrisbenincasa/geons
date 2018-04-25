package com.chrisbenincasa.geons.general

import org.scalatest.{Assertions, FlatSpec}
import shapeless.LabelledGeneric
import com.chrisbenincasa.geons.general.GenericInstanceMerge._
import com.chrisbenincasa.geons.general.GenericInstanceMerge.syntax._
import org.scalacheck.ScalacheckShapeless._
import org.scalatest.prop.GeneratorDrivenPropertyChecks

case class B(x: Option[Int] = None, q: Option[Int] = None)
case class A(id: Int, other: Option[String] = None, b: Option[B] = None)

class GenericMergeTest extends FlatSpec with Assertions with GeneratorDrivenPropertyChecks {
  "GenericInstanceMerge" should "merge" in {
    forAll() { (a: A, a2: A) =>
      val expectedB = if (a2.b.isDefined || a.b.isDefined) {
        Some(
          B(
            x = a2.b.flatMap(_.x).orElse(a.b.flatMap(_.x)),
            q = a2.b.flatMap(_.q).orElse(a.b.flatMap(_.q))
          )
        )
      } else None

      val expected = A(
        id = a2.id,
        other = a2.other.orElse(a.other),
        b = expectedB
      )

      assert(a2.merge(a) === expected)
    }
  }

  it should "merge nested structures" in {
    val a1 = A(
      id = 2,
      other = Some("1"),
      b = Some(
        B(
          x = None,
          q = Some(20)
        )
      )
    )

    val a2 = A(
      a1.id,
      b = Some(
        B(
          x = Some(1)
        )
      )
    )

    // Preserve "q" value in "a1"
    assert(a2.merge(a1) === A(2, Some("1"), Some(B(Some(1), Some(20)))))
  }

  it should "do nothing when left-side has Nones" in {
    forAll() { a: A =>
      val a2 = A(a.id)

      assert(a2.merge(a) === a)
    }
  }

  implicit def gen[H](v: Option[H])(implicit g: LabelledGeneric[H]) = g
}
