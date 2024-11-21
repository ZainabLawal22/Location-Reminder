package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Singleton class that is used to create a reminder db
 */
object LocalDB {

    /**
     * Static method that creates a reminder class and returns the DAO of the reminder
     */
    fun createRemindersDao(context: Context): RemindersDao {
        return Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "locationReminders.db"
        ).addMigrations(MIGRATION_1_2)
            .build().reminderDao()
    }

    /**
     * Migration from version 1 to 2: Add the `delete_flag` column to the `reminders` table.
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE reminders ADD COLUMN delete_flag INTEGER DEFAULT 0 NOT NULL" +
                    "ADD COLUMN request_code INTEGER")

        }
    }
}