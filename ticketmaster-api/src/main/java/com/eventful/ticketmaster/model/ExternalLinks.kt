package com.eventful.ticketmaster.model

import com.eventful.core.model.event.ILink
import com.eventful.core.model.event.LinkType
import com.google.gson.annotations.SerializedName

data class ExternalLinks(
    val youtube: List<ExternalLink>?,
    val twitter: List<ExternalLink>?,
    @SerializedName("itunes")
    val iTunes: List<ExternalLink>?,
    val facebook: List<ExternalLink>?,
    val wiki: List<ExternalLink>?,
    val instagram: List<ExternalLink>?,
    @SerializedName("musicbrainz")
    val musicBrainz: List<ExternalId>?,
    @SerializedName("homepage")
    val homePage: List<ExternalLink>?
) {
    val links: List<ILink>
        get() = listOf(
            ExternalLinks::youtube.name to youtube?.firstOrNull()?.url,
            ExternalLinks::twitter.name to twitter?.firstOrNull()?.url,
            ExternalLinks::iTunes.name to iTunes?.firstOrNull()?.url,
            ExternalLinks::facebook.name to facebook?.firstOrNull()?.url,
            ExternalLinks::wiki.name to wiki?.firstOrNull()?.url,
            ExternalLinks::instagram.name to instagram?.firstOrNull()?.url,
            ExternalLinks::musicBrainz.name to musicBrainz?.firstOrNull()?.id,
            ExternalLinks::homePage.name to homePage?.firstOrNull()?.url
        ).filter { it.second != null }.map {
            ILink.with(
                it.second!!,
                LinkType.fromString(it.first)!!
            )
        }
}
