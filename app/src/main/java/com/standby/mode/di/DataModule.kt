package com.standby.mode.di

import android.content.Context
import androidx.room.Room
import com.standby.mode.data.local.ProfileDao
import com.standby.mode.data.local.SettingsDataStore
import com.standby.mode.data.local.StandbyDatabase
import com.standby.mode.data.remote.OpenMeteoApi
import com.standby.mode.data.repository.CalendarRepository
import com.standby.mode.data.repository.CalendarRepositoryImpl
import com.standby.mode.data.repository.ProfileRepository
import com.standby.mode.data.repository.ProfileRepositoryImpl
import com.standby.mode.data.repository.SettingsRepository
import com.standby.mode.data.repository.SettingsRepositoryImpl
import com.standby.mode.data.repository.WeatherRepository
import com.standby.mode.data.repository.WeatherRepositoryImpl
import com.standby.mode.domain.provider.WeatherProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StandbyDatabase {
        return Room.databaseBuilder(
            context,
            StandbyDatabase::class.java,
            StandbyDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideProfileDao(database: StandbyDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindWeatherProvider(impl: OpenMeteoApi): WeatherProvider

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository
}

