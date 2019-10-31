package com.example.favourites

import com.example.core.model.ticketmaster.IEvent
import com.example.core.usecase.GetSavedEvents
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
internal class FavouritesViewModelTest {

    private val mainThreadSurrogate = newSingleThreadContext("Main Thread")
    private val testDispatcher = TestCoroutineDispatcher()
    private val getSavedEvents = mockk<GetSavedEvents> {
        coEvery { this@mockk.invoke(any()) } returns flowOf(listOf<IEvent>())
    }

    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GivenFavouritesViewModel WhenInit ThenShouldGetSavedEvents`() {
        FavouritesViewModel(getSavedEvents, testDispatcher)
        coVerify { getSavedEvents.invoke(any()) }
    }
}