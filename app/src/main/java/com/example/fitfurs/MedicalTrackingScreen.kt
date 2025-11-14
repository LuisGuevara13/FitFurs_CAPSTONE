package com.example.fitfurs

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalTrackingScreen(
    navController: NavHostController,
    username: String,
    petId: String
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val FitFursBlack = Color(0xFF000000)
    val FitFursGrayText = Color(0xFF4A4A4A)
    val FitFursLightGray = Color(0xFFF7F7F7)
    val FitFursBlue = Color(0xFF4A90E2)

    var petName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var appointmentList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var medicalHistory by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // --- Load pet name ---
    DisposableEffect(username, petId) {
        val reg = db.collection("users").document(username)
            .collection("pets").document(petId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                petName = snapshot?.getString("petName") ?: "Unknown Pet"
                isLoading = false
            }
        onDispose { reg.remove() }
    }

    // --- Load appointments ---
    DisposableEffect(username, petId) {
        val reg2 = db.collection("users").document(username)
            .collection("pets").document(petId)
            .collection("appointments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                appointmentList = snapshot.documents.mapNotNull { it.data }
            }
        onDispose { reg2.remove() }
    }

    // --- Load medical history ---
    DisposableEffect(username, petId) {
        val reg3 = db.collection("users").document(username)
            .collection("pets").document(petId)
            .collection("medicalHistory")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                medicalHistory = snapshot.documents.mapNotNull { it.data }
            }
        onDispose { reg3.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        petName ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = FitFursBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FitFursBlack)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("scheduleAppointment/$username/$petId") },
                containerColor = FitFursBlack,
                shape = RoundedCornerShape(16.dp),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Appointment", tint = Color.White)
            }
        },
        containerColor = FitFursLightGray
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FitFursBlack)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Static cards ---
                item {
                    Text(
                        "Medical Tracking",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitFursBlack
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MedicalCard(
                        title = "Vet Visit",
                        rows = listOf("Last Exam" to "May 15, 2025", "Heartworm / Rx" to "Current"),
                        backgroundColor = Color.White,
                        titleColor = FitFursBlack
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    MedicalCard(
                        title = "Upcoming",
                        rows = listOf("June 18" to "10:30 AM - 4:00 PM"),
                        backgroundColor = Color.White,
                        titleColor = FitFursBlack
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- Medical History ---
                item {
                    Text(
                        "Medical History",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FitFursBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (medicalHistory.isEmpty()) {
                        Text("No medical history yet.", color = FitFursGrayText)
                    } else {
                        medicalHistory.forEach { record ->
                            MedicalCard(
                                title = record["title"]?.toString() ?: "Unknown",
                                rows = listOf(
                                    "Date" to (record["date"]?.toString() ?: "-"),
                                    "Notes" to (record["notes"]?.toString() ?: "-")
                                ),
                                backgroundColor = Color.White,
                                titleColor = FitFursBlack
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- Scheduled Appointments ---
                item {
                    Text(
                        "Scheduled Appointments",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FitFursBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (appointmentList.isEmpty()) {
                    item { Text("No appointments yet.", color = FitFursGrayText) }
                } else {
                    items(appointmentList) { appointment ->
                        AppointmentItem(
                            appointment = appointment,
                            username = username,
                            petId = petId
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalCard(title: String, rows: List<Pair<String, String>>, backgroundColor: Color, titleColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = titleColor)
            Spacer(modifier = Modifier.height(8.dp))
            rows.forEach { (label, value) ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = Color.Gray)
                    Text(value, fontWeight = FontWeight.Medium, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(
    appointment: Map<String, Any>,
    username: String,
    petId: String
) {
    val context = LocalContext.current
    val FitFursBlack = Color(0xFF000000)
    val FitFursLightGray = Color(0xFFF7F7F7)

    var showDialog by remember { mutableStateOf(false) }

    // Parse appointment date + time
    val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())
    val appointmentDateTime = try {
        sdf.parse("${appointment["date"]} ${appointment["time"]}")
    } catch (e: Exception) {
        null
    }

    val currentTime = System.currentTimeMillis()
    val status = when {
        appointment["status"]?.toString() == "cancelled" -> "Cancelled"
        appointmentDateTime == null -> "Unknown"
        currentTime < appointmentDateTime.time -> "Scheduled"
        currentTime in appointmentDateTime.time..(appointmentDateTime.time + 2 * 3600_000) -> "Pending"
        else -> "Cancelled" // automatically after 2 hours
    }

    // Update Firestore automatically if needed
    LaunchedEffect(status, appointment["id"]) {
        val db = FirebaseFirestore.getInstance()
        val docId = appointment["id"].toString()
        if ((status == "Pending" || status == "Cancelled") &&
            appointment["status"] != status
        ) {
            db.collection("users").document(username)
                .collection("pets").document(petId)
                .collection("appointments")
                .document(docId)
                .update("status", status)
                .addOnSuccessListener { Log.d("AppointmentStatus", "$docId updated to $status") }
        }
    }

    if (showDialog && status == "Scheduled") {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cancel Appointment") },
            text = { Text("Are you sure you want to cancel this appointment?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()
                        val docId = appointment["id"].toString()
                        db.collection("users").document(username)
                            .collection("pets").document(petId)
                            .collection("appointments")
                            .document(docId)
                            .update("status", "cancelled")
                            .addOnSuccessListener {
                                Toast.makeText(context, "Appointment cancelled", Toast.LENGTH_SHORT).show()
                            }
                        showDialog = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("No") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FitFursLightGray),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${appointment["date"]}", color = FitFursBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Status: $status", color = if (status == "Cancelled") Color.Red else Color.Blue, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Time", color = Color.Gray, fontSize = 14.sp)
                    Text(appointment["time"].toString(), fontWeight = FontWeight.Medium, color = FitFursBlack)
                }
                Column {
                    Text("Reason", color = Color.Gray, fontSize = 14.sp)
                    Text(appointment["reason"].toString(), color = FitFursBlack)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (status == "Scheduled") {
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FitFursBlack,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Cancel Appointment", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(status, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}





