package com.example.fitfurs

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(navController: NavHostController, username: String, petId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var meal by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showVetCalc by remember { mutableStateOf(false) }

    val petRef = db.collection("users")
        .document(username)
        .collection("pets")
        .document(petId)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedBorderColor = Color.Black,
        unfocusedBorderColor = Color.Black,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Black,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        cursorColor = Color.Black
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Activity") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black // <-- Back arrow black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White // <-- Top bar white
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // 🥣 Add Meal Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Add Meal", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = meal,
                        onValueChange = { meal = it },
                        label = { Text("Meal Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Spacer(Modifier.height(8.dp))

                    // ⏰ Time Picker Button
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val cal = Calendar.getInstance()
                                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    cal.set(Calendar.MINUTE, minute)
                                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                                    time = sdf.format(cal.time)
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (time.isEmpty()) "Pick Time" else "Time: $time", color = Color.White)
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (e.g. 1 cup)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Spacer(Modifier.height(16.dp))

                    // Add Meal Button
                    Button(
                        onClick = {
                            if (meal.isNotEmpty() && time.isNotEmpty() && amount.isNotEmpty()) {

                                val newMeal = mapOf(
                                    "meal" to meal,
                                    "time" to time,
                                    "amount" to amount,
                                    "timestamp" to System.currentTimeMillis()
                                )

                                // Save to Firestore
                                petRef.collection("mealtime")
                                    .add(newMeal)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Meal saved!", Toast.LENGTH_SHORT).show()

                                        // Schedule notification AFTER successful save
                                        try {
                                            scheduleMealNotification(context, meal, time, username, petId)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(context, "Error scheduling notification.", Toast.LENGTH_SHORT).show()
                                        }

                                        // Clear input fields
                                        meal = ""
                                        time = ""
                                        amount = ""
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed to save meal: ${e.message}", Toast.LENGTH_LONG).show()
                                        e.printStackTrace()
                                    }

                            } else {
                                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Add Meal", color = Color.White)
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { showVetCalc = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("Food Calculator", color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 🩺 Exercise Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Exercise", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            petRef.get().addOnSuccessListener { document ->
                                if (document.exists()) {

                                    val breed = document.getString("breed") ?: "Unknown"
                                    val weight = document.getString("weight")?.toDoubleOrNull() ?: 0.0

                                    val recommendation = when {
                                        weight < 7 -> "Short walks (10–15 mins twice daily) for $breed"
                                        weight in 7.0..15.0 -> "Moderate walks (20–30 mins twice daily) for $breed"
                                        else -> "Active play & long walks (30–45 mins twice daily) for $breed"
                                    }

                                    val duration = when {
                                        weight < 7 -> 10
                                        weight in 8.0..15.0 -> 25
                                        else -> 40
                                    }

                                    val data = mapOf(
                                        "breed" to breed,
                                        "weight" to weight,
                                        "recommendation" to recommendation,
                                        "duration" to duration,
                                        "timestamp" to System.currentTimeMillis()
                                    )

                                    petRef.collection("exercise").add(data)
                                    petRef.update("petBMI", (weight / 5).toString())

                                    Toast.makeText(context, "Exercise recommendation saved!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Exercise", color = Color.White)
                    }
                }
            }
        }

        if (showVetCalc) {
            AlertDialog(
                onDismissRequest = { showVetCalc = false },
                confirmButton = {},
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                    ) {
                        VetStylePetFoodCalculatorScreen(onBack = { showVetCalc = false })
                    }
                }
            )
        }
    }
}



/**
 * Schedule meal notification
 */
fun scheduleMealNotification(
    context: Context,
    mealName: String,
    timeString: String,
    username: String,
    petId: String
) {
    try {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = sdf.parse(timeString)

        if (date != null) {
            val cal = Calendar.getInstance().apply {
                time = date
                val now = Calendar.getInstance()
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(context, "Enable 'Exact alarms' permission.", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    return
                }
            }

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("meal_name", mealName)
                putExtra("username", username)
                putExtra("petId", petId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )

            Toast.makeText(context, "Reminder set for $timeString", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Invalid time format.", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error scheduling alarm.", Toast.LENGTH_SHORT).show()
    }
}






