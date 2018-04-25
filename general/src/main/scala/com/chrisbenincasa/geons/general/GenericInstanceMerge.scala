package com.chrisbenincasa.geons.general

import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist._

object GenericInstanceMerge extends GenericInstanceMerge0 {
  // Implicits that can be imported to allow the syntax:  "a1.merge(a0)"
  object syntax {
    final class GenMergeableOps[T](val x: T) extends AnyVal {
      def merge(other: T)(implicit g: GenericMergeable[T]): T = g.merge(x, other)
    }

    implicit def mergeableOps[T](x: T): GenMergeableOps[T] = new GenMergeableOps[T](x)
  }

  import GenericMergeable._

  // Termination case - hit after we've unfolded the whole HList
  implicit val mergeHNil: GenericMergeable[HNil] = makeMergeable((_, _) => HNil)

  // High pri implicit on values that are Optional
  // Defers to the mergeable instance for the "inner" type.
  // Uses Comapped to view the "unwrapped" type (with a slight hack to coerce the single type into an HList)
  //   i.e. Comapped views Option[X] as just X
  implicit def mergeOptValue[K, H <: Option[_], Out, Gen <: HList](
    implicit
    comapped: Comapped.Aux[H :: HNil, Option, Out :: HNil],
    genericMergeable: Lazy[GenericMergeable[Out]]
  ): GenericMergeable[FieldType[K, H]] = {
    makeMergeable[FieldType[K, H]] { (left, right) =>
      (left, right) match {
        case (l, r) if l.isDefined && r.isDefined =>
          val v = genericMergeable.value.merge(l.get.asInstanceOf[Out], r.get.asInstanceOf[Out])
          field[K](Some(v).asInstanceOf[H])
        case (l, _) if l.isDefined => l
        case (_, r) => r
      }
    }
  }

  // HCons step in a partial merge
  // This produces an instance that can merge the head of an HList and then starts merge on the tail of the HList
  implicit def mergeHCons[K, H, T <: HList](
    implicit
    mergeValue: Lazy[GenericMergeable[H]],
    tailMerge: GenericMergeable[T]
  ): GenericMergeable[H :: T] = {
    makeMergeable[H :: T]((left, right) => {
      mergeValue.value.merge(left.head, right.head) :: tailMerge.merge(left.tail, right.tail)
    })
  }

  // Entry point implicit for mergeable case classes
  // Deconstructs the case classes into their generic representation and then proceeds to merge
  implicit def mergeGenerics[T, Gen <: HList, Test <: HList](
    implicit
    gen: LabelledGeneric.Aux[T, Gen],
    hMergeable: Lazy[GenericMergeable[Gen]]
  ): GenericMergeable[T] = {
    makeMergeable((left, right) => gen.from(hMergeable.value.merge(gen.to(left), gen.to(right))))
  }
}

trait GenericInstanceMerge0 {
  import GenericMergeable._

  // Low-pri implicit on all regular values.
  // Applies the polymorphic function "mergeGenericField" to handle the merge
  // between left and right
  implicit def mergeRegValue0[K, H, Out <: HList](
    implicit mapper: Mapper.Aux[mergeGenericField.type, (FieldType[K, H], FieldType[K, H]) :: HNil, FieldType[K, H] :: HNil]
  ): GenericMergeable[FieldType[K, H]] = {
    makeMergeable((left, right) => {
      mapper((left -> right) :: HNil).head
    })
  }
}

object GenericMergeable {
  /**
   * Helper for creating instances of GenericMergeable
   * @param f
   * @tparam T
   * @return
   */
  def makeMergeable[T](f: (T, T) => T): GenericMergeable[T] = new GenericMergeable[T] {
    override def merge(left: T, right: T): T = f(left, right)
  }
}

trait GenericMergeable[T] {
  def merge(left: T, right: T): T
}

object mergeGenericField extends Poly1 {
  // Handles a merge case where the Left and Right values are Option types.
  // Gives precedence to the Left-hand value only if it is Some(_)
  implicit def mergeOptionals[K, V <: Option[_]]: Case.Aux[(FieldType[K, V], FieldType[K, V]), FieldType[K, V]] =
    at[(FieldType[K, V], FieldType[K, V])] {
      case (l, _) if l.isDefined => l
      case (_, r) => r
    }

  // Handles the simplest merge case, where Left and Right are non-Option types.
  // Gives the left side precedence always.
  implicit def mergeRegular[K <: Symbol, V](
    implicit
    witness: Witness.Aux[K],
    notOptional: V <:!< Option[_]
  ): Case.Aux[(FieldType[K, V], FieldType[K, V]), FieldType[K, V]] =
    at[(FieldType[K, V], FieldType[K, V])] {
      case (x, _) => x
    }
}