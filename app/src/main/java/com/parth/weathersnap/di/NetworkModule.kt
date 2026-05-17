package com.parth.weathersnap.di
import com.parth.weathersnap.data.remote.GeocodingApiService
import com.parth.weathersnap.data.remote.WeatherApiService
import com.parth.weathersnap.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * NetworkModule - Hilt module providing networking dependencies.
 *
 * We need TWO Retrofit instances because Open-Meteo uses different base URLs:
 *   1. Weather API: https://api.open-meteo.com/
 *   2. Geocoding API: https://geocoding-api.open-meteo.com/
 *
 * @Named qualifiers distinguish between the two Retrofit instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides the OkHttp logging interceptor.
     * BODY level logs full request/response for debugging during development.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    /** Provides the shared OkHttpClient with logging and timeout configuration */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** Retrofit instance for the Weather API (api.open-meteo.com) */
    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.WEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Retrofit instance for the Geocoding API (geocoding-api.open-meteo.com) */
    @Provides
    @Singleton
    @Named("geocoding")
    fun provideGeocodingRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.GEOCODING_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Provides the WeatherApiService from the weather Retrofit instance */
    @Provides
    @Singleton
    fun provideWeatherApiService(
        @Named("weather") retrofit: Retrofit
    ): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    /** Provides the GeocodingApiService from the geocoding Retrofit instance */
    @Provides
    @Singleton
    fun provideGeocodingApiService(
        @Named("geocoding") retrofit: Retrofit
    ): GeocodingApiService {
        return retrofit.create(GeocodingApiService::class.java)
    }
}
