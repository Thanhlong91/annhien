package com.annhien.womenshealth.core.online.food

import com.annhien.womenshealth.core.network.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpenFoodFactsModule {

    private const val BASE_URL = "https://world.openfoodfacts.org/"

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(networkModule: NetworkModule): OpenFoodFactsApi {
        val retrofit = networkModule.provideRetrofit(
            baseUrl = BASE_URL,
            client = networkModule.provideOkHttpClient()
        )
        return retrofit.create(OpenFoodFactsApi::class.java)
    }
}
