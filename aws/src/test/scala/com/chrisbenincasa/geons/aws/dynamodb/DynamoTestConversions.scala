package com.chrisbenincasa.geons.aws.dynamodb

import com.amazonaws.services.dynamodbv2.model.AttributeValue

object DynamoTestConversions {
  implicit val attrToStr: AttributeValue => String = x => x.getS
  implicit val strToAttr: String => AttributeValue = x => new AttributeValue(x)
}
