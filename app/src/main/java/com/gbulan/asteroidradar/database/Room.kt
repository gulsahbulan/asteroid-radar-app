package com.gbulan.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.gbulan.asteroidradar.network.getFormattedDate

@Dao
interface AsteroidDao {
    @Query("SELECT * FROM asteroids_table ORDER BY closeApproachDate ASC")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * FROM asteroids_table WHERE closeApproachDate >= :currentDate ORDER BY closeApproachDate")
    fun getWeekAsteroids(
        currentDate: String = getFormattedDate()
    ): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * FROM asteroids_table WHERE closeApproachDate == :currentDate ORDER BY closeApproachDate")
    fun getTodayAsteroids(
        currentDate: String = getFormattedDate()
    ): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsteroids(asteroids: List<DatabaseAsteroid>)

    @Query("DELETE FROM asteroids_table WHERE closeApproachDate < :currentDate")
    suspend fun clearOutdatedAsteroid(
        currentDate: String = getFormattedDate()
    )
}

@Database(entities = [DatabaseAsteroid::class], version = 1, exportSchema = false)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    synchronized(AsteroidDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AsteroidDatabase::class.java,
                "asteroids"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}