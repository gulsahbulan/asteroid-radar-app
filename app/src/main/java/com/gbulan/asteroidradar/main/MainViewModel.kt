package com.gbulan.asteroidradar.main

import androidx.lifecycle.*
import com.gbulan.asteroidradar.domain.PictureOfDay
import com.gbulan.asteroidradar.network.NasaApiService
import com.gbulan.asteroidradar.database.AsteroidDao
import com.gbulan.asteroidradar.repository.AsteroidRepository
import com.gbulan.asteroidradar.repository.GetAsteroidsError
import com.gbulan.asteroidradar.repository.Period
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(private val asteroidRepository: AsteroidRepository) : ViewModel() {
    private val periodOfAsteroidsData = MutableLiveData<Period>(Period.WEEK)

    val asteroids = Transformations.switchMap(periodOfAsteroidsData) {
        asteroidRepository.getAsteroids(it)
    }

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _snackBar = MutableLiveData<String?>()
    val snackBar: LiveData<String?>
        get() = _snackBar

    init {
        getPictureOfDay()
        refreshAsteroids()
    }

    fun onSnackBarShown() {
        _snackBar.value = null
        getPictureOfDay()
        refreshAsteroids()
    }

    private fun refreshAsteroids() = launchDataLoad {
        asteroidRepository.refreshAsteroids()
    }

    private fun launchDataLoad(block: suspend () -> Unit): Unit {
        viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: GetAsteroidsError) {
                Timber.e(error.cause, "Unable to refresh Nasa Asteroids data.")
                _snackBar.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }

    private fun getPictureOfDay() {
        viewModelScope.launch {
            try {
                _pictureOfDay.value = asteroidRepository.getPictureOfDay()
            } catch (e: Exception) {
                Timber.e("Failed to get PictureOfDay data. %s", e.message)
            }
        }
    }

    fun showWeekAsteroids() {
        periodOfAsteroidsData.value = Period.WEEK
    }

    fun showTodayAsteroids() {
        periodOfAsteroidsData.value = Period.TODAY
    }

    fun showSavedAsteroids() {
        periodOfAsteroidsData.value = Period.ALL
    }

    class Factory(private val apiService: NasaApiService, private val database: AsteroidDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(AsteroidRepository(apiService, database)) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}