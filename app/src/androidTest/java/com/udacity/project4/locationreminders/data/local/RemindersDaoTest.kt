package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test



@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetReminderById() = runBlocking {
        // GIVEN - insert a reminder
        val reminderDTO = ReminderDTO(
            description = "Test 1 description",
            title = "Test 1 title",
            latitude = 30.0,
            location = "Test 1 location",
            longitude = 40.0
        )
        database.reminderDao().saveReminder(reminderDTO)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminderDTO.id)

        // THEN - The loaded data contains the expected values
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminderDTO.id))
        assertThat(loaded.title, `is`(reminderDTO.title))
        assertThat(loaded.description, `is`(reminderDTO.description))
        assertThat(loaded.location, `is`(reminderDTO.location))
        assertThat(loaded.latitude, `is`(reminderDTO.latitude))
        assertThat(loaded.longitude, `is`(reminderDTO.longitude))
    }

    @Test
    fun saveRemindersAndGetAllReminders() = runBlocking {

        // GIVEN - insert reminders
        val reminder1 = ReminderDTO(
            description = "Test 1 description",
            title = "Test 1 title",
            latitude = 30.0,
            location = "Test 1 location",
            longitude = 40.0
        )
        val reminder2 = ReminderDTO(
            description = "Test 2 description",
            title = "Test 2 title",
            latitude = 22.0,
            location = "Test 2 location",
            longitude = 33.0
        )
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Get the reminders from the database
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values
        assertThat(loaded, notNullValue())

        assertThat(loaded[0].id, `is`(reminder1.id))
        assertThat(loaded[0].title, `is`(reminder1.title))
        assertThat(loaded[0].description, `is`(reminder1.description))
        assertThat(loaded[0].location, `is`(reminder1.location))
        assertThat(loaded[0].latitude, `is`(reminder1.latitude))
        assertThat(loaded[0].longitude, `is`(reminder1.longitude))

        assertThat(loaded[1].id, `is`(reminder2.id))
        assertThat(loaded[1].title, `is`(reminder2.title))
        assertThat(loaded[1].description, `is`(reminder2.description))
        assertThat(loaded[1].location, `is`(reminder2.location))
        assertThat(loaded[1].latitude, `is`(reminder2.latitude))
        assertThat(loaded[1].longitude, `is`(reminder2.longitude))
    }

    @Test
    fun saveReminders_andDeleteAllReminders() = runBlocking {
        // GIVEN - insert reminders
        val reminder1 = ReminderDTO(
            description = "Test 1 description",
            title = "Test 1 title",
            latitude = 30.0,
            location = "Test 1 location",
            longitude = 40.0
        )
        val reminder2 = ReminderDTO(
            description = "Test 2 description",
            title = "Test 2 title",
            latitude = 22.0,
            location = "Test 2 location",
            longitude = 33.0
        )
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        //get reminders
        var loaded = database.reminderDao().getReminders()
        assertThat(loaded, notNullValue())

        //delete reminders
        database.reminderDao().deleteAllReminders()

        // WHEN - get all from database
        loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values
        assertThat(loaded, `is`(emptyList()))

    }

}