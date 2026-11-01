package com.standby.mode.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.standby.mode.ui.editor.LayoutEditorScreen
import com.standby.mode.ui.onboarding.OnboardingScreen
import com.standby.mode.ui.theme.StandByTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            StandByTheme {
                MainAppNavigation()
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "settings") {
        composable("settings") {
            SettingsScreen(
                onNavigateToEditor = { navController.navigate("editor") },
                onNavigateToOem = { navController.navigate("oem") }
            )
        }
        composable("editor") {
            LayoutEditorScreen(
                onNavigateUp = { navController.popBackStack() }
            )
        }
        composable("oem") {
            OemGuidanceScreen(
                onNavigateUp = { navController.popBackStack() }
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onFinished = {
                    navController.navigate("settings") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
    }
}

