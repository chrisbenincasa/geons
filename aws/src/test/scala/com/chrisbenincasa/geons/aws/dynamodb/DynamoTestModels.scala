package com.chrisbenincasa.geons.aws.dynamodb

case class SimpleIndex(
  key: String,
  value: String
) extends DynamoModelNoSortKey[String]