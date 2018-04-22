package com.chrisbenincasa.geons.aws.dynamodb

import org.scalatest.{Assertions, FlatSpec}
import DynamoTestConversions._
import com.amazonaws.services.dynamodbv2.model.AttributeValue

class DynamoMapperSpec extends FlatSpec with Assertions {
  "DynamoDecomposer" can "decompose to a simple Map" in {
    val decomposer = GenericDynamoModelDecomposer[SimpleIndex]
    val composer = GenericDynamoModelComposer[SimpleIndex]

    val simple = SimpleIndex("one", "two")

    val map = decomposer.to(simple)

    assert(map === Map("key" -> new AttributeValue("one"), "value" -> new AttributeValue("two")))

    val reconsituted = composer.from(map)

    assert(simple === reconsituted)
  }
}
