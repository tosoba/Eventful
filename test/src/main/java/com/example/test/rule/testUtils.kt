package com.example.test.rule

import com.example.coreandroid.ticketmaster.Event
import io.mockk.mockk

fun eventsList(size: Int): List<Event> = (1..size).map { mockk<Event>(relaxed = true) }