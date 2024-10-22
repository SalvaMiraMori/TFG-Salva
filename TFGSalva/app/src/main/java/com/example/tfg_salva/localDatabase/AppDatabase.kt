package com.example.tfg_salva.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tfg_salva.dataClasses.Game
import com.example.tfg_salva.localDatabase.DAOs.*
import javax.inject.Singleton

@Singleton
@Database(entities = [
    UserDataEntity::class,
    SubscribedGamesEntity::class,
    TeamEntity::class,
    NotificationEntity::class,
    MatchEntity::class,
    GameEntity::class],
    version = 7)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDataDao(): UserDataDao
    abstract fun subscribedGamesDao(): SubscribedGamesDao
    abstract fun teamDao(): TeamDao
    abstract fun gameDao(): GameDao
    abstract fun matchDao(): MatchDao
    abstract fun notificationDao(): NotificationDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "item_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}