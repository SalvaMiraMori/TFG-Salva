package com.example.tfg_salva.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

data class UserData (var userID: String, var subscribedGames: ArrayList<String>)