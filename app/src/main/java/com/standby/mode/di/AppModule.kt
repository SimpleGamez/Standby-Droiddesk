package com.standby.mode.di

import com.standby.mode.domain.provider.BatteryProvider
import com.standby.mode.domain.provider.MediaSessionProvider
import com.standby.mode.service.BatteryMonitor
import com.standby.mode.service.MediaSessionMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindBatteryProvider(impl: BatteryMonitor): BatteryProvider

    @Binds
    @Singleton
    abstract fun bindMediaSessionProvider(impl: MediaSessionMonitor): MediaSessionProvider
}

