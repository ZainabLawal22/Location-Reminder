package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDao : RemindersDao {

    companion object {
        val MOCK_REMINDER_DTO =
            ReminderDTO("title", "description", "location", 0.0, 0.0, "uuid",0 ,10)
        val MOCK_REMINDER =
            ReminderDataItem("title", "description", "location", 0.0, 0.0, "uuid",0, 10)
    }

    private val reminderList = mutableListOf<ReminderDTO>()

    var shouldReturnError = false

    /**
     * @return all reminders.
     */
    override suspend fun getReminders(): List<ReminderDTO> {
        return if (!shouldReturnError) {
            reminderList
        } else {
            throw Exception("error")
        }
    }

    /**
     * @param reminderId the id of the reminder
     * @return the reminder object with the reminderId
     */
    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        return if (!shouldReturnError) {
            reminderList.first { it.id == reminderId }
        } else {
            throw Exception("error")
        }
    }

    /**
     * Insert a reminder in the database. If the reminder already exists, replace it.
     *
     * @param reminder the reminder to be inserted.
     */
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    /**
     * Delete all reminders.
     */
    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }

    /**
     * Delete reminders by id.
     */
    override suspend fun deleteReminders(id: List<String>) {
        reminderList.removeIf { reminder ->
            id.contains(reminder.id)
        }
    }

    /**
     * Get last requestCode.
     */
    override suspend fun getLastRequestCode(): Int? {
        return if (!shouldReturnError) {
            reminderList.lastOrNull()?.requestCode ?: 0
        } else {
            throw Exception("error")
        }
    }
}