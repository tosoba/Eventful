package com.example.test.rule

import com.example.coreandroid.ticketmaster.Event
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope

fun relaxedEventsList(size: Int): List<Event> = (1..size).map { mockk<Event>(relaxed = true) }

fun eventsList(size: Int): List<Event> = (1..size).map {
    Event(
        it.toString(),
        "name$it",
        "url$it",
        "imageUrl$it",
        null,
        null,
        null,
        null,
        null,
        null,
        emptyList(),
        null,
        null,
        null
    )
}

@ExperimentalCoroutinesApi
inline fun <T> TestCoroutineScope.onPausedDispatcher(block: () -> T): T {
    pauseDispatcher()
    val result = block()
    resumeDispatcher()
    return result
}
