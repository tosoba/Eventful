package com.example.test.rule

import com.example.coreandroid.ticketmaster.Event
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope

fun relaxedEventsList(size: Int): List<Event> = (1..size).map { mockk<Event>(relaxed = true) }

fun event(index: Int = 0): Event = Event(
    index.toString(),
    "name$index",
    "url$index",
    "imageUrl$index",
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

fun eventsList(size: Int): List<Event> = (1..size).map { event(it) }

@ExperimentalCoroutinesApi
inline fun <T> TestCoroutineScope.onPausedDispatcher(block: () -> T): T {
    pauseDispatcher()
    val result = block()
    resumeDispatcher()
    return result
}
