package com.eventful.test.rule

import android.util.Log
import com.eventful.core.android.model.event.Event
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope

inline fun <reified T : Any> relaxedMockedList(size: Int): List<T> = (1..size)
    .map { mockk<T>(relaxed = true) }

inline fun <reified T : Any> mockedList(size: Int, builder: (Int) -> T): List<T> = (1..size)
    .map(builder)

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

@ExperimentalCoroutinesApi
inline fun <T> TestCoroutineScope.onPausedDispatcher(block: () -> T): T {
    pauseDispatcher()
    val result = block()
    resumeDispatcher()
    return result
}

fun mockLog() {
    mockkStatic(Log::class)
    every { Log.v(any(), any()) } returns 0
    every { Log.d(any(), any()) } returns 0
    every { Log.i(any(), any()) } returns 0
    every { Log.e(any(), any()) } returns 0
}
