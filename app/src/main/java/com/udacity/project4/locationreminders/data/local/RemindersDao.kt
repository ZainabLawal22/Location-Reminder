package com.udacity.project4.locationreminders.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

/**
 * Data Access Object for the reminders table.
 */
@Dao
interface RemindersDao {

    /**
     * @return all reminders.
     */
    @Query("SELECT * FROM reminders WHERE delete_flag = 0")
    suspend fun getReminders(): List<ReminderDTO>

    /**
     * @param reminderId the id of the reminder
     * @return the reminder object with the reminderId
     */
    @Query("SELECT * FROM reminders where entry_id = :reminderId AND delete_flag = 0")
    suspend fun getReminderById(reminderId: String): ReminderDTO?

    /**
     * Insert a reminder in the database. If the reminder already exists, replace it.
     * @param reminder the reminder to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReminder(reminder: ReminderDTO)

    /**
     * Delete all reminders.
     */
    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    /**
     * Delete reminders by id.
     */
    @Query("UPDATE reminders SET delete_flag = 1 WHERE entry_id IN (:id)")
    suspend fun deleteReminders(id: List<String>)

    /**
     * Get last requestCode.
     */
    @Query("SELECT request_code FROM reminders ORDER BY request_code DESC LIMIT 1")
    suspend fun getLastRequestCode(): Int?

}