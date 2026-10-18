package com.annhien.womenshealth.core.online.environment

import com.annhien.womenshealth.BuildConfig
import com.annhien.womenshealth.core.network.NetworkModule
import com.annhien.womenshealth.core.preferences.OnlineUtilityPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpenAqModule {

    private const val BASE_URL = "https://api.openaq.org/"

    @Provides
    @Singleton
    fun provideOpenAqApi(
        networkModule: NetworkModule,
        preferences: OnlineUtilityPreferences
    ): OpenAqApi {
        val client = networkModule.provideOkHttpClient().newBuilder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val customKey = runBlocking { preferences.getOpenAqApiKey().firstOrNull() } ?: ""
                val apiKey = customKey.ifBlank { BuildConfig.OPENAQ_API_KEY }
                if (apiKey.isNotBlank()) {
                    requestBuilder.header("X-API-Key", apiKey)
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        val retrofit = networkModule.provideRetrofit(
            baseUrl = BASE_URL,
            client = client
        )
        return retrofit.create(OpenAqApi::class.java)
    }

    private const val WAQI_BASE_URL = "https://api.waqi.info/"

    @Provides
    @Singleton
    fun provideWaqiApi(
        networkModule: NetworkModule
    ): WaqiApi {
        val retrofit = networkModule.provideRetrofit(
            baseUrl = WAQI_BASE_URL,
            client = networkModule.provideOkHttpClient()
        )
        return retrofit.create(WaqiApi::class.java)
    }
}
