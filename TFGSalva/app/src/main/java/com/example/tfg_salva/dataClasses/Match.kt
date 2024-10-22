package com.example.tfg_salva.dataClasses

data class Match(val playingStartingTime: String,
                 val status: String, val esportsMatchId: String, var gamesList: ArrayList<Game>,
                 var teams: ArrayList<Team>, var numberOfGames: Int)