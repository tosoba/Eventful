package com.eventful.search

import androidx.lifecycle.SavedStateHandle
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@FlowPreview
@ExperimentalCoroutinesApi
internal class SearchStateWillUpdateTests : BaseSearchFlowProcessorTests() {

    private fun SearchFlowProcessor.stateWillUpdate(
        update: SearchStateUpdate,
        savedStateHandle: SavedStateHandle
    ) = stateWillUpdate(
        currentState = SearchState(),
        nextState = SearchState(),
        update = update,
        savedStateHandle = savedStateHandle
    )

    @Test
    @DisplayName("On Events.Loading update with null searchText - should not save searchText")
    fun nullSearchTextTest() = testScope.runBlockingTest {
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

        flowProcessor().stateWillUpdate(
            update = SearchStateUpdate.Events.Loading(null),
            savedStateHandle = savedStateHandle
        )

        verify(exactly = 0) { savedStateHandle.set<String>(SearchState.KEY_SEARCH_TEXT, any()) }
    }

    @Test
    @DisplayName("On Events.Loading update with non null searchText - should save searchText")
    fun allConditionsMetTest() = testScope.runBlockingTest {
        val searchText = "test"
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

        flowProcessor().stateWillUpdate(
            update = SearchStateUpdate.Events.Loading(searchText),
            savedStateHandle = savedStateHandle
        )

        verify(exactly = 1) { savedStateHandle[SearchState.KEY_SEARCH_TEXT] = searchText }
    }
}
