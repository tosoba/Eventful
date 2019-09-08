package com.example.ticketmasterapi.model

import com.example.core.model.ticketmaster.ILink
import com.example.core.model.ticketmaster.LinkType
import com.google.gson.annotations.SerializedName

data class ExternalLinks(
    val youtube: ExternalLink?,
    val twitter: ExternalLink?,
    @SerializedName("itunes")
    val iTunes: ExternalLink?,
    val facebook: ExternalLink?,
    val wiki: ExternalLink?,
    val instagram: ExternalLink?,
    @SerializedName("musicbrainz")
    val musicBrainz: ExternalLink?,
    @SerializedName("homepage")
    val homePage: ExternalLink?
) {
    val links: List<ILink>
        get() = this::class.members
            .map { it.name to it.call() as ExternalLink? }
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