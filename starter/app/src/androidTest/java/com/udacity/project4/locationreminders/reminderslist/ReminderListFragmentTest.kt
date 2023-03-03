package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.test.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest :AutoCloseKoinTest(){
    private lateinit var remindersRepository: ReminderDataSource
    private lateinit var applicationContext: Application

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        stopKoin()
        applicationContext = getApplicationContext()
        val testModule = module {
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get())as ReminderDataSource}
            single { LocalDB.createRemindersDao(applicationContext) }
        }

        startKoin {
            androidContext(applicationContext)
            modules(listOf(testModule))
        }

        remindersRepository = get()

        runBlocking {
            remindersRepository.deleteAllReminders()
        }
    }

    private fun createNewReminder(): ReminderDTO {
        return ReminderDTO(title = "Tuesday meeting",
            description = "meeting old friends",
            location = "Mall",
            latitude = 17.16,
            longitude = 17.22,
            id = "id1")

    }

    @Test
    fun clickToNavigate()  {
        val testCase = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)
        testCase.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB))
            .perform(click())
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun addedReminderToRV() = runBlockingTest {
        runBlocking {

            val testReminder = createNewReminder()
            remindersRepository.saveReminder(testReminder)


            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

            onView(withText(testReminder.title)).check(matches(isDisplayed()))
            onView(withText(testReminder.description)).check(matches(isDisplayed()))
            onView(withText(testReminder.location)).check(matches(isDisplayed()))
        }
    }

    private fun checkReminder(firstReminder: ReminderDTO, secondReminder: ReminderDTO) :Boolean
    {
        return firstReminder==secondReminder
    }
}