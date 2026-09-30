package com.example.ipotracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ipotracker.data.local.dao.ExchangeConfigDao
import com.example.ipotracker.data.local.dao.IpoDao
import com.example.ipotracker.data.local.dao.RegistrarDao
import com.example.ipotracker.data.local.dao.SearchHistoryDao
import com.example.ipotracker.data.local.dao.WatchlistDao
import com.example.ipotracker.data.local.entity.ExchangeConfigEntity
import com.example.ipotracker.data.local.entity.IpoEntity
import com.example.ipotracker.data.local.entity.RegistrarEntity
import com.example.ipotracker.data.local.entity.SearchHistoryEntity
import com.example.ipotracker.data.local.entity.WatchlistEntity

@Database(
    entities = [
        WatchlistEntity::class,
        SearchHistoryEntity::class,
        RegistrarEntity::class,
        IpoEntity::class,
        ExchangeConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun registrarDao(): RegistrarDao
    abstract fun ipoDao(): IpoDao
    abstract fun exchangeConfigDao(): ExchangeConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ipo_tracker_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
