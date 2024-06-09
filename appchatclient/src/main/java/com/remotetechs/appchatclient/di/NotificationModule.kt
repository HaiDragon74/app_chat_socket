package com.remotetechs.appchatclient.di
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.remotetechs.appchatclient.MainActivity
import com.remotetechs.appchatclient.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class NotificationModule {
    @Provides
    fun providesNotificationBuild(@ApplicationContext context: Context): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java)
        val notificationId = System.currentTimeMillis().toInt()
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId,
            intent, PendingIntent.FLAG_MUTABLE
        )
        return NotificationCompat.Builder(context, "channel id")
            .setSmallIcon(R.drawable.ic_appchat)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
    }

    @Provides
    fun providesNotificationManager(@ApplicationContext context: Context): NotificationManagerCompat {
        val notificationManager = NotificationManagerCompat.from(context)
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                "channel id",
                "channel",
                NotificationManager.IMPORTANCE_HIGH
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        notificationManager.createNotificationChannel(channel)
        return notificationManager
    }
}