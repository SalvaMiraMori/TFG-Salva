package com.example.tfg_salva.viewModel

import com.example.tfg_salva.localDatabase.UserDataEntity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tfg_salva.MyApplication
import com.example.tfg_salva.dataClasses.UserData
import com.example.tfg_salva.database.DatabaseAdapter
import com.example.tfg_salva.localDatabase.AppDatabase
import com.example.tfg_salva.localDatabase.LocalDatabaseRepo
import com.example.tfg_salva.localDatabase.SubscribedGamesEntity
import kotlin.concurrent.thread

object UsersViewModel: ViewModel(), DatabaseAdapter.UsersInterface {

    var TAG = "UsersViewModel"
    var databaseAdapter: DatabaseAdapter
    var userDataMLD: MutableLiveData<UserData>
    var appDatabase: AppDatabase

    init {
        databaseAdapter = DatabaseAdapter()
        userDataMLD = MutableLiveData()

        var myApplication = MyApplication
        appDatabase = AppDatabase.getDatabase(myApplication.getContext())
    }

    fun addUserIdToDB(id: String){
        databaseAdapter.addUserIdToDB(id)
    }

    override fun setUserData(userData: UserData) {
        userDataMLD.postValue(userData)
        LocalDatabaseRepo.insertUserData(userData)

    }

    fun addSubscribedGame(esportsMatchId: String){
        userDataMLD.value?.let { databaseAdapter.addSubscribedGame(esportsMatchId, it.userID) }
        userDataMLD.value?.let { LocalDatabaseRepo.insertSubscribedGame(esportsMatchId, it.userID) }
    }

    fun removeSubscribedGame(esportsMatchId: String){
        userDataMLD.value?.let { databaseAdapter.removeSubscribedGame(esportsMatchId, it.userID) }
        userDataMLD.value?.let { LocalDatabaseRepo.deleteSubscribedGame(esportsMatchId, it.userID) }
    }

}