package com.example.tfg_salva.localDatabase.DAOs

import com.example.tfg_salva.localDatabase.UserDataEntity
import androidx.room.*

@Dao
interface UserDataDao {
    @Query("SELECT * FROM userDataEntity")
    fun getAll(): List<UserDataEntity>

/*    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: UserDataEntity)

    @Delete
    fun delete(vararg users: UserDataEntity)
}