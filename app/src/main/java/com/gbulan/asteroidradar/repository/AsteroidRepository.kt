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
import org.json.JSONObject

class AsteroidRepository(private val apiService: NasaApiService, private val asteroidDao: AsteroidDao) {

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(asteroidDao.getAsteroids()) {
            it.asDomainModel()
        }

    suspend fun getPictureOfDay(): PictureOfDay {
        return apiService.getPictureOfDay()
    }

    suspend fun refreshAsteroids() {
        try {
            val asteroids = parseAsteroidsJsonResult(JSONObject(apiService.getAsteroids()))
            asteroidDao.insertAsteroids(asteroids.asDatabaseModel())
        } catch (e: Throwable) {
            throw GetAsteroidsError("Unable to refresh Asteroids data", e)
        }
    }
}

class GetAsteroidsError(message: String, cause: Throwable) : Throwable(message, cause)