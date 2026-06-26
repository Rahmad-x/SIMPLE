package com.example.simple

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.simple.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class SimpleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupReminderWorker()
        triggerImmediateCheck()
    }

    private fun setupReminderWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "return_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun triggerImmediateCheck() {
        // TRIGGER LANGSUNG SAAT APP DIBUKA (Hanya untuk testing)
        val immediateRequest = OneTimeWorkRequestBuilder<ReminderWorker>().build()
        WorkManager.getInstance(this).enqueue(immediateRequest)
    }
}
