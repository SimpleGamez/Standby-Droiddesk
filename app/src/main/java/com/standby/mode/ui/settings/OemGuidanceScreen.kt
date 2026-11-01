package com.standby.mode.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OemGuidanceScreen(
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val manufacturer = Build.MANUFACTURER.uppercase()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Background Restrictions & Fixes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Detected Device: $manufacturer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Certain manufacturers aggressively kill background apps and prevent auto-launching when plugged in. Follow these steps to ensure StandBy operates reliably.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Battery Optimization Settings")
                }
            }

            item {
                OemInstructionCard(
                    brand = "Samsung (One UI)",
                    steps = listOf(
                        "1. Open Settings -> Apps -> StandBy Mode",
                        "2. Tap Battery -> Select 'Unrestricted'",
                        "3. Go to Device Care -> Battery -> Background usage limits",
                        "4. Ensure StandBy is in 'Never sleeping apps'"
                    )
                )
            }

            item {
                OemInstructionCard(
                    brand = "Xiaomi / Redmi / POCO (HyperOS / MIUI)",
                    steps = listOf(
                        "1. Open Settings -> Apps -> Manage apps -> StandBy Mode",
                        "2. Enable 'Autostart'",
                        "3. Tap 'Battery saver' -> Choose 'No restrictions'",
                        "4. In Security -> Permissions -> Autostart -> Allow StandBy"
                    )
                )
            }

            item {
                OemInstructionCard(
                    brand = "OnePlus / Oppo / Realme (OxygenOS / ColorOS)",
                    steps = listOf(
                        "1. Open Settings -> Battery -> More settings -> Optimize battery use",
                        "2. Locate StandBy Mode and set to 'Don't optimize'",
                        "3. In App Management -> StandBy Mode -> Allow background activity"
                    )
                )
            }

            item {
                OemInstructionCard(
                    brand = "Google Pixel & AOSP",
                    steps = listOf(
                        "1. Open Settings -> Apps -> StandBy Mode -> App battery usage",
                        "2. Select 'Unrestricted'",
                        "3. Ensure 'Display over other apps' is permitted"
                    )
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dontkillmyapp.com/"))
                        context.startActivity(browserIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View DontKillMyApp.com Details")
                }
            }
        }
    }
}

@Composable
private fun OemInstructionCard(brand: String, steps: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = brand,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            steps.forEach { step ->
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

