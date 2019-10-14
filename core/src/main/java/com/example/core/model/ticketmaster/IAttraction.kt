package com.example.core.model.ticketmaster

interface IAttraction {
    val id: String
    val name: String?
    val url: String
    val links: List<ILink>?
    val imageUrl: String?
    val kind: String?
}