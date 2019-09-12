package com.example.ticketmasterapi.model

import com.example.core.model.ticketmaster.ILink
import com.example.core.model.ticketmaster.LinkType
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
        get() = this::class.members
            .map { it.name to it.call() as? ExternalLink? }
            .filter { it.second != null }
            .map { it.first to it.second!! }
            .map { (type, link) ->
                object : ILink {
                    override val url: String
                        get() = link.url
                    override val type: LinkType
                        get() = LinkType.fromString(type)!!

                }
            }
}