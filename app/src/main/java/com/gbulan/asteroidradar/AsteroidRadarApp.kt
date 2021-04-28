package com.gbulan.asteroidradar

import android.app.Application
import androidx.work.*
import com.gbulan.asteroidradar.work.RefreshDataWorker
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AsteroidRadarApp:  Application() {

    private fun setupRecurringWork() {
        // initialize WorkManager with a Factory
        val workManagerConfiguration = Configuration.Builder()
            .setWorkerFactory(RefreshDataWorker.Factory())
            .build()
        WorkManager.initialize(this, workManagerConfiguration)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val work = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(RefreshDataWorker::class.java.name, KEEP, work)

    }

    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
        Timber.plant(Timber.DebugTree())
    }
}