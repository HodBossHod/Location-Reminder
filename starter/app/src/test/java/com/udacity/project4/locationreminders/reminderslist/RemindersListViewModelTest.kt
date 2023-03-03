package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.junit.*
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var remindersRepository: FakeDataSource

    // Subject under test
    private lateinit var remindersViewModel: RemindersListViewModel

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        val firstReminder=  ReminderDTO("First Test","meeting a friend","Zoo park",30.1756,31.0987)
        val secondReminder=  ReminderDTO("Second Test","meeting a friend","Zoo park",30.1756,31.0987)
        val thirdReminder=  ReminderDTO("Third Test","meeting a friend","Zoo park",30.1756,31.0987)
        runBlockingTest {
            remindersRepository.saveReminder(firstReminder)
            remindersRepository.saveReminder(secondReminder)
            remindersRepository.saveReminder(thirdReminder)
        }
        remindersViewModel = RemindersListViewModel( ApplicationProvider.getApplicationContext(),remindersRepository)
    }
    @After
    fun tearDown() {
        stopKoin()
    }
    @Test
    fun loadDataTest(){
        mainCoroutineRule.pauseDispatcher()
        remindersViewModel.loadReminders()
        Assert.assertThat(remindersViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        Assert.assertThat(remindersViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
        Assert.assertThat(remindersViewModel.remindersList.getOrAwaitValue().isEmpty(), CoreMatchers.`is`(false))
    }

    @Test
    fun testEmptyData(){
        remindersRepository.clearAllReminders()
        remindersViewModel.loadReminders()
        Assert.assertThat(remindersViewModel.remindersList.getOrAwaitValue().isEmpty(), CoreMatchers.`is`(true))
    }
    @Test
    fun getError(){
        remindersRepository.setError(true)
        remindersViewModel.loadReminders()
        Assert.assertThat(remindersViewModel.showSnackBar.getOrAwaitValue(), CoreMatchers.`is`("Database error"))
        remindersRepository.setError(false)
    }

}