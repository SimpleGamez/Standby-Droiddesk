package com.standby.mode.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standby.mode.data.repository.ProfileRepository
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.ModuleType
import com.standby.mode.domain.model.PageLayout
import com.standby.mode.domain.model.StandbyModule
import com.standby.mode.domain.model.StandbyProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LayoutEditorViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _currentProfile = MutableStateFlow<StandbyProfile?>(null)
    val currentProfile: StateFlow<StandbyProfile?> = _currentProfile.asStateFlow()

    private val _selectedPageIndex = MutableStateFlow(0)
    val selectedPageIndex: StateFlow<Int> = _selectedPageIndex.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getActiveProfile().collect { profile ->
                if (_currentProfile.value == null && profile != null) {
                    _currentProfile.value = profile
                }
            }
        }
    }

    fun selectPage(index: Int) {
        _selectedPageIndex.value = index
    }

    fun addPage(name: String) {
        val profile = _currentProfile.value ?: return
        val newPage = PageLayout(
            id = "page_${UUID.randomUUID()}",
            name = name,
            sortOrder = profile.pages.size,
            modules = listOf(
                StandbyModule(
                    id = "mod_${UUID.randomUUID()}",
                    type = ModuleType.CLOCK,
                    row = 0,
                    column = 0,
                    rowSpan = 2,
                    colSpan = 2
                )
            )
        )
        val updatedPages = profile.pages + newPage
        val updatedProfile = profile.copy(pages = updatedPages)
        _currentProfile.value = updatedProfile
        _selectedPageIndex.value = updatedPages.size - 1
        saveChanges()
    }

    fun deletePage(pageIndex: Int) {
        val profile = _currentProfile.value ?: return
        if (profile.pages.size <= 1) return // Keep at least one page
        val updatedPages = profile.pages.toMutableList().apply { removeAt(pageIndex) }
        val updatedProfile = profile.copy(pages = updatedPages)
        _currentProfile.value = updatedProfile
        _selectedPageIndex.value = 0.coerceAtMost(updatedPages.size - 1)
        saveChanges()
    }

    fun addModule(type: ModuleType, column: Int = 0) {
        val profile = _currentProfile.value ?: return
        val pageIdx = _selectedPageIndex.value
        if (pageIdx !in profile.pages.indices) return

        val page = profile.pages[pageIdx]
        val newModule = StandbyModule(
            id = "mod_${UUID.randomUUID()}",
            type = type,
            row = page.modules.size,
            column = column,
            rowSpan = 1,
            colSpan = 2,
            config = if (type == ModuleType.CLOCK) mapOf("style" to ClockStyle.DIGITAL.name) else emptyMap()
        )

        val updatedModules = page.modules + newModule
        val updatedPage = page.copy(modules = updatedModules)
        val updatedPages = profile.pages.toMutableList().apply { set(pageIdx, updatedPage) }
        _currentProfile.value = profile.copy(pages = updatedPages)
        saveChanges()
    }

    fun removeModule(moduleId: String) {
        val profile = _currentProfile.value ?: return
        val pageIdx = _selectedPageIndex.value
        if (pageIdx !in profile.pages.indices) return

        val page = profile.pages[pageIdx]
        val updatedModules = page.modules.filterNot { it.id == moduleId }
        val updatedPage = page.copy(modules = updatedModules)
        val updatedPages = profile.pages.toMutableList().apply { set(pageIdx, updatedPage) }
        _currentProfile.value = profile.copy(pages = updatedPages)
        saveChanges()
    }

    fun updateModuleConfig(moduleId: String, configKey: String, configValue: String) {
        val profile = _currentProfile.value ?: return
        val pageIdx = _selectedPageIndex.value
        if (pageIdx !in profile.pages.indices) return

        val page = profile.pages[pageIdx]
        val updatedModules = page.modules.map { mod ->
            if (mod.id == moduleId) {
                val newConfig = mod.config.toMutableMap().apply { put(configKey, configValue) }
                mod.copy(config = newConfig)
            } else mod
        }
        val updatedPage = page.copy(modules = updatedModules)
        val updatedPages = profile.pages.toMutableList().apply { set(pageIdx, updatedPage) }
        _currentProfile.value = profile.copy(pages = updatedPages)
        saveChanges()
    }

    fun moveModuleToColumn(moduleId: String, targetColumn: Int) {
        val profile = _currentProfile.value ?: return
        val pageIdx = _selectedPageIndex.value
        if (pageIdx !in profile.pages.indices) return

        val page = profile.pages[pageIdx]
        val updatedModules = page.modules.map { mod ->
            if (mod.id == moduleId) mod.copy(column = targetColumn) else mod
        }
        val updatedPage = page.copy(modules = updatedModules)
        val updatedPages = profile.pages.toMutableList().apply { set(pageIdx, updatedPage) }
        _currentProfile.value = profile.copy(pages = updatedPages)
        saveChanges()
    }

    fun saveChanges() {
        val profile = _currentProfile.value ?: return
        viewModelScope.launch {
            profileRepository.saveProfile(profile)
        }
    }
}

