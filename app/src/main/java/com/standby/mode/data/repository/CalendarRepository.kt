package com.standby.mode.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.standby.mode.domain.model.CalendarEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface CalendarRepository {
    val eventsFlow: Flow<List<CalendarEvent>>
    suspend fun refreshEvents(): List<CalendarEvent>
}

@Singleton
class CalendarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarRepository {

    private val _eventsFlow = MutableStateFlow<List<CalendarEvent>>(emptyList())
    override val eventsFlow: Flow<List<CalendarEvent>> = _eventsFlow.asStateFlow()

    override suspend fun refreshEvents(): List<CalendarEvent> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _eventsFlow.value = emptyList()
            return@withContext emptyList()
        }

        val events = mutableListOf<CalendarEvent>()
        val now = System.currentTimeMillis()
        val endOfDay = now + 24 * 60 * 60 * 1000L

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
            ContentUris.appendId(this, now)
            ContentUris.appendId(this, endOfDay)
        }.build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.DISPLAY_COLOR,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.ALL_DAY
        )

        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                val titleIdx = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
                val beginIdx = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endIdx = cursor.getColumnIndex(CalendarContract.Instances.END)
                val colorIdx = cursor.getColumnIndex(CalendarContract.Instances.DISPLAY_COLOR)
                val locationIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)
                val allDayIdx = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY)

                while (cursor.moveToNext() && events.size < 10) {
                    val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                    val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "Event" else "Event"
                    val begin = if (beginIdx >= 0) cursor.getLong(beginIdx) else now
                    val end = if (endIdx >= 0) cursor.getLong(endIdx) else now + 3600000L
                    val color = if (colorIdx >= 0) cursor.getInt(colorIdx) else 0
                    val location = if (locationIdx >= 0) cursor.getString(locationIdx) else null
                    val allDay = if (allDayIdx >= 0) cursor.getInt(allDayIdx) == 1 else false

                    events.add(
                        CalendarEvent(
                            id = id,
                            title = title,
                            startTime = begin,
                            endTime = end,
                            color = color,
                            location = location,
                            allDay = allDay
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _eventsFlow.value = events
        events
    }
}

