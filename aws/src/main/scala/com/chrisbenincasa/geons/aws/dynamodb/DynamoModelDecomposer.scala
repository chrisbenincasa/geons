//
// DynamoModelDecomposer.scala
//
// Copyright (c) 2017 by Curalate, Inc.
//

package com.chrisbenincasa.geons.aws.dynamodb

/**
 * Break down a dynamo model into a map
 *
 * @tparam T
 */
trait DynamoModelDecomposer[T <: DynamoModel[_, _]] extends GenericDynamoModelDecomposer[T]
