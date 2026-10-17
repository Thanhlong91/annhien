package com.annhien.womenshealth.core.online.health

import com.annhien.womenshealth.core.network.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MyHealthfinderModule {

    private const val BASE_URL = "https://health.gov/"

    @Provides
    @Singleton
    fun provideMyHealthfinderApi(networkModule: NetworkModule): MyHealthfinderApi {
        val retrofit = networkModule.provideRetrofit(
            baseUrl = BASE_URL,
            client = networkModule.provideOkHttpClient()
        )
        return retrofit.create(MyHealthfinderApi::class.java)
    }
}
