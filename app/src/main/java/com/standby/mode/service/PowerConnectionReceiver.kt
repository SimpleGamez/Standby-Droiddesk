package com.standby.mode.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.standby.mode.data.repository.ProfileRepository
import com.standby.mode.data.repository.SettingsRepository
import com.standby.mode.ui.standby.StandbyActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PowerConnectionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        val activeProfile = profileRepository.getActiveProfile().first()
                        val triggerConfig = activeProfile?.triggerConfig
                        if (triggerConfig?.manualOnly != true && triggerConfig?.onChargerConnected == true) {
                            val launchIntent = Intent(context, StandbyActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(launchIntent)
                        }
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        val settings = settingsRepository.settingsFlow.first()
                        if (settings.exitOnUnplug) {
                            StandbyForegroundService.stop(context)
                            // Broadcast exit event to StandbyActivity
                            val exitIntent = Intent("com.standby.mode.ACTION_EXIT_STANDBY").apply {
                                setPackage(context.packageName)
                            }
                            context.sendBroadcast(exitIntent)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

