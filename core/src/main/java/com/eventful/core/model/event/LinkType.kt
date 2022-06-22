package com.eventful.core.model.event

import java.util.*

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
        fun fromString(str: String): LinkType? =
            values().find {
                it.typeStr.toLowerCase(Locale.getDefault()) == str.toLowerCase(Locale.getDefault())
            }
    }
}
