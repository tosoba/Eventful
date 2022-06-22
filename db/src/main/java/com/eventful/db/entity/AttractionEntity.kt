package com.eventful.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.eventful.core.model.event.IAttraction
import com.eventful.db.Tables

@Entity(tableName = Tables.ATTRACTION, primaryKeys = ["id"])
data class AttractionEntity(
    override val id: String,
    override val name: String?,
    override val url: String?,
    override val links: List<LinkEntity>?,
    @ColumnInfo(name = "image_url") override val imageUrl: String?,
    override val kind: String?
) : IAttraction {
    constructor(
        other: IAttraction
    ) : this(
        other.id,
        other.name,
        other.url,
        other.links?.map { LinkEntity(it) },
        other.imageUrl,
        other.kind)
}
