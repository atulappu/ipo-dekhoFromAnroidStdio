package com.example.ipotracker.data.local.dao

import androidx.room.*
import com.example.ipotracker.data.local.entity.IpoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IpoDao {
    @Query("SELECT * FROM ipo_entries ORDER BY lastSyncedAt DESC")
    fun getAllIpos(): Flow<List<IpoEntity>>

    @Query("SELECT * FROM ipo_entries WHERE status = :status ORDER BY lastSyncedAt DESC")
    fun getIposByStatus(status: String): Flow<List<IpoEntity>>

    @Query("SELECT * FROM ipo_entries WHERE category = :category ORDER BY lastSyncedAt DESC")
    fun getIposByCategory(category: String): Flow<List<IpoEntity>>

    @Query("SELECT * FROM ipo_entries WHERE id = :id LIMIT 1")
    fun getIpoById(id: String): Flow<IpoEntity?>

    @Query("SELECT * FROM ipo_entries WHERE id = :id LIMIT 1")
    suspend fun getIpoByIdDirect(id: String): IpoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ipos: List<IpoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ipo: IpoEntity)

    @Update
    suspend fun update(ipo: IpoEntity)

    @Query("DELETE FROM ipo_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM ipo_entries")
    suspend fun getCount(): Int

    @Query("DELETE FROM ipo_entries")
    suspend fun clearAll()
}
