package com.chrisbenincasa.geons.aws.dynamodb

/**
 * Create a dynamo model from a map
 *
 * @tparam T
 */
trait DynamoModelComposer[T <: DynamoModel[_, _]] extends GenericDynamoModelComposer[T]