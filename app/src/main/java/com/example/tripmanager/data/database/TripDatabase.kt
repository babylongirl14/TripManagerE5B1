package com.example.tripmanager.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.tripmanager.data.dao.DocumentDao
import com.example.tripmanager.data.dao.ItineraryDao
import com.example.tripmanager.data.dao.TripDao
import com.example.tripmanager.data.dao.UserDao
import com.example.tripmanager.data.model.Document
import com.example.tripmanager.data.model.ItineraryItem
import com.example.tripmanager.data.model.Trip
import com.example.tripmanager.data.model.User

@Database(
    entities = [User::class, Trip::class, Document::class, ItineraryItem::class],
    version = 3,
    exportSchema = false
)
abstract class TripDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun documentDao(): DocumentDao
    abstract fun itineraryDao(): ItineraryDao

    companion object {
        @Volatile
        private var INSTANCE: TripDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS documents (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        fileName TEXT NOT NULL,
                        filePath TEXT NOT NULL,
                        fileType TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS itinerary_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        activityDateTime INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        hasReminder INTEGER NOT NULL,
                        reminderTimeBefore TEXT NOT NULL,
                        alertType TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): TripDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TripDatabase::class.java,
                    "trip_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
