package com.example.db.entity

import androidx.room.Entity
import com.example.db.Tables

@Entity(tableName = Tables.ATTRACTION, primaryKeys = ["id"])
data class AttractionEntity(
    val id: String,
    val name: String?,
    val url: String,
    val links: List<LinkEntity>,
    val imageUrl: String?,
    val kind: String?
)