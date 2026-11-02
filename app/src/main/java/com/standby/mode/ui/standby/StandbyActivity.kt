package com.standby.mode.ui.standby

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.standby.mode.service.StandbyForegroundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StandbyActivity : ComponentActivity() {

    private val viewModel: StandbyViewModel by viewModels()

    private val exitReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.standby.mode.ACTION_EXIT_STANDBY") {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        configureLockScreenDisplay()

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Observe brightness config
        lifecycleScope.launch {
            viewModel.activeProfile.collectLatest { profile ->
                profile?.brightnessConfig?.let { bConfig ->
                    viewModel.brightnessController.applyBrightness(window, bConfig)
                }
            }
        }

        val filter = IntentFilter("com.standby.mode.ACTION_EXIT_STANDBY")
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            exitReceiver,
            filter,
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )

        setContent {
            StandbyScreenHost(onExit = { finish() }, viewModel = viewModel)
        }

        hideSystemBars()
    }

    override fun onStart() {
        super.onStart()
        StandbyForegroundService.start(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun configureLockScreenDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        }

        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        try {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } catch (e: Exception) {
            // Ignore if decor view not ready
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(exitReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        StandbyForegroundService.stop(this)
    }
}

