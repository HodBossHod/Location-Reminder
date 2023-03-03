package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
@get:Rule
var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    private val NUM_OF_REMINDERS =10


    @Before
    fun initializeDB() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersDAO = database.reminderDao()
        repository =
            RemindersLocalRepository(
                remindersDAO
            )
    }

    @After
    fun closeDb()
    {
        database.close()
    }

    private fun createMultipleReminders():MutableList<ReminderDTO>{
        val list : MutableList<ReminderDTO> = ArrayList()
        for(i in 0..NUM_OF_REMINDERS){
            list.add(
                ReminderDTO(title = "Test${i}",
                    description = "Description of Reminder % ${i}",
                    location = "Location # ${i}",
                    latitude = i.toDouble(),
                    longitude = i.toDouble(),
                    id = "id${i}")
            )
        }
        return list

    }

    private fun checkReminder(firstReminder:ReminderDTO, secondReminder:ReminderDTO) :Boolean
    {
        return firstReminder==secondReminder
    }

    @Test
    fun checkAllAfterInserting() = runBlocking {

        val inputRemindersList = createMultipleReminders()

        for (reminderDTO in inputRemindersList) {
            database.reminderDao().saveReminder(reminderDTO)
        }

        val retrievedRemindersList = remindersDAO.getReminders()

        for (i in 0 until NUM_OF_REMINDERS)
        {
            val result = checkReminder(retrievedRemindersList[i],inputRemindersList[i])
            assertThat(result,`is`(true))
        }

    }

    @Test
    fun invalidID() = runBlocking {
        val testReminder = repository.getReminder("ID is not found")
        val error =  (testReminder) as Result.Error
        assertThat(error.message, `is`("Reminder has not been found!"))
    }
}