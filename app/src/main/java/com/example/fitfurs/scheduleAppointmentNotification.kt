import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.fitfurs.NotificationReceiverMed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun scheduleAppointmentNotification(
    context: Context,
    date: String,          // "MM/dd/yyyy"
    time: String,          // "hh:mm a"
    reason: String,        // appointment reason
    username: String,
    petId: String,
    appointmentId: String
) {
    // Parse date + time into a Date object
    val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
    val appointmentDate: Date = try {
        sdf.parse("$date $time") ?: return
    } catch (e: Exception) {
        e.printStackTrace()
        return
    }

    // Create a Calendar instance for the alarm
    val calendar = Calendar.getInstance().apply {
        this.time = appointmentDate
    }

    // Prepare the intent for the broadcast
    val intent = Intent(context, NotificationReceiverMed::class.java).apply {
        putExtra("appointment_reason", reason)
        putExtra("username", username)
        putExtra("petId", petId)
        putExtra("appointmentId", appointmentId)
    }

    // Create PendingIntent
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        calendar.timeInMillis.toInt(), // unique ID for each appointment
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Schedule the exact alarm safely
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // fallback: schedule normally if exact alarms not allowed
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    } catch (e: SecurityException) {
        Log.e("ScheduleNotification", "Cannot schedule exact alarm", e)
    }
}
