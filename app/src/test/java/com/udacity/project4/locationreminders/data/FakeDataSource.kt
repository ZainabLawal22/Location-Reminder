package com.udacity.project4.locationreminders.data

import android.os.Build
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

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
}