package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeReminderDataSource: FakeDataSource

    @Before
    fun setupReminderListViewModel() {
        fakeReminderDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeReminderDataSource
        )
    }

    @Test
    fun loadReminders_givenErrorResult_showsSnackBarErrorMessage() =
        runBlockingTest {
            val message = "Error message"
            fakeReminderDataSource.errorMessage = message

            remindersListViewModel.loadReminders()

            Assert.assertEquals(message, remindersListViewModel.showSnackBar.getOrAwaitValue())
        }

    @Test
    fun loadReminders_givenValidForm_showsReminderList() =
        runBlockingTest {
            val reminder1 = ReminderDataItem(
                "Title todo1",
                "Description todo1",
                "Location todo1",
                50.0,
                50.0)

            val reminder2 = ReminderDataItem(
                "Title todo2",
                "Description todo2",
                "Location todo2",
                50.0,
                50.0)

            fakeReminderDataSource.saveReminder(reminder1.toReminderDTO())
            fakeReminderDataSource.saveReminder(reminder2.toReminderDTO())

            remindersListViewModel.loadReminders()

            Assert.assertEquals(listOf(reminder1,reminder2),remindersListViewModel.remindersList.getOrAwaitValue())
        }

    @After
    fun tearDown() {
        stopKoin()
    }
}
