package com.cortisense.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object NotificationHelper {
    const val CHANNEL_CHECKIN = "checkin_reminders"
    const val CHANNEL_EXERCISE = "exercise_reminders"
    const val CHANNEL_STRESS = "stress_alerts"
    const val CHANNEL_ENGAGEMENT = "engagement_notifs"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name1 = context.getString(R.string.notif_channel_checkin_name)
            val desc1 = context.getString(R.string.notif_channel_checkin_desc)
            val channel1 = NotificationChannel(CHANNEL_CHECKIN, name1, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = desc1
            }

            val name2 = context.getString(R.string.notif_channel_exercise_name)
            val desc2 = context.getString(R.string.notif_channel_exercise_desc)
            val channel2 = NotificationChannel(CHANNEL_EXERCISE, name2, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = desc2
            }

            val name3 = context.getString(R.string.notif_channel_stress_name)
            val desc3 = context.getString(R.string.notif_channel_stress_desc)
            val channel3 = NotificationChannel(CHANNEL_STRESS, name3, NotificationManager.IMPORTANCE_HIGH).apply {
                description = desc3
            }

            val name4 = context.getString(R.string.notif_channel_engagement_name)
            val desc4 = context.getString(R.string.notif_channel_engagement_desc)
            val channel4 = NotificationChannel(CHANNEL_ENGAGEMENT, name4, NotificationManager.IMPORTANCE_LOW).apply {
                description = desc4
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel1)
            manager.createNotificationChannel(channel2)
            manager.createNotificationChannel(channel3)
            manager.createNotificationChannel(channel4)
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use appropriate icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
                
                // Persist to database
                CoroutineScope(Dispatchers.IO).launch {
                    val pref = PreferenceManager(context)
                    val email = pref.userEmail.first()
                    if (email.isNotEmpty()) {
                        val db = AppDatabase.getDatabase(context)
                        db.notificationDao().insertNotification(
                            NotificationEntity(
                                userEmail = email,
                                title = title,
                                message = message,
                                type = channelId
                            )
                        )
                    }
                }
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }

    fun getWittyCheckinMessage(): Pair<Int, Int> {
        val messages = listOf(
            R.string.witty_checkin_t1 to R.string.witty_checkin_m1,
            R.string.witty_checkin_t2 to R.string.witty_checkin_m2,
            R.string.witty_checkin_t3 to R.string.witty_checkin_m3,
            R.string.witty_checkin_t4 to R.string.witty_checkin_m4
        )
        return messages.random()
    }

    fun getWittyStressMessage(level: String): Pair<Int, Int> {
        return when (level) {
            "stress_level_critical" -> listOf(
                R.string.witty_critical_t1 to R.string.witty_critical_m1,
                R.string.witty_critical_t2 to R.string.witty_critical_m2
            ).random()
            "stress_level_high" -> listOf(
                R.string.witty_high_t1 to R.string.witty_high_m1,
                R.string.witty_high_t2 to R.string.witty_high_m2
            ).random()
            "stress_level_moderate" -> listOf(
                R.string.witty_moderate_t1 to R.string.witty_moderate_m1,
                R.string.witty_moderate_t2 to R.string.witty_moderate_m2
            ).random()
            else -> listOf(
                R.string.witty_low_t1 to R.string.witty_low_m1,
                R.string.witty_low_t2 to R.string.witty_low_m2
            ).random()
        }
    }

    fun getRandomEngagementMessage(): Pair<Int, Int> {
        val messages = listOf(
            R.string.witty_engage_t1 to R.string.witty_engage_m1,
            R.string.witty_engage_t2 to R.string.witty_engage_m2,
            R.string.witty_engage_t3 to R.string.witty_engage_m3,
            R.string.witty_engage_t4 to R.string.witty_engage_m4,
            R.string.witty_engage_t5 to R.string.witty_engage_m5,
            R.string.witty_engage_t6 to R.string.witty_engage_m6
        )
        return messages.random()
    }

    fun scheduleNotifications(context: Context) {
        val checkinWork = androidx.work.PeriodicWorkRequestBuilder<NotificationWorker>(6, java.util.concurrent.TimeUnit.HOURS)
            .setInputData(androidx.work.workDataOf("type" to "checkin"))
            .build()
            
        val engagementWork = androidx.work.PeriodicWorkRequestBuilder<NotificationWorker>(1, java.util.concurrent.TimeUnit.DAYS)
            .setInputData(androidx.work.workDataOf("type" to "engagement"))
            .build()
            
        val achievementWork = androidx.work.PeriodicWorkRequestBuilder<NotificationWorker>(7, java.util.concurrent.TimeUnit.DAYS)
            .setInputData(androidx.work.workDataOf("type" to "achievements"))
            .build()
            
        val stressWork = androidx.work.PeriodicWorkRequestBuilder<NotificationWorker>(12, java.util.concurrent.TimeUnit.HOURS)
            .setInputData(androidx.work.workDataOf("type" to "stress"))
            .build()
            
        val tasksWork = androidx.work.PeriodicWorkRequestBuilder<NotificationWorker>(8, java.util.concurrent.TimeUnit.HOURS)
            .setInputData(androidx.work.workDataOf("type" to "tasks"))
            .build()

        androidx.work.WorkManager.getInstance(context).apply {
            enqueueUniquePeriodicWork("checkinWork", androidx.work.ExistingPeriodicWorkPolicy.KEEP, checkinWork)
            enqueueUniquePeriodicWork("engagementWork", androidx.work.ExistingPeriodicWorkPolicy.KEEP, engagementWork)
            enqueueUniquePeriodicWork("achievementWork", androidx.work.ExistingPeriodicWorkPolicy.KEEP, achievementWork)
            enqueueUniquePeriodicWork("stressWork", androidx.work.ExistingPeriodicWorkPolicy.KEEP, stressWork)
            enqueueUniquePeriodicWork("tasksWork", androidx.work.ExistingPeriodicWorkPolicy.KEEP, tasksWork)
        }
    }
}
