package com.example.ticketmasterapi.model

import com.example.core.model.ticketmaster.ILink
import com.google.gson.annotations.SerializedName

data class ExternalLinks(
    val youtube: List<ExternalLink>?,
    val twitter: List<ExternalLink>?,
    @SerializedName("itunes")
    val iTunes: List<ExternalLink>?,
    val facebook: List<ExternalLink>?,
    val wiki: List<ExternalLink>?,
    val instagram: List<ExternalLink>?,
    //TODO: this is discarded for now because it has an id instead of a url
    @SerializedName("musicbrainz")
    val musicBrainz: List<ExternalId>?,
    @SerializedName("homepage")
    val homePage: List<ExternalLink>?
) {
    val links: List<ILink>
        get() = emptyList()
}