package com.chrisbenincasa.geons.aws.dynamodb

/**
 * A dynamo model that has a primary key and a sort key
 *
 * @tparam PrimaryKey
 * @tparam SortKey
 */
trait DynamoModel[PrimaryKey, +SortKey]

/**
 * Extension trait for models that have no sort key
 *
 * @tparam PrimaryKey
 */
trait DynamoModelNoSortKey[PrimaryKey] extends DynamoModel[PrimaryKey, Null]