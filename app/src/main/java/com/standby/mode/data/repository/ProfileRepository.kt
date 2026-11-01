package com.standby.mode.data.repository

import com.standby.mode.data.local.PageEntity
import com.standby.mode.data.local.ProfileDao
import com.standby.mode.data.local.toDomain
import com.standby.mode.data.local.toEntity
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.ModuleType
import com.standby.mode.domain.model.PageLayout
import com.standby.mode.domain.model.StandbyModule
import com.standby.mode.domain.model.StandbyProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<StandbyProfile>>
    fun getActiveProfile(): Flow<StandbyProfile?>
    suspend fun getProfileById(id: String): StandbyProfile?
    suspend fun saveProfile(profile: StandbyProfile)
    suspend fun deleteProfile(id: String)
    suspend fun setActiveProfile(id: String)
    suspend fun ensureDefaultProfilesExist()
    fun createDefaultDeskProfile(): StandbyProfile
    fun createDefaultBedsideProfile(): StandbyProfile
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<StandbyProfile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { entity ->
                val pages = profileDao.getPagesForProfile(entity.id).map { pageEntity ->
                    val modules = profileDao.getModulesForPage(pageEntity.id).map { it.toDomain() }
                    pageEntity.toDomain(modules)
                }
                entity.toDomain(pages)
            }
        }
    }

    override fun getActiveProfile(): Flow<StandbyProfile?> {
        return profileDao.getActiveProfile().map { entity ->
            entity?.let {
                val pages = profileDao.getPagesForProfile(it.id).map { pageEntity ->
                    val modules = profileDao.getModulesForPage(pageEntity.id).map { mod -> mod.toDomain() }
                    pageEntity.toDomain(modules)
                }
                it.toDomain(pages)
            }
        }
    }

    override suspend fun getProfileById(id: String): StandbyProfile? {
        val entity = profileDao.getProfileById(id) ?: return null
        val pages = profileDao.getPagesForProfile(id).map { pageEntity ->
            val modules = profileDao.getModulesForPage(pageEntity.id).map { it.toDomain() }
            pageEntity.toDomain(modules)
        }
        return entity.toDomain(pages)
    }

    override suspend fun saveProfile(profile: StandbyProfile) {
        val profileEntity = profile.toEntity()
        val pageEntities = profile.pages.mapIndexed { index, page -> page.copy(sortOrder = index).toEntity(profile.id) }
        val moduleEntities = profile.pages.flatMap { page ->
            page.modules.map { it.toEntity(page.id) }
        }
        profileDao.saveProfileWithPagesAndModules(profileEntity, pageEntities, moduleEntities)
    }

    override suspend fun deleteProfile(id: String) {
        profileDao.deleteProfileWithChildren(id)
    }

    override suspend fun setActiveProfile(id: String) {
        profileDao.setActiveProfile(id)
    }

    override suspend fun ensureDefaultProfilesExist() {
        val desk = createDefaultDeskProfile()
        val bedside = createDefaultBedsideProfile()
        if (profileDao.getProfileById(desk.id) == null) {
            saveProfile(desk)
        }
        if (profileDao.getProfileById(bedside.id) == null) {
            saveProfile(bedside)
        }
    }

    override fun createDefaultDeskProfile(): StandbyProfile {
        val page1Id = "desk_page_1"
        val page1 = PageLayout(
            id = page1Id,
            name = "Clock & Weather",
            sortOrder = 0,
            modules = listOf(
                StandbyModule(
                    id = "mod_clock_desk",
                    type = ModuleType.CLOCK,
                    row = 0,
                    column = 0,
                    rowSpan = 2,
                    colSpan = 2,
                    config = mapOf("style" to ClockStyle.DIGITAL.name)
                ),
                StandbyModule(
                    id = "mod_weather_desk",
                    type = ModuleType.WEATHER,
                    row = 0,
                    column = 2,
                    rowSpan = 1,
                    colSpan = 2
                ),
                StandbyModule(
                    id = "mod_battery_desk",
                    type = ModuleType.BATTERY,
                    row = 1,
                    column = 2,
                    rowSpan = 1,
                    colSpan = 2
                )
            )
        )

        val page2Id = "desk_page_2"
        val page2 = PageLayout(
            id = page2Id,
            name = "Media & Calendar",
            sortOrder = 1,
            modules = listOf(
                StandbyModule(
                    id = "mod_media_desk",
                    type = ModuleType.MEDIA_CONTROLS,
                    row = 0,
                    column = 0,
                    rowSpan = 2,
                    colSpan = 2
                ),
                StandbyModule(
                    id = "mod_calendar_desk",
                    type = ModuleType.CALENDAR,
                    row = 0,
                    column = 2,
                    rowSpan = 2,
                    colSpan = 2
                )
            )
        )

        return StandbyProfile(
            id = "profile_desk",
            name = "Desk",
            pages = listOf(page1, page2),
            isActive = true
        )
    }

    override fun createDefaultBedsideProfile(): StandbyProfile {
        val pageId = "bedside_page_1"
        val page = PageLayout(
            id = pageId,
            name = "Night Clock",
            sortOrder = 0,
            modules = listOf(
                StandbyModule(
                    id = "mod_clock_bedside",
                    type = ModuleType.CLOCK,
                    row = 0,
                    column = 0,
                    rowSpan = 2,
                    colSpan = 4,
                    config = mapOf("style" to ClockStyle.MINIMAL.name)
                )
            )
        )

        return StandbyProfile(
            id = "profile_bedside",
            name = "Bedside",
            pages = listOf(page),
            isActive = false
        )
    }
}

