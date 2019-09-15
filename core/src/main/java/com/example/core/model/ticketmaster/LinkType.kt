package com.example.core.model.ticketmaster

enum class LinkType(private val typeStr: String) {
    YOUTUBE("youtube"),
    TWITTER("twitter"),
    ITUNES("itunes"),
    FACEBOOK("facebook"),
    WIKI("wiki"),
    INSTAGRAM("instagram"),
    MUSICBRAINZ("musicbrainz"),
    HOMEPAGE("homepage");

    companion object {
        fun fromString(str: String): LinkType? = values()
            .find { it.typeStr.toLowerCase() == str.toLowerCase() }
    }
}