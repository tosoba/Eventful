package com.example.search

import com.example.coreandroid.util.Initial
import com.example.coreandroid.util.LoadingFailed
import com.example.test.rule.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
internal class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private val viewModelWithRelaxedUseCases: SearchViewModel
        get() = SearchViewModel(
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            testDispatcher
        )

    @Test
    fun `GivenSearchVM WhenInitialized StateIsInitial`() = runBlocking {
        val vm = viewModelWithRelaxedUseCases
        val states = mutableListOf<SearchState>()
        vm.state.take(1).collect { states.add(it) }
        val (_, _, events) = states.last()
        val (value, status, _, _) = events
        assert(value.isEmpty() && status is Initial)
    }

    @Test
    fun `GivenSearchVM WhenNoEventsLoadedAndOnNotConnected StateStatusIsLoadingError`() =
        runBlocking {
            val vm = viewModelWithRelaxedUseCases

            val states = mutableListOf<SearchState>()
            val loadingJob = launch {
                vm.state.take(2).collect { states.add(it) }
            }

            vm.onNotConnected()
            loadingJob.join()

            val (_, _, events) = states.last()
            val (value, status, _, _) = events
            assert(value.isEmpty() && status is LoadingFailed<*> && status.error == SearchError.NotConnected)
        }
}