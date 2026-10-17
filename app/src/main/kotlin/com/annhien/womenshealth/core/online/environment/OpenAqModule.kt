package com.annhien.womenshealth.core.online.environment

import com.annhien.womenshealth.BuildConfig
import com.annhien.womenshealth.core.network.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpenAqModule {

    private const val BASE_URL = "https://api.openaq.org/"

    @Provides
    @Singleton
    fun provideOpenAqApi(networkModule: NetworkModule): OpenAqApi {
        val client = networkModule.provideOkHttpClient().newBuilder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val apiKey = BuildConfig.OPENAQ_API_KEY
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
}
