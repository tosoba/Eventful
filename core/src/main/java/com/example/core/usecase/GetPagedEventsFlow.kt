package com.example.core.usecase

import com.example.core.model.Resource
import com.example.core.model.PagedResult
import com.example.core.model.event.IEvent
import com.example.core.model.event.trimmedLowerCasedName
import com.example.core.util.PagedDataList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPagedEventsFlow @Inject constructor(private val dispatcher: CoroutineDispatcher) {
    operator fun <Event> invoke(
        currentEvents: PagedDataList<Event>,
        toEvent: (Event) -> IEvent,
        getEvents: suspend (Int) -> Resource<PagedResult<IEvent>>
    ): Flow<Resource<PagedResult<IEvent>>> = flow {
        val currentEventNames = currentEvents.data.map(toEvent)
            .map { it.trimmedLowerCasedName }
            .toSet()
        var page = currentEvents.offset
        var resource: Resource<PagedResult<IEvent>>
        do {
            resource = withContext(dispatcher) { getEvents(page++) }
                .map { result ->
                    PagedResult(
                        items = result.items
                            .filterNot { currentEventNames.contains(it.trimmedLowerCasedName) }
                            .distinctBy { it.trimmedLowerCasedName },
                        currentPage = result.currentPage,
                        totalPages = result.totalPages
                    )
                }
        } while (resource is Resource.Success<PagedResult<IEvent>>
            && resource.data.items.isEmpty()
            && page < currentEvents.limit
        )
        emit(resource)
    }
}