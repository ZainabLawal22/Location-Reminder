package com.udacity.project4.locationreminders.data

import android.os.Build
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    companion object {
        val MOCK_REMINDER_DTO =
            ReminderDTO("title", "description", "location", 0.0, 0.0, "uuid", 0, 10)
        val MOCK_REMINDER =
            ReminderDataItem("title", "description", "location", 0.0, 0.0, "uuid", 0, 10)
    }

    private val reminders: MutableList<ReminderDTO>? = mutableListOf()
    var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.let { return Result.Success(it[id.toInt()]) }
        return Result.Error("Reminders not found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    override suspend fun deleteReminders(id: String) {
        reminders?.removeIf { reminder ->
            id.contains(reminder.id)
        }
    }

    override suspend fun getLastRequestCode(): Result<Int> {
        return if (!shouldReturnError) {
            Result.Success(reminders?.lastOrNull()?.requestCode ?: 0)
        } else {
            Result.Error("error")
        }
    }

}