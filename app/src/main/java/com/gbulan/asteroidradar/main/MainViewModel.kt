package com.gbulan.asteroidradar.main

import androidx.lifecycle.*
import com.gbulan.asteroidradar.model.Asteroid
import com.gbulan.asteroidradar.model.PictureOfDay
import com.gbulan.asteroidradar.api.NasaApiService
import com.gbulan.asteroidradar.repository.AsteroidRepository
import com.gbulan.asteroidradar.repository.GetAsteroidsError
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(private val asteroidRepository: AsteroidRepository) : ViewModel() {
    private val _asteroids = MutableLiveData<List<Asteroid>>()
    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids

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
        getAsteroids()
    }

    fun onSnackBarShown() {
        _snackBar.value = null
        getPictureOfDay()
        getAsteroids()
    }

    private fun getAsteroids() = launchDataLoad {
        _asteroids.value = asteroidRepository.getAsteroids()
    }

    private fun launchDataLoad(block: suspend () -> Unit): Unit {
        viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: GetAsteroidsError) {
                Timber.e(error.cause, "Failed to get Nasa Asteroids data.")
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

    class Factory(private val apiService: NasaApiService) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(AsteroidRepository(apiService)) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}