package com.gbulan.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.reflect.Type


class ScalarOrJsonConverterFactory : Converter.Factory() {
    private val scalar: Converter.Factory = ScalarsConverterFactory.create()
    private val json: Converter.Factory = MoshiConverterFactory.create(
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    )

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        for (annotation in annotations) {
            if (annotation.annotationClass == NasaApiService.Json::class) {
                return json.responseBodyConverter(type, annotations, retrofit)
            }
            if (annotation.annotationClass == NasaApiService.Scalar::class) {
                return scalar.responseBodyConverter(type, annotations, retrofit)
            }
        }
        return null
    }

    companion object {
        fun create() = ScalarOrJsonConverterFactory()
    }
}