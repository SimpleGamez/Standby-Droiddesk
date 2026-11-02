package com.standby.mode.service

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.standby.mode.data.repository.ProfileRepository
import com.standby.mode.data.repository.SettingsRepository
import com.standby.mode.domain.model.TriggerConfig
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
        Log.d("PowerConnectionReceiver", "Received action: $action")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        profileRepository.ensureDefaultProfilesExist()
                        val activeProfile = profileRepository.getActiveProfile().first()
                        val triggerConfig = activeProfile?.triggerConfig ?: TriggerConfig()

                        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                        val isLocked = keyguardManager?.isKeyguardLocked == true

                        val shouldTrigger = !triggerConfig.manualOnly && (
                            triggerConfig.onChargerConnected ||
                            (triggerConfig.onChargingAndLocked && isLocked) ||
                            triggerConfig.onChargingAndLandscape
                        )
                        Log.d("PowerConnectionReceiver", "isLocked: $isLocked, shouldTrigger: $shouldTrigger")

                        if (shouldTrigger) {
                            Log.d("PowerConnectionReceiver", "Starting StandbyForegroundService and StandbyActivity")
                            // 1. Start foreground service immediately; this posts high-importance full-screen intent
                            StandbyForegroundService.start(context)

                            // 2. Also try direct startActivity in case the device is already unlocked
                            try {
                                val launchIntent = Intent(context, StandbyActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                context.startActivity(launchIntent)
                            } catch (e: Exception) {
                                Log.w("PowerConnectionReceiver", "Direct startActivity caught: ${e.message}")
                            }
                        }
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        val settings = settingsRepository.settingsFlow.first()
                        Log.d("PowerConnectionReceiver", "Power disconnected. exitOnUnplug: ${settings.exitOnUnplug}")
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
            } catch (e: Exception) {
                Log.e("PowerConnectionReceiver", "Error processing power event", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

