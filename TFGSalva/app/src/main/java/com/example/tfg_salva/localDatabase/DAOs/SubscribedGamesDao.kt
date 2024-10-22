package com.example.tfg_salva.localDatabase.DAOs

import androidx.room.*
import com.example.tfg_salva.localDatabase.SubscribedGamesEntity

@Dao
interface SubscribedGamesDao {

    @Query("SELECT * FROM subscribedGamesEntity")
    fun getAll(): List<SubscribedGamesEntity>

/*    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg subscribedGames: SubscribedGamesEntity)

    @Delete
    fun delete(vararg subscribedGame: SubscribedGamesEntity)
}