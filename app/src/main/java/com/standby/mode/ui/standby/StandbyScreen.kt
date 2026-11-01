package com.standby.mode.ui.standby

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.ModuleType
import com.standby.mode.domain.model.PageLayout
import com.standby.mode.domain.model.StandbyModule
import com.standby.mode.ui.components.AutoDimOverlay
import com.standby.mode.ui.components.BurnInProtectionWrapper
import com.standby.mode.ui.components.NightModeOverlay
import com.standby.mode.ui.modules.AmbientVisualsModule
import com.standby.mode.ui.modules.BatteryModule
import com.standby.mode.ui.modules.CalendarModule
import com.standby.mode.ui.modules.ClockModule
import com.standby.mode.ui.modules.CustomTextModule
import com.standby.mode.ui.modules.MediaControlsModule
import com.standby.mode.ui.modules.NotificationsModule
import com.standby.mode.ui.modules.PhotoSlideshowModule
import com.standby.mode.ui.modules.QuickTogglesModule
import com.standby.mode.ui.modules.TimerModule
import com.standby.mode.ui.modules.WeatherModule
import com.standby.mode.ui.settings.MainActivity
import com.standby.mode.ui.theme.StandByTheme
import kotlinx.coroutines.delay

@Composable
fun StandbyScreenHost(
    onExit: () -> Unit,
    viewModel: StandbyViewModel = hiltViewModel()
) {
    val activeProfile by viewModel.activeProfile.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isNightActive by viewModel.brightnessController.isNightActive.collectAsState()

    val themeConfig = activeProfile?.themeConfig
    val nightMode = activeProfile?.brightnessConfig?.nightMode ?: com.standby.mode.domain.model.NightMode.OFF

    StandByTheme(
        themeConfig = themeConfig ?: com.standby.mode.domain.model.ThemeConfig(),
        nightMode = if (isNightActive) nightMode else com.standby.mode.domain.model.NightMode.OFF
    ) {
        StandbyScreen(
            viewModel = viewModel,
            onExit = onExit
        )
    }
}

@Composable
fun StandbyScreen(
    viewModel: StandbyViewModel,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val mediaInfo by viewModel.mediaInfo.collectAsState()
    val weather by viewModel.currentWeather.collectAsState()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isControlsVisible by viewModel.isControlsVisible.collectAsState()
    val isDimmed by viewModel.isDimmed.collectAsState()
    val isNightActive by viewModel.brightnessController.isNightActive.collectAsState()

    val pages = activeProfile?.pages.orEmpty()
    val pagerState = rememberPagerState(pageCount = { if (pages.isEmpty()) 1 else pages.size })

    // Auto rotate pages
    LaunchedEffect(settings.pageAutoRotateEnabled, settings.pageAutoRotateIntervalSeconds, pages.size) {
        if (settings.pageAutoRotateEnabled && pages.size > 1) {
            while (true) {
                delay(settings.pageAutoRotateIntervalSeconds * 1000L)
                val nextPage = (pagerState.currentPage + 1) % pages.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    // Burn-in config
    val burnInConfig = activeProfile?.burnInConfig ?: com.standby.mode.domain.model.BurnInConfig()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (activeProfile?.themeConfig?.useGradientBackground == true &&
                    activeProfile?.themeConfig?.gradientColors?.size ?: 0 >= 2
                ) {
                    Brush.verticalGradient(
                        activeProfile!!.themeConfig.gradientColors.map { Color(it) }
                    )
                } else {
                    Brush.verticalGradient(listOf(Color.Black, Color(0xFF08080C)))
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.handleGesture(
                    action = settings.tapAction,
                    onNextPage = { /* handled by pager */ },
                    onPrevPage = { /* handled by pager */ },
                    onExit = onExit
                )
            }
    ) {
        BurnInProtectionWrapper(config = burnInConfig) {
            if (pages.isEmpty()) {
                // Fallback page
                DefaultStandbyPage(
                    settings = settings,
                    batteryInfo = batteryInfo,
                    weather = weather,
                    isWeatherLoading = isWeatherLoading
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    RenderPage(
                        page = page,
                        settings = settings,
                        batteryInfo = batteryInfo,
                        mediaInfo = mediaInfo,
                        weather = weather,
                        isWeatherLoading = isWeatherLoading,
                        calendarEvents = calendarEvents,
                        notifications = notifications,
                        onPlayPause = { viewModel.playPauseMedia() },
                        onNext = { viewModel.nextMedia() },
                        onPrev = { viewModel.prevMedia() }
                    )
                }
            }
        }

        // Night mode overlay
        NightModeOverlay(
            nightMode = activeProfile?.brightnessConfig?.nightMode ?: com.standby.mode.domain.model.NightMode.OFF,
            isNightActive = isNightActive
        )

        // Inactivity Auto-Dim overlay
        AutoDimOverlay(
            isDimmed = isDimmed,
            dimLevel = activeProfile?.brightnessConfig?.dimLevel ?: 0.75f,
            onWake = { viewModel.onUserInteraction() }
        )

        // Floating On-Screen Controls
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeProfile?.name?.let { profileName ->
                    Text(
                        text = profileName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onExit) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit StandBy",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultStandbyPage(
    settings: com.standby.mode.domain.model.StandbySettings,
    batteryInfo: com.standby.mode.domain.model.BatteryInfo,
    weather: com.standby.mode.domain.model.WeatherData?,
    isWeatherLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            ClockModule(
                style = settings.clockStyle,
                is24Hour = settings.is24Hour,
                showSeconds = settings.showSeconds,
                dateFormat = settings.dateFormat,
                additionalTimeZones = settings.additionalTimeZones
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            WeatherModule(
                weather = weather,
                isLoading = isWeatherLoading,
                unit = settings.temperatureUnit
            )
        }
    }
}

@Composable
private fun RenderPage(
    page: PageLayout,
    settings: com.standby.mode.domain.model.StandbySettings,
    batteryInfo: com.standby.mode.domain.model.BatteryInfo,
    mediaInfo: com.standby.mode.domain.model.MediaInfo?,
    weather: com.standby.mode.domain.model.WeatherData?,
    isWeatherLoading: Boolean,
    calendarEvents: List<com.standby.mode.domain.model.CalendarEvent>,
    notifications: List<com.standby.mode.domain.model.NotificationItem>,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val modules = page.modules
    if (modules.isEmpty()) return

    // In landscape standby, render as a 2-column split (Left side & Right side)
    val leftModules = modules.filter { it.column < 2 }
    val rightModules = modules.filter { it.column >= 2 }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rightModules.isEmpty()) {
            // Full screen single module / stack
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                modules.forEach { mod ->
                    RenderModule(
                        module = mod,
                        settings = settings,
                        batteryInfo = batteryInfo,
                        mediaInfo = mediaInfo,
                        weather = weather,
                        isWeatherLoading = isWeatherLoading,
                        calendarEvents = calendarEvents,
                        notifications = notifications,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrev = onPrev
                    )
                }
            }
        } else {
            // Left Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                leftModules.forEach { mod ->
                    RenderModule(
                        module = mod,
                        settings = settings,
                        batteryInfo = batteryInfo,
                        mediaInfo = mediaInfo,
                        weather = weather,
                        isWeatherLoading = isWeatherLoading,
                        calendarEvents = calendarEvents,
                        notifications = notifications,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrev = onPrev
                    )
                }
            }

            // Right Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                rightModules.forEach { mod ->
                    RenderModule(
                        module = mod,
                        settings = settings,
                        batteryInfo = batteryInfo,
                        mediaInfo = mediaInfo,
                        weather = weather,
                        isWeatherLoading = isWeatherLoading,
                        calendarEvents = calendarEvents,
                        notifications = notifications,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrev = onPrev
                    )
                }
            }
        }
    }
}

