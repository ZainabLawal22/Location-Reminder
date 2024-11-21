package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.BaseTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.FakeDao
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.util.ReminderUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : BaseTest {

    private val repository: ReminderDataSource by inject()

    private val dao: RemindersDao by inject(named("fakeDao"))

    @Test
    fun noData_DisplayedInUi() = runBlockingTest {
        launchFragmentInContainer<ReminderListFragment>(bundleOf(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun list_DisplayedInUi() {
        runBlocking {
            val reminder = ReminderUtils.MOCK_REMINDER_DTO
            repository.saveReminder(reminder)

            launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

            onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
            onView(withId(R.id.reminderssRecyclerView)).check(
                matches(
                    hasDescendant(
                        withText(
                            reminder.title
                        )
                    )
                )
            )
        }
    }

    @Test
    fun clickAddReminder_navigateToSaveReminder() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun listError_showSnackBar() {
        runBlocking {
            val fakeDao = dao as FakeDao
            fakeDao.shouldReturnError = true

            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

            sleep(1000)
            onView(withText("error")).check(matches(isDisplayed()))
        }
    }
}