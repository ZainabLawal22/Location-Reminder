package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.BaseTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.ReminderUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.koin.core.qualifier.named

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : BaseTest {

    private val remindersRepository: ReminderDataSource by inject()
    private val dao: RemindersDao by inject(named("fakeDao"))

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun saveReminderAndGet_success() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersRepository.saveReminder(reminder)

        val loaded = remindersRepository.getReminder(reminder.id)
        assertThat(loaded is Result.Success, `is`(true))

        val data = (loaded as Result.Success).data

        assertThat(data.id, `is`(reminder.id))
        assertThat(data.title, `is`(reminder.title))
        assertThat(data.description, `is`(reminder.description))
        assertThat(data.location, `is`(reminder.location))
        assertThat(data.latitude, `is`(reminder.latitude))
        assertThat(data.longitude, `is`(reminder.longitude))
        assertThat(data.requestCode, `is`(reminder.requestCode))
    }

    @Test
    fun saveReminderAndGet_notFound() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersRepository.saveReminder(reminder)

        val loaded = remindersRepository.getReminder("")
        assertThat(loaded is Result.Error, `is`(true))
    }

    @Test
    fun saveReminderAndGet_error() = runBlocking {
        val fakeDao = dao as FakeDao
        fakeDao.shouldReturnError = true
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersRepository.saveReminder(reminder)

        val loaded = remindersRepository.getReminder("")
        assertThat(loaded is Result.Error, `is`(true))
    }

    @Test
    fun saveReminderAndGetReminders_success() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersRepository.saveReminder(reminder)

        val loaded = remindersRepository.getReminders()

        assertThat(loaded is Result.Success, `is`(true))

        val data = (loaded as Result.Success).data
        assertThat(data.isEmpty(), not(true))
    }

    @Test
    fun saveReminderAndGetReminders_failed() = runBlocking {
        val fakeDao = dao as FakeDao
        fakeDao.shouldReturnError = true
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersRepository.saveReminder(reminder)

        val loaded = remindersRepository.getReminders()

        assertThat(loaded is Result.Error, `is`(true))
    }

    @Test
    fun getRemindersWithoutData() = runBlocking {
        val loaded = remindersRepository.getReminders()

        assertThat(loaded is Result.Success, `is`(true))

        val data = (loaded as Result.Success).data

        assertThat<List<ReminderDTO>>(data, CoreMatchers.notNullValue())
        assertThat(data.isEmpty(), `is`(true))
    }

    @Test
    fun deleteAllReminders() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersRepository.saveReminder(reminder)

        var loaded = remindersRepository.getReminders()

        var data = (loaded as Result.Success).data

        assertThat<List<ReminderDTO>>(data, CoreMatchers.notNullValue())
        assertThat(data.isEmpty(), not(true))

        remindersRepository.deleteAllReminders()
        loaded = remindersRepository.getReminders()
        data = (loaded as Result.Success).data
        assertThat<List<ReminderDTO>>(data, CoreMatchers.notNullValue())
        assertThat(data.isEmpty(), `is`(true))
    }

    @Test
    fun deleteReminders() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersRepository.saveReminder(reminder)

        var loaded = remindersRepository.getReminder(reminder.id)
        val data = (loaded as Result.Success).data

        assertThat(data, CoreMatchers.notNullValue())

        remindersRepository.deleteReminders(reminder.id)
        loaded = remindersRepository.getReminder(reminder.id)
        assertThat(loaded is Result.Error, `is`(true))
    }

    @Test
    fun getLastRequestCode_withData() = runBlocking {
        val reminder = ReminderUtils.MOCK_REMINDER_DTO
        remindersRepository.saveReminder(reminder)

        val loaded = remindersRepository.getReminders()
        val data = (loaded as Result.Success).data.last()
        val code = (remindersRepository.getLastRequestCode() as Result.Success).data
        assertThat(code, `is`(data.requestCode))
    }

    @Test
    fun getLastRequestCode_noData() = runBlocking {
        val code = (remindersRepository.getLastRequestCode() as Result.Success).data
        assertThat(code, `is`(0))
    }

    @Test
    fun getLastRequestCode_failed() = runBlocking {
        val fakeDao = dao as FakeDao
        fakeDao.shouldReturnError = true

        val code = remindersRepository.getLastRequestCode()
        assertThat(code is Result.Error, `is`(true))
    }
}



