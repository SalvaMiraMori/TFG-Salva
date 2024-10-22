package com.example.tfg_salva.localDatabase.DAOs

import androidx.room.*
import com.example.tfg_salva.localDatabase.GameEntity
import com.example.tfg_salva.localDatabase.NotificationEntity

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notificationEntity")
    fun getAll(): List<NotificationEntity>

    @Query("SELECT * FROM notificationEntity WHERE esportsGameId = :esportsGameId")
    fun getGameNotifications(esportsGameId: String): List<NotificationEntity>

    @Query("DELETE FROM notificationEntity WHERE esportsGameId = :esportsGameId")
    fun deleteNotificationsFromGame(esportsGameId: String)

    @Query("DELETE FROM notificationEntity WHERE esportsMatchId = :esportsMatchId")
    fun deleteNotificationsFromMatch(esportsMatchId: String)

/*    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg notifications: NotificationEntity)

    @Delete
    fun delete(vararg notifications: NotificationEntity)
}