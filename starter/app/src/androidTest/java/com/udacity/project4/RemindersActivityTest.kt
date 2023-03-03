package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get



@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var applicationContext: Application
    private lateinit var reminderRepository: ReminderDataSource

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @Before
    fun initialize() {
        stopKoin()
        applicationContext = getApplicationContext()
        val testModule = module {
            viewModel {
                RemindersListViewModel(
                    applicationContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    applicationContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get())as ReminderDataSource}
            single { LocalDB.createRemindersDao(applicationContext) }
        }

        startKoin {
            modules(listOf(testModule))
        }

        reminderRepository = get()


        runBlocking {
            reminderRepository.deleteAllReminders()
        }
    }

    @After
    fun unregisterIdlingResource() = runBlocking {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        reminderRepository.deleteAllReminders()
    }

    @Test
    fun remindersScreen_clickOnFab_opensSaveReminderScreen() = runBlocking {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun reminder_Location_Activity_show_toast_message() {

        val activityCaseScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityCaseScenario)
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("Reminder title"))
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("Reminder Description"))
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.map)).perform(ViewActions.longClick())
        Espresso.onView(withId(R.id.save_location_button)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Reminder saved successfully")).inRoot(
            RootMatchers.withDecorView(not(Is.`is`(getActivity(activityCaseScenario)!!.window.decorView)))
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        activityCaseScenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    @Test
    fun snackbarMessage() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.replaceText("Description"))
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.save_location_button)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(com.google.android.material.R.id.snackbar_text)).check(ViewAssertions.matches(ViewMatchers.withText("Please enter the location title")))
        activityScenario.close()
    }
}