@Composable
fun RenderModule(
    module: StandbyModule,
    settings: com.standby.mode.domain.model.StandbySettings,
    batteryInfo: com.standby.mode.domain.model.BatteryInfo,
    mediaInfo: com.standby.mode.domain.model.MediaInfo?,
    weather: com.standby.mode.domain.model.WeatherData?,
    isWeatherLoading: Boolean,
    calendarEvents: List<com.standby.mode.domain.model.CalendarEvent>,
    notifications: List<com.standby.mode.domain.model.NotificationItem>,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    when (module.type) {
        ModuleType.CLOCK -> {
            val style = module.config["style"]?.let {
                runCatching { ClockStyle.valueOf(it) }.getOrDefault(settings.clockStyle)
            } ?: settings.clockStyle
            ClockModule(
                style = style,
                is24Hour = settings.is24Hour,
                showSeconds = settings.showSeconds,
                dateFormat = settings.dateFormat,
                additionalTimeZones = settings.additionalTimeZones
            )
        }
        ModuleType.BATTERY -> BatteryModule(batteryInfo = batteryInfo)
        ModuleType.WEATHER -> WeatherModule(
            weather = weather,
            isLoading = isWeatherLoading,
            unit = settings.temperatureUnit
        )
        ModuleType.CALENDAR -> CalendarModule(events = calendarEvents)
        ModuleType.MEDIA_CONTROLS -> MediaControlsModule(
            mediaInfo = mediaInfo,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrev
        )
        ModuleType.NOTIFICATIONS -> NotificationsModule(
            notifications = notifications,
            privacyMode = settings.notificationPrivacyMode
        )
        ModuleType.PHOTO_SLIDESHOW -> {
            val uriList = settings.photoFolderUri?.let { listOf(Uri.parse(it)) } ?: emptyList()
            PhotoSlideshowModule(
                photos = uriList,
                intervalSeconds = settings.photoIntervalSeconds,
                kenBurnsEnabled = settings.kenBurnsEnabled
            )
        }
        ModuleType.QUICK_TOGGLES -> QuickTogglesModule()
        ModuleType.TIMER -> TimerModule()
        ModuleType.CUSTOM_TEXT -> {
            val text = module.config["text"] ?: "Stay focused and stay grounded."
            val author = module.config["author"] ?: ""
            CustomTextModule(text = text, author = author)
        }
        ModuleType.AMBIENT_VISUALS -> AmbientVisualsModule()
    }
}

