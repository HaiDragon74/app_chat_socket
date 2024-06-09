package com.remotetechs.appchatclient.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import com.remotetechs.appchatclient.model.Message
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val notificationBuild: NotificationCompat.Builder,
    private val notificationManager: NotificationManagerCompat
) : ViewModel() {
    fun showNotification(context: Context, message: Message) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationBuild
            .setContentTitle(message.name)
            .setContentText(message.message)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuild.build())
    }
}