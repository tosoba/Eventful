package com.example.core.model.ticketmaster

interface ILink {
    val url: String
    val type: LinkType

    companion object {
        fun with(url: String, type: LinkType) = object : ILink {
            override val url: String get() = url
            override val type: LinkType get() = type
        }
    }
}