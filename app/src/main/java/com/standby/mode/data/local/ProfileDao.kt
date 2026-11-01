package com.standby.mode.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    // --- Profile ---

    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun deleteProfileById(profileId: String)

    @Query("UPDATE profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun activateProfile(profileId: String)

    @Transaction
    suspend fun setActiveProfile(profileId: String) {
        deactivateAllProfiles()
        activateProfile(profileId)
    }

    // --- Pages ---

    @Query("SELECT * FROM pages WHERE profileId = :profileId ORDER BY sortOrder ASC")
    suspend fun getPagesForProfile(profileId: String): List<PageEntity>

    @Query("SELECT * FROM pages WHERE profileId = :profileId ORDER BY sortOrder ASC")
    fun observePagesForProfile(profileId: String): Flow<List<PageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<PageEntity>)

    @Query("DELETE FROM pages WHERE id = :pageId")
    suspend fun deletePageById(pageId: String)

    @Query("DELETE FROM pages WHERE profileId = :profileId")
    suspend fun deletePagesForProfile(profileId: String)

    // --- Modules ---

    @Query("SELECT * FROM modules WHERE pageId = :pageId ORDER BY `row` ASC, `column` ASC")
    suspend fun getModulesForPage(pageId: String): List<ModuleEntity>

    @Query("SELECT * FROM modules WHERE pageId = :pageId ORDER BY `row` ASC, `column` ASC")
    fun observeModulesForPage(pageId: String): Flow<List<ModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: ModuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>)

    @Query("DELETE FROM modules WHERE id = :moduleId")
    suspend fun deleteModuleById(moduleId: String)

    @Query("DELETE FROM modules WHERE pageId = :pageId")
    suspend fun deleteModulesForPage(pageId: String)

    // --- Composite operations ---

    @Transaction
    suspend fun saveProfileWithPagesAndModules(
        profile: ProfileEntity,
        pages: List<PageEntity>,
        modules: List<ModuleEntity>
    ) {
        insertProfile(profile)
        deletePagesForProfile(profile.id)
        insertPages(pages)
        for (page in pages) {
            deleteModulesForPage(page.id)
        }
        insertModules(modules)
    }

    @Transaction
    suspend fun deleteProfileWithChildren(profileId: String) {
        val pages = getPagesForProfile(profileId)
        for (page in pages) {
            deleteModulesForPage(page.id)
        }
        deletePagesForProfile(profileId)
        deleteProfileById(profileId)
    }
}
