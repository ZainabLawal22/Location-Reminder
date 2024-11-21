package com.udacity.project4

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.*
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.junit.After
import org.junit.Before
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest

interface BaseTest : KoinTest {

    @Before
    fun init() {
        stopKoin()
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    androidApplication(),
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    androidApplication(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get(named("fakeDao"))) as ReminderDataSource }
            single(named("dao")) {
                Room.inMemoryDatabaseBuilder(
                    androidContext(),
                    RemindersDatabase::class.java
                ).build().reminderDao()
            }
            single<RemindersDao>(named("fakeDao")) { FakeDao() }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(myModule))
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}