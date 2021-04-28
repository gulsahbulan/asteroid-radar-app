package com.gbulan.asteroidradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.gbulan.asteroidradar.database.getDatabase
import com.gbulan.asteroidradar.network.NasaApi
import com.gbulan.asteroidradar.network.NasaApiService
import com.gbulan.asteroidradar.repository.AsteroidRepository
import retrofit2.HttpException

class RefreshDataWorker(appContext: Context, params: WorkerParameters, private val network: NasaApiService):
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = AsteroidRepository(network, database.asteroidDao)
        return try {
            repository.refreshAsteroids()
            repository.clearOutdatedAsteroid()
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }

    class Factory(private val network: NasaApiService = NasaApi.retrofitService) : WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker {
            return RefreshDataWorker(appContext, workerParameters, network)
        }

    }
}