package com.standby.mode.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.standby.mode.domain.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StandbyNotificationListenerService : NotificationListenerService() {

    companion object {
        private val _activeNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
        val activeNotifications: StateFlow<List<NotificationItem>> = _activeNotifications.asStateFlow()

        var isConnected: Boolean = false
            private set

        var onConnectedCallback: (() -> Unit)? = null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        updateNotifications()
        onConnectedCallback?.invoke()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
        _activeNotifications.value = emptyList()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateNotifications()
    }

    private fun updateNotifications() {
        try {
            val sbns = try {
                super.getActiveNotifications()?.toList() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            val pm = packageManager
            val items = sbns.mapNotNull { sbn ->
                // Skip ongoing/persistent notifications (like foreground services)
                if (sbn.isOngoing) return@mapNotNull null

                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                    ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

                if (title.isBlank() && text.isBlank()) return@mapNotNull null

                val appName = try {
                    val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    sbn.packageName
                }

                NotificationItem(
                    id = sbn.key ?: "${sbn.packageName}_${sbn.id}",
                    packageName = sbn.packageName,
                    appName = appName,
                    title = title,
                    content = text,
                    timestamp = sbn.postTime,
                    category = sbn.notification.category
                )
            }.take(10)

            _activeNotifications.value = items
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

