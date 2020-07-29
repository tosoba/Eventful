package com.eventful.core.usecase.event

import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.event.IEvent
import com.eventful.core.util.PagedDataList
import com.eventful.core.util.ext.lowerCasedTrimmed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPagedEventsFlow @Inject constructor(private val dispatcher: CoroutineDispatcher) {
    operator fun <MappableToEvent> invoke(
        currentEvents: PagedDataList<MappableToEvent>,
        toEvent: (MappableToEvent) -> IEvent,
        getEvents: suspend (Int) -> Resource<PagedResult<IEvent>>
    ): Flow<Resource<PagedResult<IEvent>>> = flow {
        val currentEventNames = currentEvents.data.map(toEvent)
            .map { it.name.lowerCasedTrimmed }
            .toSet()
        var page = currentEvents.offset
        var resource: Resource<PagedResult<IEvent>>
        do {
            resource = withContext(dispatcher) { getEvents(page++) }
                .map { result ->
                    PagedResult(
                        items = result.items
                            .filterNot { currentEventNames.contains(it.name.lowerCasedTrimmed) }
                            .distinctBy { it.name.lowerCasedTrimmed },
                        currentPage = result.currentPage,
                        totalPages = result.totalPages
                    )
                }
        } while (resource is Resource.Success<PagedResult<IEvent>> &&
            resource.data.items.isEmpty() &&
            page < currentEvents.limit
        )
        emit(resource)
    }
}
