package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.BaseTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.Is.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
class SaveReminderViewModelTest : BaseTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val viewModel: SaveReminderViewModel by inject()

    private val dataSource: ReminderDataSource by inject()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun fetchRequestCode_successNoData() {
        assertThat(viewModel.requestCode, `is`(0))
        viewModel.fetchRequestCode()
        assertThat(viewModel.requestCode, `is`(1))
    }

    @Test
    fun fetchRequestCode_successHasData() {
        val fakeDataSource = dataSource as FakeDataSource
        assertThat(viewModel.requestCode, `is`(0))
        runBlocking {
            fakeDataSource.saveReminder(FakeDataSource.MOCK_REMINDER_DTO)
        }
        viewModel.fetchRequestCode()
        assertThat(
            viewModel.requestCode,
            `is`(FakeDataSource.MOCK_REMINDER_DTO.requestCode?.plus(1))
        )
    }

    @Test
    fun fetchRequestCode_error() {
        val fakeDataSource = dataSource as FakeDataSource
        fakeDataSource.shouldReturnError = true
        assertThat(viewModel.requestCode, `is`(0))
        viewModel.fetchRequestCode()
        assertThat(viewModel.requestCode, `is`(1))
    }

    @Test
    fun onClear_cleared() {
        viewModel.reminderTitle.value = ""
        viewModel.reminderDescription.value = ""
        viewModel.reminderSelectedLocationStr.value = ""
        viewModel.selectedPOI.value = PointOfInterest(LatLng(0.0, 0.0), "", "")
        viewModel.latLong.value = LatLng(0.0, 0.0)

        viewModel.onClear()

        assertThat(viewModel.reminderTitle.value, `is`(nullValue()))
        assertThat(viewModel.reminderDescription.value, `is`(nullValue()))
        assertThat(viewModel.reminderSelectedLocationStr.value, `is`(nullValue()))
        assertThat(viewModel.selectedPOI.value, `is`(nullValue()))
        assertThat(viewModel.latLong.value, `is`(nullValue()))
    }

    @Test
    fun validateAndSaveReminder_dataNotValid() {
        val data = ReminderDataItem("", "", "", 0.0, 0.0)
        var remindersBefore: List<ReminderDTO>? = null
        runBlocking {
            val reminders = dataSource.getReminders()
            if (reminders is Result.Success) {
                remindersBefore = reminders.data
            }
        }
        assertThat(remindersBefore?.isEmpty(), `is`(true))
        viewModel.validateAndSaveReminder(data)
        runBlocking {
            val reminders = dataSource.getReminders()
            if (reminders is Result.Success) {
                remindersBefore = reminders.data
            }
        }
        assertThat(remindersBefore?.isEmpty(), `is`(true))
    }

    @Test
    fun validateAndSaveReminder_validData() {
        val data = FakeDataSource.MOCK_REMINDER
        var remindersBefore: List<ReminderDTO>? = null
        runBlocking {
            val reminders = dataSource.getReminders()
            if (reminders is Result.Success) {
                remindersBefore = reminders.data
            }
        }
        assertThat(remindersBefore?.isEmpty(), `is`(true))
        viewModel.validateAndSaveReminder(data)
        runBlocking {
            val reminders = dataSource.getReminders()
            if (reminders is Result.Success) {
                remindersBefore = reminders.data
            }
        }
        assertThat(remindersBefore?.isEmpty(), `is`(false))
    }

    @Test
    fun saveReminder_success() {
        val data = FakeDataSource.MOCK_REMINDER
        // mainCoroutineRule.pauseDispatcher()
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()
        SaveReminderViewModel::class.java.declaredMethods.firstOrNull { it.name == "saveReminder" }
            ?.let { method ->
                method.isAccessible = true
                method.invoke(viewModel, data)
            }
        assertThat(viewModel.showLoading.value, `is`(true))
        assertThat(viewModel.showToast.value, `is`(nullValue()))
        assertThat(viewModel.navigationCommand.value, `is`(nullValue()))
        // mainCoroutineRule.resumeDispatcher()
        mainCoroutineRule.dispatcher.scheduler.runCurrent()
        assertThat(viewModel.showToast.value, `is`(notNullValue()))
        assertThat(viewModel.navigationCommand.value, `is`(notNullValue()))
        assertThat(viewModel.showLoading.value, `is`(false))

    }

    @Test
    fun validateEnteredData_success() {
        val data = FakeDataSource.MOCK_REMINDER
        val method =
            SaveReminderViewModel::class.java.declaredMethods.firstOrNull { it.name == "validateEnteredData" }
        method?.isAccessible = true
        val result: Boolean = method?.invoke(viewModel, data) as Boolean
        assertThat(result, `is`(true))
    }

    @Test
    fun validateEnteredData_failed() {
        val data = ReminderDataItem("", "", "", 0.0, 0.0)
        val method =
            SaveReminderViewModel::class.java.declaredMethods.firstOrNull { it.name == "validateEnteredData" }
        method?.isAccessible = true
        val result: Boolean = method?.invoke(viewModel, data) as Boolean
        assertThat(result, `is`(false))
    }
}
