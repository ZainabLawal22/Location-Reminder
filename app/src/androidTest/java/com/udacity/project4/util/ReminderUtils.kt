package com.udacity.project4.util

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

class ReminderUtils {
    companion object {
        val MOCK_REMINDER_DTO =
            ReminderDTO("title", "description", "location", 0.0, 0.0, "uuid",0, 10)
        val MOCK_REMINDER =
            ReminderDataItem("title", "description", "location", 0.0, 0.0, "uuid", 0,10)
        val EMPTY_REMINDER =
            ReminderDataItem("", "", "", 0.0, 0.0, "", 0,0)
    }
}