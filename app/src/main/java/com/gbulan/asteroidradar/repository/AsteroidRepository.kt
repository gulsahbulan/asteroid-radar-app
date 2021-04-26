package com.gbulan.asteroidradar.repository

import com.gbulan.asteroidradar.model.Asteroid
import com.gbulan.asteroidradar.model.PictureOfDay
import com.gbulan.asteroidradar.api.NasaApiService
import com.gbulan.asteroidradar.api.parseAsteroidsJsonResult
import org.json.JSONObject

class AsteroidRepository(private val apiService: NasaApiService) {
    suspend fun getPictureOfDay(): PictureOfDay {
        return apiService.getPictureOfDay()
    }

    suspend fun getAsteroids(): List<Asteroid> {
        return try {
            parseAsteroidsJsonResult(JSONObject(apiService.getAsteroids()))
        } catch (e: Throwable) {
            throw GetAsteroidsError("Unable to retrieve Asteroids data", e)
        }
    }
}

class GetAsteroidsError(message: String, cause: Throwable) : Throwable(message, cause)