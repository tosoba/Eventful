package com.example.ticketmasterapi

import com.example.ticketmasterapi.model.Classification
import com.example.ticketmasterapi.model.Image

val List<Image>.imageUrl: String
    get() = find { it.width == 1136 }?.url ?: firstOrNull()?.url ?: "" // TODO: placeholder url

val List<Classification>.kind: String?
    get() = firstOrNull()
        ?.run {
            if (segment == null) null
            else "${segment.name} ${genre?.name ?: ""}${subGenre?.name?.let { "| $it" } ?: ""}".trim()
        }

val List<Classification>.kinds: List<String>
    get() = map { listOf(it.genre?.name, it.segment?.name, it.subGenre?.name) }
        .flatten()
        .filterNotNull()
        .distinctBy(String::toLowerCase)