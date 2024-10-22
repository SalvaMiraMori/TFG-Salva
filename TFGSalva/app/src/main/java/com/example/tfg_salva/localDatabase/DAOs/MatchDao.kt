package com.example.tfg_salva.localDatabase.DAOs

import androidx.room.*
import com.example.tfg_salva.localDatabase.MatchEntity

@Dao
interface MatchDao {
    @Query("SELECT * FROM matchEntity")
    fun getAll(): List<MatchEntity>

    @Query("SELECT * FROM matchEntity WHERE status = 'Unstarted'")
    fun getScheduledMatches(): List<MatchEntity>

    @Query("SELECT * FROM matchEntity WHERE status = 'In progress'")
    fun getLiveMatches(): List<MatchEntity>

    @Query("SELECT * FROM matchEntity WHERE status = 'Completed'")
    fun getCompletedMatches(): List<MatchEntity>

    @Query("DELETE FROM matchEntity WHERE esportsMatchId = :esportsMatchId")
    fun deleteMatchById(esportsMatchId: String)

/*    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg matches: MatchEntity)

    @Delete
    fun delete(vararg matches: MatchEntity)
}