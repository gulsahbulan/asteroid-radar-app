package com.gbulan.asteroidradar.network

import com.gbulan.asteroidradar.BuildConfig
import com.gbulan.asteroidradar.util.Constants.BASE_URL
import com.gbulan.asteroidradar.domain.PictureOfDay

import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface NasaApiService {

    @Retention(AnnotationRetention.RUNTIME)
    annotation class Scalar

    @Retention(AnnotationRetention.RUNTIME)
    annotation class Json

    @GET("neo/rest/v1/feed/")
    @Scalar
    suspend fun getAsteroids(
        @Query("api_key") apiKey: String = BuildConfig.API_KEY
    ): String

    @GET("planetary/apod")
    @Json
    suspend fun getPictureOfDay(
        @Query("api_key") apiKey: String = BuildConfig.API_KEY
    ): PictureOfDay
}

object NasaApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarOrJsonConverterFactory.create())
        .build()

    val retrofitService: NasaApiService by lazy { retrofit.create(NasaApiService::class.java) }
}

