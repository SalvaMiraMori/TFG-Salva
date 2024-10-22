package com.example.tfg_salva.localDatabase.DAOs

import androidx.room.*
import com.example.tfg_salva.localDatabase.GameEntity
import com.example.tfg_salva.localDatabase.UserDataEntity

@Dao
interface GameDao {
    @Query("SELECT * FROM gameEntity")
    fun getAll(): List<GameEntity>

    @Query("SELECT * FROM gameEntity WHERE esportsMatchId = :esportsMatchId")
    fun getGameFromMatch(esportsMatchId: String): List<GameEntity>

    @Query("DELETE FROM gameEntity WHERE esportsMatchId = :esportsMatchId")
    fun deleteGameFromMatch(esportsMatchId: String)

/*    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg games: GameEntity)

    @Delete
    fun delete(vararg games: GameEntity)
}