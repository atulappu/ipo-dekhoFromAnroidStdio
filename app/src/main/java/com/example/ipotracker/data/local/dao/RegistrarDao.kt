package com.example.ipotracker.data.local.dao

import androidx.room.*
import com.example.ipotracker.data.local.entity.RegistrarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistrarDao {
    @Query("SELECT * FROM registrars ORDER BY issuesManaged DESC")
    fun getAllRegistrars(): Flow<List<RegistrarEntity>>

    @Query("SELECT * FROM registrars WHERE id = :id LIMIT 1")
    suspend fun getRegistrarById(id: String): RegistrarEntity?

    @Query("SELECT COUNT(*) FROM registrars")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(registrars: List<RegistrarEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registrar: RegistrarEntity)

    @Update
    suspend fun update(registrar: RegistrarEntity)

    @Query("UPDATE registrars SET url = :newUrl, comment = :comment, modifyDate = :modifyDate, modifyBy = :modifyBy WHERE id = :id")
    suspend fun updateRegistrarUrl(
        id: String,
        newUrl: String,
        comment: String,
        modifyDate: String,
        modifyBy: String
    )

    @Query("UPDATE registrars SET name = :name, url = :url, issuesManaged = :issuesManaged, issueAmountCr = :issueAmountCr, comment = :comment, modifyDate = :modifyDate, modifyBy = :modifyBy WHERE id = :id")
    suspend fun updateFullRegistrar(
        id: String,
        name: String,
        url: String,
        issuesManaged: Int,
        issueAmountCr: Double,
        comment: String,
        modifyDate: String,
        modifyBy: String
    )
}
