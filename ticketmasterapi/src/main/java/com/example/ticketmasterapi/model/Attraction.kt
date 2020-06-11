package com.example.ticketmasterapi.model

import com.example.core.model.event.IAttraction
import com.example.core.model.event.ILink
import com.example.ticketmasterapi.imageUrl
import com.example.ticketmasterapi.kind

data class Attraction(
    val classifications: List<Classification>?,
    override val id: String,
    val images: List<Image>?,
    override val name: String?,
    override val url: String?,
    val externalLinks: ExternalLinks?
) : IAttraction {
    override val links: List<ILink> get() = externalLinks?.links ?: emptyList()
    override val imageUrl: String? get() = images?.imageUrl
    override val kind: String? get() = classifications?.kind
}
