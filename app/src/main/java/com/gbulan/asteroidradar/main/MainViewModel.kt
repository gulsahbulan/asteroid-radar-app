package com.gbulan.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbulan.asteroidradar.Asteroid
import com.gbulan.asteroidradar.api.NasaApi
import kotlinx.coroutines.launch

enum class NasaApiStatus { LOADING, ERROR, DONE }

class MainViewModel : ViewModel() {

    private val _status = MutableLiveData<NasaApiStatus>()

    val status: LiveData<NasaApiStatus>
        get() = _status

    private val _properties = MutableLiveData<List<Asteroid>>()

    val properties: LiveData<List<Asteroid>>
        get() = _properties

    init {
        getNasaAsteroids()
    }

    private fun getNasaAsteroids() {
        viewModelScope.launch {
            try {
                _status.value = NasaApiStatus.LOADING
                _properties.value = NasaApi.retrofitService.getProperties()
                _status.value = NasaApiStatus.DONE
            } catch (e: Exception) {
                _status.value = NasaApiStatus.ERROR
                _properties.value = ArrayList()
            }
        }
    }
}