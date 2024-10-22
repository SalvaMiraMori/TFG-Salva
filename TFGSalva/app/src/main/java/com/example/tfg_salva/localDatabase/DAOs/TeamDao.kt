package com.example.tfg_salva.localDatabase.DAOs

import androidx.room.*
import com.example.tfg_salva.localDatabase.TeamEntity

@Dao
interface TeamDao {
    @Query("SELECT * FROM teamEntity")
    fun getAll(): List<TeamEntity>

    @Query("SELECT * FROM teamEntity WHERE esportsMatchId = :esportsMatchId")
    fun getTeamsFromMatch(esportsMatchId: String): List<TeamEntity>

    @Query("DELETE FROM teamEntity WHERE esportsMatchId = :esportsMatchId")
    fun deleteTeamsOfMatch(esportsMatchId: String)

/*    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg teams: TeamEntity)

    @Delete
    fun delete(vararg teams: TeamEntity)
}