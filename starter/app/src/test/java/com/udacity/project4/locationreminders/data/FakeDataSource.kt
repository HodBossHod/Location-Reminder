package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private  var remindersData = mutableListOf<ReminderDTO>()
    private var returnError = false


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        try {
            if (returnError){
                return Result.Error("Database error")
            }
            return Result.Success(remindersData.toList())
        }catch (ex:Exception){
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersData.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            if(returnError){
                return  Result.Error("Error")
            }
            val ans= remindersData.find { it.id==id } ?: return Result.Error("Didn't find any reminder")
            return Result.Success(ans)
        }catch (ex:Exception){
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        remindersData.clear()
    }

    fun setError(boolean: Boolean){
        returnError=boolean
    }

    fun clearAllReminders(){
        remindersData.clear()
    }




}