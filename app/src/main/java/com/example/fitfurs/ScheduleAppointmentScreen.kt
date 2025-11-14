package com.example.fitfurs

import androidx.compose.foundation.ExperimentalFoundationApi
import android.app.TimePickerDialog
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
import scheduleAppointmentNotification
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScheduleAppointmentScreen(navController: NavHostController, username: String, petId: String) {

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {

        val db = FirebaseFirestore.getInstance()
        val context = LocalContext.current

        var date by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("") }
        var reason by remember { mutableStateOf("") }
        var navigateBack by remember { mutableStateOf(false) }

        val FitFursBlack = Color(0xFF000000)
        val calendar = Calendar.getInstance()

        // --- DATE PICKER ---
        val datePickerDialog = remember {
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    date = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        // --- TIME PICKER ---
        val timePickerDialog = remember {
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val ampm = if (hourOfDay >= 12) "PM" else "AM"
                    val hour12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                    time = String.format("%02d:%02d %s", hour12, minute, ampm)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
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

                // --- DATE BUTTON ---
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

                // --- TIME BUTTON ---
                item {
                    Text(
                        "Time",
                        color = FitFursBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { timePickerDialog.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = FitFursBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Text(if (time.isEmpty()) "Select Time" else "Time: $time", color = Color.White)
                    }
                }

                // --- REASON FIELD ---
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

                // --- SAVE BUTTON ---
                item {
                    Button(
                        onClick = {
                            if (date.isNotBlank() && time.isNotBlank() && reason.isNotBlank()) {

                                val appointment = hashMapOf(
                                    "date" to date,
                                    "time" to time,
                                    "reason" to reason,
                                    "timestamp" to System.currentTimeMillis()
                                )

                                db.collection("users").document(username)
                                    .collection("pets").document(petId)
                                    .collection("appointments")
                                    .add(appointment)
                                    .addOnSuccessListener { docRef ->
                                        // Schedule notification using the appointmentId
                                        scheduleAppointmentNotification(
                                            context = context,
                                            date = date,
                                            time = time,
                                            reason = reason,
                                            username = username,
                                            petId = petId,
                                            appointmentId = docRef.id // <--- pass Firestore document ID
                                        )

                                        Toast.makeText(context, "Appointment Added!", Toast.LENGTH_SHORT).show()
                                        navigateBack = true
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error adding appointment", Toast.LENGTH_SHORT).show()
                                    }

                            } else {
                                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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

        // Navigate safely after save
        if (navigateBack) {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        }
    }
}
