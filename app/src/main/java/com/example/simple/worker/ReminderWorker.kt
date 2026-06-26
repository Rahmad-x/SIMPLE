package com.example.simple.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.simple.domain.model.TransactionStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        Log.d("ReminderWorker", "Worker execution started manually triggered/scheduled")
        
        // DEBUG NOTIFICATION: Always show this to prove the worker is running
        showNotification("Worker is active! Checking for due items...")

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        
        if (userId == null) {
            Log.d("ReminderWorker", "User not logged in, skipping.")
            return ListenableWorker.Result.success()
        }
        
        val db = FirebaseFirestore.getInstance()

        // Periksa barang yang jatuh tempo dalam rentang 2 hari ke depan (untuk testing)
        val calendar = Calendar.getInstance()
        val startOfToday = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 2)
        val endOfNextTwoDays = calendar.timeInMillis

        Log.d("ReminderWorker", "Checking between $startOfToday and $endOfNextTwoDays for user $userId")

        return try {
            val snapshot = db.collectionGroup("transactions")
                .whereEqualTo("userId", userId)
                .whereIn("status", listOf(TransactionStatus.APPROVED.name, TransactionStatus.BORROWED.name))
                .whereGreaterThanOrEqualTo("dueDate", startOfToday)
                .whereLessThanOrEqualTo("dueDate", endOfNextTwoDays)
                .get()
                .await()

            Log.d("ReminderWorker", "Found ${snapshot.size()} pending returns")

            snapshot.documents.forEach { doc ->
                val itemName = doc.getString("itemName") ?: "Barang"
                Log.d("ReminderWorker", "Showing notification for $itemName")
                showNotification(itemName)
            }
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Error in worker", e)
            ListenableWorker.Result.retry()
        }
    }

    private fun showNotification(itemName: String) {
        val channelId = "borrow_reminder"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Pengingat Pengembalian", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Batas Waktu Pengembalian")
            .setContentText("Besok adalah batas waktu pengembalian barang: $itemName")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            Log.e("ReminderWorker", "SecurityException: No notification permission", e)
        }
    }
}
