package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt
@get:Rule
var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun createNewDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb()
    {
        database.close()
    }

    private fun createNewReminder():ReminderDTO{
        return ReminderDTO(title = "Tuesday meeting",
            description = "meeting old friends",
            location = "Mall",
            latitude = 17.16,
            longitude = 17.22,
            id = "id1")

    }

    private fun checkReminder(firstReminder:ReminderDTO, secondReminder:ReminderDTO) :Boolean
    {
        return firstReminder==secondReminder
    }

    @Test
    fun getSameOutput() = runBlockingTest {

        val testReminderItem = createNewReminder()

        database.reminderDao().saveReminder(testReminderItem)

        val result =
            database.reminderDao().getReminderById("id1")
                ?.let { checkReminder(testReminderItem, it) }
        assertThat(result,`is`(true))
    }
}