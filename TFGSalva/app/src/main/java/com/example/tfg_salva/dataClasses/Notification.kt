package com.example.tfg_salva.dataClasses

data class Notification (val id: String, val playingTime: String, val text: String, val blueSideKills: String,
                         val redSideKills: String, val blueSideGold: String, val redSideGold: String,
                         val blueSideTowers: String, val redSideTowers: String, val blueSideDragons: String,
                         val redSideDragons: String, val blueSideTeam: String, val redSideTeam: String)