package com.chrisbenincasa.geons.aws.dynamodb

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import shapeless.{::, <:!<, Default, HList, HNil, LabelledGeneric, Lazy, Poly1, Witness}
import shapeless.labelled.{FieldType, field}
import shapeless.ops.hlist.{Mapped, Mapper, Zip}
import shapeless.ops.record.Keys
import scala.collection.JavaConverters._

trait GenericDynamoModelComposer[T] {
  def from(m: Map[String, AttributeValue]): T

  def from(m: java.util.Map[String, AttributeValue]): T = from(m.asScala.toMap)
}

object GenericDynamoModelComposer {
  trait FieldBuilder[K, H, Q] {
    def extract(x: Q): Option[FieldType[K, H]]
  }

  implicit def mkFieldBuilder[K <: Symbol, H](
    implicit attrToH: AttributeValue => H
  ): FieldBuilder[K, H, AttributeValue] =
    new FieldBuilder[K, H, AttributeValue] {
      override def extract(x: AttributeValue): Option[FieldType[K, H]] = Some(field[K](attrToH(x)))
    }

  implicit val hnilDynamoComposer: GenericDynamoModelComposer[HNil] = new GenericDynamoModelComposer[HNil] {
    override def from(m: Map[String, AttributeValue]): HNil = HNil
  }

  implicit def hconsDynamoComposer[K <: Symbol, H, T <: HList](
    implicit
    witness: Witness.Aux[K],
    fieldBuilder: FieldBuilder[K, H, AttributeValue],
    tComposer: GenericDynamoModelComposer[T]
  ): GenericDynamoModelComposer[Option[FieldType[K, H]] :: T] = {
    val fieldName = witness.value.name
    new GenericDynamoModelComposer[Option[FieldType[K, H]] :: T] {
      override def from(m: Map[String, AttributeValue]): Option[FieldType[K, H]] :: T = {
        m.get(fieldName).flatMap(fieldBuilder.extract) :: tComposer.from(m - fieldName)
      }
    }
  }

  object applyDefaultValues extends Poly1 {
    implicit def hIsOptional[K <: Symbol, H <: Option[_], Default <: Option[H]] =
      at[(Option[FieldType[K, H]], Default)] {
        case (Some(o), _) => o
        case (None, None | _: None.type) => field[K][H](None.asInstanceOf[H])
        case (None, d) => field[K][H](d.get)
      }

    implicit def hIsRequired[K <: Symbol, H, Default <: Option[H]](
      implicit
      witness: Witness.Aux[K],
      notOptional: H <:!< Option[_]
    ) =
      at[(Option[FieldType[K, H]], Default)] {
        case (Some(o), _) => o
        case (None, None) => throw new IllegalArgumentException(s"Map contained no value (and there was no default) for required field: ${witness.value.name}")
        case (None, d) => field[K][H](d.get)
      }
  }

  implicit def genericDynamoComposer[
    A <: DynamoModel[_, _],
    Repr <: HList,
    Key <: HList,
    Defs <: HList,
    Zipped <: HList,
    Optional <: HList
  ](
    implicit
    gen: LabelledGeneric.Aux[A, Repr],
    keys: Keys.Aux[Repr, Key],
    defaults: Default.Aux[A, Defs],
    mapped: Mapped.Aux[Repr, Option, Optional],
    makerN: Lazy[GenericDynamoModelComposer[Optional]],
    zipped: Zip.Aux[Optional :: Defs :: HNil, Zipped],
    mapper: Mapper.Aux[applyDefaultValues.type, Zipped, Repr]
  ): DynamoModelComposer[A] = new DynamoModelComposer[A] {
    override def from(m: Map[String, AttributeValue]): A = {
      gen.from(mapper(zipped(makerN.value.from(m) :: defaults() :: HNil)))
    }
  }

  def apply[A <: DynamoModel[_, _]](implicit d: DynamoModelComposer[A]) = d
}
