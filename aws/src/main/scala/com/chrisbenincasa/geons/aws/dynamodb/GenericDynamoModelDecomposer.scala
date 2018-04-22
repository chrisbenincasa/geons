package com.chrisbenincasa.geons.aws.dynamodb

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

trait GenericDynamoModelDecomposer[A] {
  def to(a: A): Map[String, AttributeValue]
}

object GenericDynamoModelDecomposer {
  implicit val hnilDynamoDecomposer: GenericDynamoModelDecomposer[HNil] = mkGenDynamoModelDecomposer(_ => Map.empty)

  implicit def hconsDynamoComposer[K <: Symbol, H, T <: HList](
    implicit
    witness: Witness.Aux[K],
    hDecomposer: Lazy[H => AttributeValue],
    tDecomposer: GenericDynamoModelDecomposer[T]
  ): GenericDynamoModelDecomposer[FieldType[K, H] :: T] = {
    val fieldName = witness.value.name
    mkGenDynamoModelDecomposer[FieldType[K, H] :: T] { hlist =>
      val h = hDecomposer.value(hlist.head)
      val t = tDecomposer.to(hlist.tail)
      Map(fieldName -> h) ++ t
    }
  }

  implicit def genericDynamoDecomposer[
    A <: DynamoModel[_, _],
    R <: HList
  ](
    implicit gen: LabelledGeneric.Aux[A, R],
    hDecomposer: Lazy[GenericDynamoModelDecomposer[R]]
  ): GenericDynamoModelDecomposer[A] = {
    mkGenDynamoModelDecomposer[A] { v =>
      hDecomposer.value.to(gen.to(v))
    }
  }

  def apply[A <: DynamoModel[_, _]](implicit d: GenericDynamoModelDecomposer[A]) = d

  private def mkGenDynamoModelDecomposer[A](fn: A => Map[String, AttributeValue]): GenericDynamoModelDecomposer[A] =
    new GenericDynamoModelDecomposer[A] {
      override def to(a: A): Map[String, AttributeValue] = fn(a)
    }
}


