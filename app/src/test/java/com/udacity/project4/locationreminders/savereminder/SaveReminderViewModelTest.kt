package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.toReminderDTO

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var reminderDataItem: ReminderDataItem

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        val fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun validateEnteredData_enteredDataIsCorrect() {
        reminderDataItem = ReminderDataItem(
            description = "Test description",
            title = "Test title",
            latitude = 20.0,
            location = "Test location",
            longitude = 30.0
        )
        val value = saveReminderViewModel.validateEnteredData(reminderDataItem)

        MatcherAssert.assertThat(value, Is.`is`(true))
    }

    @Test
    fun validateEnteredData_enteredDataIsNotCorrect() {
        reminderDataItem = ReminderDataItem(
            description = "Test description",
            title = "",
            latitude = 20.0,
            location = "Test location",
            longitude = 30.0
        )
        val value = saveReminderViewModel.validateEnteredData(reminderDataItem)

        MatcherAssert.assertThat(value, Is.`is`(false))
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.value,
            Is.`is`(R.string.err_enter_title)
        )
    }

    @Test
    fun onClear_clearReminderData() {
        saveReminderViewModel.latitude.value = 20.0
        val value = saveReminderViewModel.latitude.value
        MatcherAssert.assertThat(value, Is.`is`(20.0))

        saveReminderViewModel.onClear()
        val latitude = saveReminderViewModel.latitude.value

        MatcherAssert.assertThat(latitude, Is.`is`(Matchers.nullValue()))
    }

    @Test
    fun saveReminder_checkToastMessage() = runTest {
        reminderDataItem = ReminderDataItem(
            description = "Test description",
            title = "Test title",
            latitude = 20.0,
            location = "Test location",
            longitude = 30.0
        )
        saveReminderViewModel.saveReminder(reminderDataItem)

        val value = saveReminderViewModel.showToast.getOrAwaitValue()

        MatcherAssert.assertThat(value, Is.`is`("Reminder Saved !"))
    }

    @Test
    fun checkShowLoading() = runTest {
        reminderDataItem = ReminderDataItem(
            description = "Test description",
            title = "Test title",
            latitude = 20.0,
            location = "Test location",
            longitude = 30.0
        )

        // Pause the dispatcher (to delay coroutine execution)
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        // Trigger the save reminder action
        saveReminderViewModel.saveReminder(reminderDataItem)

        // Assert that loading is shown
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            Is.`is`(true)
        )

        // Run pending coroutines and resume dispatcher
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        // Assert that loading is no longer shown
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            Is.`is`(false)
        )
    }


}
