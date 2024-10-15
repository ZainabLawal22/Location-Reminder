package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.DefaultAsserter.fail

//@ExperimentalCoroutinesApi
//@RunWith(AndroidJUnit4::class)
//@MediumTest
//class RemindersLocalRepositoryTest {
//
//    @get:Rule
//    var mainCoroutineRule = MainAndroidTestCoroutineRule()
//
//    @get:Rule
//    var instantExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var database: RemindersDatabase
//    private lateinit var remindersDAO: RemindersDao
//    private lateinit var repository: RemindersLocalRepository
//
//    @Before
//    fun setup() {
//        database = Room.inMemoryDatabaseBuilder(
//            ApplicationProvider.getApplicationContext(),
//            RemindersDatabase::class.java
//        )
//            .allowMainThreadQueries()
//            .build()
//        remindersDAO = database.reminderDao()
//        repository = RemindersLocalRepository(
//            remindersDAO,
//            Dispatchers.Main
//        )
//    }
//
//    @After
//    fun closeDB() {
//        database.close()
//    }
//
//    @Test
//    fun saveReminderAndGetByID() = runTest {
//        val reminder = ReminderDTO(
//            title = "Tennis Ball",
//            description = "Do not play many double faults",
//            location = "Wimbledon Court",
//            latitude = 95.1234,
//            longitude = 36783.1234
//        )
//        repository.saveReminder(reminder)
//
//        val result = repository.getReminder(reminder.id)
//
//        if (result is Result.Success) {
//            val loaded = result.data
//            assertThat(loaded, notNullValue())
//            assertThat(loaded.id, `is`(reminder.id))
//            assertThat(loaded.description, `is`(reminder.description))
//            assertThat(loaded.location, `is`(reminder.location))
//            assertThat(loaded.latitude, `is`(reminder.latitude))
//            assertThat(loaded.longitude, `is`(reminder.longitude))
//        } else {
//            fail("Expected Success but got Error")
//        }
//    }
//
//    @Test
//    fun deleteAllRemindersAndCheckEmpty() = runTest {
//        val reminder = ReminderDTO(
//            title = "Tennis Ball",
//            description = "Do not play many double faults",
//            location = "Wimbledon Court",
//            latitude = 95.1234,
//            longitude = 36783.1234
//        )
//        repository.saveReminder(reminder)
//        repository.deleteAllReminders()
//
//        val result = repository.getReminders()
//        if (result is Result.Success) {
//            val data = result.data
//            assertThat(data.isEmpty(), `is`(true))
//        } else {
//            fail("Expected Success but got Error")
//        }
//    }
//
//    @Test
//    fun noRemindersFoundGetReminderById() = runTest {
//        val result = repository.getReminder("3")
//        if (result is Result.Error) {
//            assertThat(result.message, notNullValue())
//            assertThat(result.message, `is`("Reminder not found!"))
//        } else {
//            fail("Expected Error but got Success")
//        }
//    }
//}



