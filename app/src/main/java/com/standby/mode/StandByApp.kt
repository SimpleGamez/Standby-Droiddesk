package com.standby.mode

import android.app.Application
import com.standby.mode.data.repository.ProfileRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class StandByApp : Application() {

    @Inject
    lateinit var profileRepository: ProfileRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            try {
                profileRepository.ensureDefaultProfilesExist()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
