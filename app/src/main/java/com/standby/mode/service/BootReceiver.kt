package com.standby.mode.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.standby.mode.data.repository.ProfileRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var profileRepository: ProfileRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Ensure default profiles exist after boot
                    profileRepository.ensureDefaultProfilesExist()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

