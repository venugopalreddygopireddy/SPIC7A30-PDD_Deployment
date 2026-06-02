package com.cortisense.app

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val type = inputData.getString("type") ?: "engagement"

        when (type) {
            "checkin" -> {
                val (titleRes, msgRes) = NotificationHelper.getWittyCheckinMessage()
                NotificationHelper.showNotification(
                    applicationContext,
                    NotificationHelper.CHANNEL_CHECKIN,
                    1001,
                    applicationContext.getString(titleRes),
                    applicationContext.getString(msgRes)
                )
            }
            "stress_reminder" -> {
                val level = inputData.getString("level") ?: "stress_level_low"
                val (titleRes, msgRes) = NotificationHelper.getWittyStressMessage(level)
                NotificationHelper.showNotification(
                    applicationContext,
                    NotificationHelper.CHANNEL_STRESS,
                    1002,
                    applicationContext.getString(titleRes),
                    applicationContext.getString(msgRes)
                )
            }
            "engagement" -> {
                val (titleRes, msgRes) = NotificationHelper.getRandomEngagementMessage()
                NotificationHelper.showNotification(
                    applicationContext,
                    NotificationHelper.CHANNEL_ENGAGEMENT,
                    1003,
                    applicationContext.getString(titleRes),
                    applicationContext.getString(msgRes)
                )
            }
        }

        return Result.success()
    }
}
