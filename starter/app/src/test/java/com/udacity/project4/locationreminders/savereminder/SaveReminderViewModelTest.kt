package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.junit.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects
    private lateinit var remindersRepository: FakeDataSource

    // Subject under test
    private lateinit var remindersViewModel: SaveReminderViewModel

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        remindersViewModel = SaveReminderViewModel( ApplicationProvider.getApplicationContext(),remindersRepository)
    }
    @After
    fun tearDown() {
        stopKoin()
    }
    //TODO: provide testing to the SaveReminderView and its live data objects

    @Test
    fun testInput(){
        val firstReminder=  ReminderDataItem("Test1","Going home",null,null,null)
        val secondReminder=  ReminderDataItem("Test2","go to school","school",30.1756,31.0987)
        Assert.assertThat(remindersViewModel.validateEnteredData(firstReminder), CoreMatchers.`is`(false))
        Assert.assertThat(remindersViewModel.validateEnteredData(secondReminder), CoreMatchers.`is`(true))
    }

    @Test
    fun testSaving(){
        val rem=  ReminderDataItem("test","go to my old school","school",30.1756,31.0987)
        mainCoroutineRule.pauseDispatcher()
        remindersViewModel.saveReminder(rem)
        Assert.assertThat(remindersViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        Assert.assertThat(remindersViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
        Assert.assertThat(remindersViewModel.showToast.getOrAwaitValue(), CoreMatchers.`is`("Reminder saved successfully"))
        Assert.assertThat(remindersViewModel.navigationCommand.getOrAwaitValue(), CoreMatchers.`is`(
            NavigationCommand.Back))
    }

}