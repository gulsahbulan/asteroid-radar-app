package com.gbulan.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.gbulan.asteroidradar.domain.Asteroid
import com.gbulan.asteroidradar.domain.PictureOfDay
import com.gbulan.asteroidradar.network.NasaApiService
import com.gbulan.asteroidradar.network.parseAsteroidsJsonResult
import com.gbulan.asteroidradar.database.AsteroidDao
import com.gbulan.asteroidradar.database.asDomainModel
import com.gbulan.asteroidradar.network.asDatabaseModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class Period { WEEK, TODAY, ALL }

class AsteroidRepository(private val apiService: NasaApiService,
                         private val asteroidDao: AsteroidDao,
                         private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {

    suspend fun getPictureOfDay(): PictureOfDay {
        return withContext(ioDispatcher) {
            apiService.getPictureOfDay()
        }
    }

    suspend fun refreshAsteroids() {
        withContext(ioDispatcher) {
            try {
                val asteroids = parseAsteroidsJsonResult(JSONObject(apiService.getAsteroids()))
                asteroidDao.insertAsteroids(asteroids.asDatabaseModel())
            } catch (e: Throwable) {
                throw GetAsteroidsError("Unable to refresh Asteroids data", e)
            }
        }
    }

    suspend fun clearOutdatedAsteroid() = withContext(ioDispatcher) {
        asteroidDao.clearOutdatedAsteroid()
    }

    fun getAsteroids(period: Period): LiveData<List<Asteroid>> {
        val result = when (period) {
            Period.ALL -> asteroidDao.getAsteroids()
            Period.WEEK -> asteroidDao.getWeekAsteroids()
            Period.TODAY -> asteroidDao.getTodayAsteroids()
        }
        return Transformations.map(result) {
            it.asDomainModel() }
    }
}

class GetAsteroidsError(message: String, cause: Throwable) : Throwable(message, cause)