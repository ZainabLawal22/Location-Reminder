package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.BaseTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.Is
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.koin.core.component.inject

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O])
class RemindersListViewModelTest : BaseTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val remindersViewModel: RemindersListViewModel by inject()

    private val dataSource: ReminderDataSource by inject()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun loadReminders_loading() {
        runBlocking {
            // mainCoroutineRule.pauseDispatcher()
            mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

            remindersViewModel.loadReminders()
            assertThat(remindersViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))
            // mainCoroutineRule.resumeDispatcher()

            mainCoroutineRule.dispatcher.scheduler.runCurrent()
            assertThat(remindersViewModel.showLoading.getOrAwaitValue(), Is.`is`(false))
        }
    }

    @Test
    fun loadReminders_successList() {
        assertThat(remindersViewModel.remindersList.value, Is.`is`(nullValue()))
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.remindersList.value, Is.`is`(notNullValue()))
    }

    @Test
    fun loadReminders_successValues() {
        runBlocking {
            dataSource.saveReminder(FakeDataSource.MOCK_REMINDER_DTO)
        }
        assertThat(remindersViewModel.remindersList.value, Is.`is`(nullValue()))
        remindersViewModel.loadReminders()
        val reminders = listOf(FakeDataSource.MOCK_REMINDER_DTO)
        val dataList = ArrayList<ReminderDataItem>()
        dataList.addAll(reminders.map { reminder ->
            ReminderDataItem(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude,
                reminder.id,
                reminder.deleteFlag,
                reminder.requestCode
            )
        })
        assertEquals(remindersViewModel.remindersList.value, dataList)
    }

    @Test
    fun loadReminders_error() {
        (dataSource as? FakeDataSource)?.shouldReturnError = true

        assertThat(remindersViewModel.showSnackBar.value, Is.`is`(nullValue()))
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.showSnackBar.value, not(Is.`is`(nullValue())))
    }

    @Test
    fun invalidateShowNoData_noData() {
        (dataSource as? FakeDataSource)?.shouldReturnError = true
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.showNoData.value, Is.`is`(true))
    }

    @Test
    fun invalidateShowNoData_hasData() {
        runBlocking {
            dataSource.saveReminder(FakeDataSource.MOCK_REMINDER_DTO)
        }
        remindersViewModel.loadReminders()
        assertThat(remindersViewModel.showNoData.value, Is.`is`(false))
    }
}

