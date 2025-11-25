package com.example.fitfurs

import androidx.compose.foundation.ExperimentalFoundationApi
import android.widget.Toast
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

// ----------------------------
// Helpers: slots & Firestore
// ----------------------------

suspend fun getTakenSlots(date: String): List<String> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val snap = db.collection("appointments_admin")
            .whereEqualTo("date", date)
            .whereIn("status", listOf("Scheduled", "Pending"))
            .get()
            .await()
        snap.documents.mapNotNull { it.getString("time") }
    } catch (e: Exception) {
        emptyList()
    }
}

fun generateAllTimeSlots(): List<String> {
    return listOf(
        "09:00 AM", "10:00 AM", "11:00 AM",
        "12:00 PM", "01:00 PM", "02:00 PM",
        "03:00 PM", "04:00 PM", "05:00 PM"
    )
}

// ----------------------------
// ScheduleAppointmentScreen
// ----------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScheduleAppointmentScreen(navController: NavHostController, username: String, petId: String) {

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {

        val db = FirebaseFirestore.getInstance()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var date by remember { mutableStateOf("") }            // selected date (MM/dd/yyyy)
        var time by remember { mutableStateOf("") }            // selected time (e.g. "09:00 AM")
        var reason by remember { mutableStateOf("") }
        var navigateBack by remember { mutableStateOf(false) }

        // Slot selector state
        val takenSlots = remember { mutableStateListOf<String>() }
        val availableSlots = remember { mutableStateListOf<String>() }
        var showSlotSelector by remember { mutableStateOf(false) }
        var loadingSlots by remember { mutableStateOf(false) }

        val FitFursBlack = Color(0xFF000000)
        val calendar = Calendar.getInstance()

        // Date picker (Android's DatePickerDialog)
        val datePickerDialog = remember {
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    // store as MM/dd/yyyy to match other code
                    date = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year)
                    // when date changes, refresh slots
                    scope.launch {
                        loadingSlots = true
                        val taken = getTakenSlots(date)
                        takenSlots.clear(); takenSlots.addAll(taken)
                        val all = generateAllTimeSlots()
                        availableSlots.clear(); availableSlots.addAll(all.filter { it !in taken })
                        loadingSlots = false
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Set Appointment",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitFursBlack
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = FitFursBlack
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // DATE
                item {
                    Text(
                        "Date",
                        color = FitFursBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { datePickerDialog.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = FitFursBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Text(if (date.isEmpty()) "Select Date" else "Date: $date", color = Color.White)
                    }
                }

                // TIME (slot selector)
                item {
                    Text(
                        "Time",
                        color = FitFursBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (date.isBlank()) {
                                    Toast.makeText(context, "Please select a date first", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                // ensure latest slots are loaded and show dialog
                                scope.launch {
                                    loadingSlots = true
                                    val taken = getTakenSlots(date)
                                    takenSlots.clear(); takenSlots.addAll(taken)
                                    val all = generateAllTimeSlots()
                                    availableSlots.clear(); availableSlots.addAll(all.filter { it !in taken })
                                    loadingSlots = false
                                    showSlotSelector = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FitFursBlack),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (time.isEmpty()) "Select Time" else time, color = Color.White)
                        }

                        // Quick visual of availability count
                        OutlinedButton(
                            onClick = { /* maybe show additional info */ },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text(
                                text = if (loadingSlots) "Loading..." else "${availableSlots.size} free • ${takenSlots.size} taken",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                }

                // REASON
                item {
                    Text(
                        "Reason",
                        color = FitFursBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("Vet check-up, vaccination...") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }

                // SAVE
                item {
                    Button(
                        onClick = {
                            if (date.isBlank() || time.isBlank() || reason.isBlank()) {
                                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Re-check on save to avoid race condition (double-booking)
                            scope.launch {
                                try {
                                    val taken = getTakenSlots(date)
                                    if (time in taken) {
                                        Toast.makeText(context, "Selected slot is already taken — choose another.", Toast.LENGTH_LONG).show()
                                        // Refresh available/taken lists
                                        takenSlots.clear(); takenSlots.addAll(taken)
                                        val all = generateAllTimeSlots()
                                        availableSlots.clear(); availableSlots.addAll(all.filter { it !in taken })
                                        return@launch
                                    }

                                    // proceed to add appointment under user's pet -> add() returns DocumentReference
                                    val appointmentData = hashMapOf(
                                        "date" to date,
                                        "time" to time,
                                        "reason" to reason,
                                        "status" to "Scheduled",
                                        "timestamp" to System.currentTimeMillis()
                                    )

                                    val userAppointmentsRef = db.collection("users").document(username)
                                        .collection("pets").document(petId)
                                        .collection("appointments")

                                    val docRef = userAppointmentsRef.add(appointmentData).await()

                                    // Create matching admin document with same id
                                    val adminData = hashMapOf(
                                        "user" to username,
                                        "petId" to petId,
                                        "date" to date,
                                        "time" to time,
                                        "reason" to reason,
                                        "status" to "Scheduled",
                                        "timestamp" to System.currentTimeMillis()
                                    )

                                    db.collection("appointments_admin").document(docRef.id).set(adminData).await()

                                    Toast.makeText(context, "Appointment Added!", Toast.LENGTH_SHORT).show()
                                    navigateBack = true
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error adding appointment: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FitFursBlack,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Save Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // navigate back after saved
        if (navigateBack) {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        }

        // Slot selector dialog (Available vs Taken)
        if (showSlotSelector) {
            AlertDialog(
                onDismissRequest = { showSlotSelector = false },
                title = {
                    Text(
                        "Select a time on $date",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                text = {
                    Column {

                        if (loadingSlots) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                color = Color.Black
                            )
                        } else {

                            // ------- AVAILABLE --------
                            Text("Available", fontWeight = FontWeight.Bold, color = Color.Black)

                            if (availableSlots.isEmpty()) {
                                Text("No available slots", color = Color.Red)
                            } else {
                                Column {
                                    availableSlots.forEach { slot ->
                                        Button(
                                            onClick = {
                                                time = slot
                                                showSlotSelector = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Black,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(slot)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // ------- TAKEN --------
                            Text("Taken", fontWeight = FontWeight.Bold, color = Color.Black)

                            if (takenSlots.isEmpty()) {
                                Text("No taken slots", color = Color.Gray)
                            } else {
                                Column {
                                    takenSlots.forEach { slot ->
                                        Text(slot, color = Color.Red, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSlotSelector = false }) {
                        Text("Close", color = Color.Black)
                    }
                },
                containerColor = Color(0xFFF5F5F5) // clean gray/white background
            )
        }
    }
}
