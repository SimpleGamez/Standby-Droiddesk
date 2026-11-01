package com.standby.mode.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.ModuleType
import com.standby.mode.domain.model.StandbyModule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutEditorScreen(
    onNavigateUp: () -> Unit,
    viewModel: LayoutEditorViewModel = hiltViewModel()
) {
    val profile by viewModel.currentProfile.collectAsState()
    val selectedPageIndex by viewModel.selectedPageIndex.collectAsState()

    var showAddModuleMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Layout & Widget Editor") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showAddModuleMenu = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Widget")
                        }
                        DropdownMenu(
                            expanded = showAddModuleMenu,
                            onDismissRequest = { showAddModuleMenu = false }
                        ) {
                            ModuleType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name.replace("_", " ")) },
                                    onClick = {
                                        viewModel.addModule(type)
                                        showAddModuleMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val pages = profile?.pages.orEmpty()
            if (pages.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedPageIndex.coerceIn(0, pages.size - 1),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    pages.forEachIndexed { index, page ->
                        Tab(
                            selected = index == selectedPageIndex,
                            onClick = { viewModel.selectPage(index) },
                            text = { Text(page.name) }
                        )
                    }
                }

                val currentPage = pages.getOrNull(selectedPageIndex)
                if (currentPage != null) {
                    PageEditorContent(
                        modules = currentPage.modules,
                        onRemoveModule = { viewModel.removeModule(it) },
                        onMoveModule = { id, col -> viewModel.moveModuleToColumn(id, col) },
                        onUpdateClockStyle = { id, style ->
                            viewModel.updateModuleConfig(id, "style", style.name)
                        }
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = { viewModel.addPage("Main Page") }) {
                        Text("Create First Page")
                    }
                }
            }
        }
    }
}

@Composable
private fun PageEditorContent(
    modules: List<StandbyModule>,
    onRemoveModule: (String) -> Unit,
    onMoveModule: (String, Int) -> Unit,
    onUpdateClockStyle: (String, ClockStyle) -> Unit
) {
    val leftModules = modules.filter { it.column < 2 }
    val rightModules = modules.filter { it.column >= 2 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "LEFT SIDE (Col 0-1)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
        }

        if (leftModules.isEmpty()) {
            item {
                EmptyColumnCard(label = "No widgets on Left Side")
            }
        } else {
            items(leftModules, key = { it.id }) { mod ->
                ModuleEditorCard(
                    module = mod,
                    currentColumn = 0,
                    onMove = { onMoveModule(mod.id, 2) },
                    onDelete = { onRemoveModule(mod.id) },
                    onUpdateClockStyle = { onUpdateClockStyle(mod.id, it) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "RIGHT SIDE (Col 2-3)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
            )
        }

        if (rightModules.isEmpty()) {
            item {
                EmptyColumnCard(label = "No widgets on Right Side")
            }
        } else {
            items(rightModules, key = { it.id }) { mod ->
                ModuleEditorCard(
                    module = mod,
                    currentColumn = 2,
                    onMove = { onMoveModule(mod.id, 0) },
                    onDelete = { onRemoveModule(mod.id) },
                    onUpdateClockStyle = { onUpdateClockStyle(mod.id, it) }
                )
            }
        }
    }
}

@Composable
private fun EmptyColumnCard(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModuleEditorCard(
    module: StandbyModule,
    currentColumn: Int,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onUpdateClockStyle: (ClockStyle) -> Unit
) {
    var showClockStyleMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getModuleIcon(module.type),
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = module.type.name.replace("_", " "),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentColumn == 0) "Left Side" else "Right Side",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Move button
                    IconButton(onClick = onMove) {
                        Icon(
                            imageVector = if (currentColumn == 0) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (currentColumn == 0) "Move Right" else "Move Left",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Delete button
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Widget",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // If Clock Module, provide Clock Style picker
            if (module.type == ModuleType.CLOCK) {
                Spacer(modifier = Modifier.height(10.dp))
                val currentStyle = module.config["style"] ?: ClockStyle.DIGITAL.name
                Box {
                    Button(
                        onClick = { showClockStyleMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Style: $currentStyle")
                    }
                    DropdownMenu(
                        expanded = showClockStyleMenu,
                        onDismissRequest = { showClockStyleMenu = false }
                    ) {
                        ClockStyle.values().forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style.name.replace("_", " ")) },
                                onClick = {
                                    onUpdateClockStyle(style)
                                    showClockStyleMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getModuleIcon(type: ModuleType): String {
    return when (type) {
        ModuleType.CLOCK -> "⏰"
        ModuleType.WEATHER -> "🌤️"
        ModuleType.BATTERY -> "🔋"
        ModuleType.MEDIA_CONTROLS -> "🎵"
        ModuleType.CALENDAR -> "📅"
        ModuleType.PHOTO_SLIDESHOW -> "🖼️"
        ModuleType.NOTIFICATIONS -> "🔔"
        ModuleType.QUICK_TOGGLES -> "🎛️"
        ModuleType.TIMER -> "⏱️"
        ModuleType.CUSTOM_TEXT -> "💬"
        ModuleType.AMBIENT_VISUALS -> "✨"
    }
}

