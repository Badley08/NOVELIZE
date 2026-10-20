package com.karlitodev.novelize.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository interface providing stream of data items.
 */
interface DataRepository {
  val data: Flow<List<String>>
}

/**
 * Default implementation of [DataRepository].
 */
class DefaultDataRepository : DataRepository {
  override val data: Flow<List<String>> = flow { emit(listOf("NoveLize")) }
}
