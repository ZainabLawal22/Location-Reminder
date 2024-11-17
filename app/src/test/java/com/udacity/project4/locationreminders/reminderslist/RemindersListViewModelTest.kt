package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    private lateinit var reminderListViewModel: RemindersListViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Use the updated MainCoroutineRule
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        val fakeDataSource = FakeDataSource()
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        reminderListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun invalidateShowNoData_noDataAvailable() {
        reminderListViewModel.remindersList.value = null
        val value = reminderListViewModel.showNoData.value

        MatcherAssert.assertThat(value, Is.`is`(CoreMatchers.nullValue()))
    }

    @Test
    fun checkShowLoading() = runTest {
        // Pause the dispatcher
        mainCoroutineRule.dispatcher.scheduler.advanceUntilIdle()

        // Trigger loading
        reminderListViewModel.loadReminders()

        // Assert that loading is shown
        MatcherAssert.assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            Is.`is`(true)
        )

        // Process the queued tasks
        mainCoroutineRule.dispatcher.scheduler.runCurrent()

        // Assert that loading is hidden
        MatcherAssert.assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            Is.`is`(false)
        )
    }

}

