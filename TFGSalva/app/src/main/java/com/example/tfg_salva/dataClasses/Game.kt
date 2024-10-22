package com.example.tfg_salva.dataClasses

data class Game(val esportsGameId: String, val state: String, val gameNumber: Int, var notifications: ArrayList<Notification>): java.io.Serializable