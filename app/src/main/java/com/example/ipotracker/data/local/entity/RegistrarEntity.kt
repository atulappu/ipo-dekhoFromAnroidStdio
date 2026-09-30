package com.example.ipotracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ipotracker.data.model.RegistrarItem

@Entity(tableName = "registrars")
data class RegistrarEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val url: String,
    val issuesManaged: Int,
    val issueAmountCr: Double,
    val comment: String,
    val createdDate: String,
    val modifyDate: String,
    val modifyBy: String
) {
    fun toDomain(): RegistrarItem = RegistrarItem(
        id = id,
        name = name,
        url = url,
        issuesManaged = issuesManaged,
        issueAmountCr = issueAmountCr,
        comments = comment,
        createdDate = createdDate,
        modifiedDate = modifyDate,
        modifiedBy = modifyBy
    )

    companion object {
        fun fromDomain(domain: RegistrarItem): RegistrarEntity = RegistrarEntity(
            id = domain.id,
            name = domain.name,
            url = domain.url,
            issuesManaged = domain.issuesManaged,
            issueAmountCr = domain.issueAmountCr,
            comment = domain.comments ?: "",
            createdDate = domain.createdDate,
            modifyDate = domain.modifiedDate,
            modifyBy = domain.modifiedBy
        )
    }
}
