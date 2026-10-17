package com.annhien.womenshealth.core.online.environment

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
        val retrofit = networkModule.provideRetrofit(
            baseUrl = BASE_URL,
            client = networkModule.provideOkHttpClient()
        )
        return retrofit.create(OpenAqApi::class.java)
    }
}
