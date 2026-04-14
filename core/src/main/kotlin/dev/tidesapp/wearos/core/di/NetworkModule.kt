package dev.tidesapp.wearos.core.di

import dev.tidesapp.wearos.core.network.TidesHeaderInterceptor
import dev.tidesapp.wearos.core.network.TidesQueryParamsInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        queryParamsInterceptor: TidesQueryParamsInterceptor,
        headerInterceptor: TidesHeaderInterceptor,
    ): OkHttpClient {
        // Default OkHttp Dispatcher caps at maxRequestsPerHost=5, which
        // serializes playbackinfo resolution for queues larger than 5 tracks.
        // Bump to 20 so a typical playlist resolves as a single parallel batch
        // instead of 4 sequential batches.
        val dispatcher = Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 20
        }
        return OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .addInterceptor(queryParamsInterceptor)
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        json: Json,
        okHttpClient: OkHttpClient,
        @Named("apiBaseUrl") baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
