package com.example.fitfurs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore

class NotificationReceiverMed : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reason = intent.getStringExtra("appointment_reason") ?: "Your Pet"
        val username = intent.getStringExtra("username") ?: return
        val petId = intent.getStringExtra("petId") ?: return
        val appointmentId = intent.getStringExtra("appointmentId") ?: return

        val channelId = "pet_channel"

        // 1️⃣ Create notification channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pet Appointments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for medical appointments"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // 2️⃣ Build and show notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // your app icon
            .setContentTitle("🐾 FitFurs Reminder")
            .setContentText("It's time for: $reason")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), notification)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        // 3️⃣ Update Firestore appointment status to "Pending"
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(username)
            .collection("pets")
            .document(petId)
            .collection("appointments")
            .document(appointmentId)
            .update("status", "Pending")
            .addOnSuccessListener {
                Log.d("NotificationReceiverMed", "Appointment status updated to Pending")
            }
            .addOnFailureListener {
                Log.e("NotificationReceiverMed", "Failed to update appointment status", it)
            }
    }
}
