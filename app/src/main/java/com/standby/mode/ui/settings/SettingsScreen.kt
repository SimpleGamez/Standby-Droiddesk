package com.standby.mode.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.ColorPalette
import com.standby.mode.domain.model.NightMode
import com.standby.mode.ui.standby.StandbyActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToEditor: () -> Unit,
    onNavigateToOem: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    var showPaletteMenu by remember { mutableStateOf(false) }
    var showClockStyleMenu by remember { mutableStateOf(false) }
    var showNightModeMenu by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Standby Droiddesk Settings") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview
            item {
                LivePreviewPane(profile = activeProfile, settings = settings)
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(context, StandbyActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.padding(start = 6.dp))
                        Text("Start StandBy")
                    }

                    OutlinedButton(
                        onClick = onNavigateToEditor,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ViewQuilt, contentDescription = null)
                        Spacer(modifier = Modifier.padding(start = 6.dp))
                        Text("Edit Layout")
                    }
                }
            }

            // Profiles
            item {
                SectionHeader("PROFILES")
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showProfileMenu = true }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Profile", fontWeight = FontWeight.SemiBold)
                            Text(
                                activeProfile?.name ?: "None",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false }
                    ) {
                        allProfiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text(profile.name) },
                                onClick = {
                                    viewModel.switchProfile(profile.id)
                                    showProfileMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Appearance & Themes
            item {
                SectionHeader("APPEARANCE & THEMES")
                SettingsCard {
                    // Color Palette
                    Box {
                        SettingRow(
                            title = "Color Palette",
                            subtitle = activeProfile?.themeConfig?.colorPalette?.name?.replace("_", " ") ?: "MATERIAL YOU",
                            onClick = { showPaletteMenu = true }
                        )
                        DropdownMenu(
                            expanded = showPaletteMenu,
                            onDismissRequest = { showPaletteMenu = false }
                        ) {
                            ColorPalette.values().forEach { palette ->
                                DropdownMenuItem(
                                    text = { Text(palette.name.replace("_", " ")) },
                                    onClick = {
                                        viewModel.updateColorPalette(palette)
                                        showPaletteMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Clock Style
                    Box {
                        SettingRow(
                            title = "Clock Style",
                            subtitle = settings.clockStyle.name.replace("_", " "),
                            onClick = { showClockStyleMenu = true }
                        )
                        DropdownMenu(
                            expanded = showClockStyleMenu,
                            onDismissRequest = { showClockStyleMenu = false }
                        ) {
                            ClockStyle.values().forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style.name.replace("_", " ")) },
                                    onClick = {
                                        viewModel.updateClockStyle(style)
                                        showClockStyleMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 24 Hour Toggle
                    SettingSwitchRow(
                        title = "24-Hour Time Format",
                        checked = settings.is24Hour,
                        onCheckedChange = { viewModel.update24Hour(it) }
                    )

                    // Show Seconds
                    SettingSwitchRow(
                        title = "Show Seconds",
                        checked = settings.showSeconds,
                        onCheckedChange = { viewModel.updateShowSeconds(it) }
                    )

                    // Gradient Background
                    SettingSwitchRow(
                        title = "Ambient Gradient Background",
                        checked = activeProfile?.themeConfig?.useGradientBackground ?: false,
                        onCheckedChange = { viewModel.updateGradientBackground(it) }
                    )
                }
            }

            // Automatic Triggers
            item {
                SectionHeader("AUTOMATIC TRIGGERS")
                val triggerConfig = activeProfile?.triggerConfig ?: com.standby.mode.domain.model.TriggerConfig()
                SettingsCard {
                    SettingSwitchRow(
                        title = "Trigger on Charger Connected",
                        checked = triggerConfig.onChargerConnected,
                        onCheckedChange = { c -> viewModel.updateTriggerConfig { it.copy(onChargerConnected = c) } }
                    )
                    SettingSwitchRow(
                        title = "Trigger on Charging + Landscape",
                        checked = triggerConfig.onChargingAndLandscape,
                        onCheckedChange = { c -> viewModel.updateTriggerConfig { it.copy(onChargingAndLandscape = c) } }
                    )
                    SettingSwitchRow(
                        title = "Trigger on Charging + Screen Locked",
                        checked = triggerConfig.onChargingAndLocked,
                        onCheckedChange = { c -> viewModel.updateTriggerConfig { it.copy(onChargingAndLocked = c) } }
                    )
                    SettingSwitchRow(
                        title = "Trigger on Charging + Stationary (Accelerometer)",
                        checked = triggerConfig.onChargingAndStationary,
                        onCheckedChange = { c -> viewModel.updateTriggerConfig { it.copy(onChargingAndStationary = c) } }
                    )
                    SettingSwitchRow(
                        title = "Exit StandBy on Unplug",
                        checked = settings.exitOnUnplug,
                        onCheckedChange = { viewModel.updateTriggerConfig { it } /* updated in settings */ }
                    )
                }
            }

            // Burn-In Protection & Night Mode
            item {
                SectionHeader("BURN-IN PROTECTION & NIGHT MODE")
                val burnInConfig = activeProfile?.burnInConfig ?: com.standby.mode.domain.model.BurnInConfig()
                SettingsCard {
                    SettingSwitchRow(
                        title = "Subtle Pixel Shift",
                        checked = burnInConfig.pixelShiftEnabled,
                        onCheckedChange = { c -> viewModel.updateBurnInConfig { it.copy(pixelShiftEnabled = c) } }
                    )
                    SettingSwitchRow(
                        title = "Periodic Floating Motion",
                        checked = burnInConfig.periodicMotionEnabled,
                        onCheckedChange = { c -> viewModel.updateBurnInConfig { it.copy(periodicMotionEnabled = c) } }
                    )
                    SettingSwitchRow(
                        title = "Inactivity Auto-Dim",
                        checked = burnInConfig.autoDimEnabled,
                        onCheckedChange = { c -> viewModel.updateBurnInConfig { it.copy(autoDimEnabled = c) } }
                    )

                    // Night Mode
                    Box {
                        SettingRow(
                            title = "Night Mode Tint",
                            subtitle = activeProfile?.brightnessConfig?.nightMode?.name ?: "OFF",
                            onClick = { showNightModeMenu = true }
                        )
                        DropdownMenu(
                            expanded = showNightModeMenu,
                            onDismissRequest = { showNightModeMenu = false }
                        ) {
                            NightMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.name.replace("_", " ")) },
                                    onClick = {
                                        viewModel.updateNightMode(mode)
                                        showNightModeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // System & OEM Guidance
            item {
                SectionHeader("SYSTEM INTEGRATION & OEM")
                SettingsCard {
                    SettingRow(
                        title = "OEM Background Optimization Fixes",
                        subtitle = "Samsung, Xiaomi, OnePlus battery killer settings",
                        onClick = onNavigateToOem
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

