package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.BaseTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.ReminderUtils

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.qualifier.named
import org.koin.test.inject


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest : BaseTest {

    private val remindersDao: RemindersDao by inject(named("dao"))

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun saveReminderAndGetById() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        val loaded = remindersDao.getReminderById(reminder.id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.deleteFlag, `is`(reminder.deleteFlag))
        assertThat(loaded.requestCode, `is`(reminder.requestCode))
    }

    @Test
    fun saveReminderAndGetReminders() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        val loaded = remindersDao.getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.isEmpty(), not(true))
    }

    @Test
    fun getRemindersWithoutData() = runBlocking {
        val loaded = remindersDao.getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.isEmpty(), `is`(true))
    }

    @Test
    fun deleteAllReminders() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        var loaded = remindersDao.getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.isEmpty(), not(true))

        remindersDao.deleteAllReminders()
        loaded = remindersDao.getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.isEmpty(), `is`(true))
    }

    @Test
    fun deleteReminders() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        var loaded = remindersDao.getReminderById(reminder.id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())

        remindersDao.deleteReminders(listOf(reminder.id))
        loaded = remindersDao.getReminderById(reminder.id)
        assertThat(loaded, nullValue())
    }

    @Test
    fun getLastRequestCode_withData() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersDao.saveReminder(reminder)

        val data = remindersDao.getReminders().last()
        val code = remindersDao.getLastRequestCode()
        assertThat(code, `is`(data.requestCode))
    }

    @Test
    fun getLastRequestCode_noData() = runBlocking {
        val code = remindersDao.getLastRequestCode()
        assertThat(code, `is`(nullValue()))
    }
}