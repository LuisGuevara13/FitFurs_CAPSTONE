package com.example.fitfurs

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
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
    val FitFursLightGray = Color(0xFFF7F7F7)
    val FitFursGrayText = Color(0xFF4A4A4A)

    var petName by remember { mutableStateOf<String?>(null) }
    var petImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var appointmentList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var medicalHistory by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // --- Load pet name + picture ---
    DisposableEffect(username, petId) {
        val reg = db.collection("users").document(username)
            .collection("pets").document(petId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                petName = snapshot?.getString("petName") ?: "Unknown Pet"

                val mediaUrlRaw = snapshot?.getString("mediaUrl")
                petImageUrl = resolvePetImageUrl(mediaUrlRaw)

                isLoading = false
            }
        onDispose { reg.remove() }
    }

    // --- Load appointments (with hidden filter) ---
    DisposableEffect(username, petId) {
        val reg2 = db.collection("users").document(username)
            .collection("pets").document(petId)
            .collection("appointments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                appointmentList = snapshot.documents.map { doc ->
                    doc.data?.toMutableMap()?.apply { this["id"] = doc.id } ?: emptyMap()
                }.filter { it["hidden"] != true }
            }
        onDispose { reg2.remove() }
    }

    // --- Load medical history (FIXED) ---
    DisposableEffect(username, petId) {
        val reg3 = db.collection("users").document(username)
            .collection("pets").document(petId)
            .collection("medicalHistory")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                medicalHistory = snapshot.documents.map { doc ->
                    doc.data?.toMutableMap()?.apply { this["id"] = doc.id } ?: emptyMap()
                }
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
                Icon(Icons.Default.Add, contentDescription = "Add Appointment")
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

                // --- PET HEADER ---
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (petImageUrl != null) {
                            AsyncImage(
                                model = petImageUrl,
                                contentDescription = "Pet Image",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.dog1),
                                contentDescription = "Default Pet Image",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = petName ?: "Unknown Pet",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = FitFursBlack
                            )
                            Text("Medical Tracking", color = FitFursGrayText)
                        }
                    }
                }

                // --- MEDICAL HISTORY TITLE ---
                item {
                    Text(
                        "Medical History",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FitFursBlack
                    )
                }

                // --- MEDICAL HISTORY LIST ---
                if (medicalHistory.isEmpty()) {
                    item { Text("No medical history yet.", color = FitFursGrayText) }
                } else {
                    items(medicalHistory) { record ->
                        MedicalCard(
                            title = record["title"]?.toString() ?: "Unknown",
                            rows = listOf(
                                "Date" to (record["date"]?.toString() ?: "-"),
                                "Notes" to (record["notes"]?.toString() ?: "-")
                            ),
                            backgroundColor = Color.White,
                            titleColor = FitFursBlack
                        )
                    }
                }

                // --- APPOINTMENTS TITLE ---
                item {
                    Text(
                        "Scheduled Appointments",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FitFursBlack
                    )
                }

                // --- APPOINTMENTS LIST ---
                if (appointmentList.isEmpty()) {
                    item { Text("No appointments yet.", color = FitFursGrayText) }
                } else {
                    items(appointmentList) { appointment ->
                        AppointmentItem(appointment, username, petId)
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

    // Normalize
    val rawStatus = (appointment["status"]?.toString() ?: "").lowercase()
    val status = when (rawStatus) {
        "scheduled" -> "Scheduled"
        "pending" -> "Pending"
        "completed" -> "Completed"
        "cancelled", "canceled" -> "Cancelled"
        else -> "Unknown"
    }

    val docId = appointment["id"].toString()
    val db = FirebaseFirestore.getInstance()

    // Cancel dialog
    if (showDialog && status == "Scheduled") {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cancel Appointment") },
            text = { Text("Are you sure you want to cancel this appointment?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        db.collection("users").document(username)
                            .collection("pets").document(petId)
                            .collection("appointments").document(docId)
                            .update("status", "Cancelled")

                        Toast.makeText(context, "Appointment cancelled", Toast.LENGTH_SHORT).show()
                        showDialog = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("No") }
            }
        )
    }

    // UI
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
                Text(
                    "${appointment["date"]}",
                    color = FitFursBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Status: $status",
                    color = when (status) {
                        "Cancelled" -> Color.Red
                        "Completed" -> Color.Gray
                        else -> Color.Blue
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Time", color = Color.Gray, fontSize = 14.sp)
                    Text(appointment["time"].toString(), fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Reason", color = Color.Gray, fontSize = 14.sp)
                    Text(appointment["reason"].toString())
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BUTTON LOGIC FIXED ---
            when (status) {
                "Scheduled" -> {
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(FitFursBlack, Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Cancel Appointment", fontWeight = FontWeight.Bold) }
                }

                "Pending" -> {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Already completed", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50), Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Mark as Completed", fontWeight = FontWeight.Bold) }
                }

                "Completed" -> {
                    Button(
                        onClick = {
                            markAppointmentCompleted(appointment, username, petId, context)
                        },
                        colors = ButtonDefaults.buttonColors(Color.Gray, Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Completed", fontWeight = FontWeight.Bold)
                    }
                }

                "Cancelled" -> {
                    Button(
                        onClick = {
                            db.collection("users").document(username)
                                .collection("pets").document(petId)
                                .collection("appointments").document(docId)
                                .update("hidden", true)
                        },
                        colors = ButtonDefaults.buttonColors(Color.Red, Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Remove", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun markAppointmentCompleted(
    appointment: Map<String, Any>,
    username: String,
    petId: String,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()
    val docId = appointment["id"].toString()

    // Fix: safely extract fields so they never become null
    val date = appointment["date"]?.toString() ?: ""
    val time = appointment["time"]?.toString() ?: ""
    val reason = appointment["reason"]?.toString() ?: ""

    // Update status
    db.collection("users").document(username)
        .collection("pets").document(petId)
        .collection("appointments")
        .document(docId)
        .update("status", "Completed")

    // Medical History entry
    val medicalData = mapOf(
        "title" to "Appointment Completed",
        "date" to date,
        "time" to time,
        "notes" to reason,
        "appointmentId" to docId,
        "petId" to petId,
        "timestamp" to System.currentTimeMillis()
    )

    db.collection("users").document(username)
        .collection("pets").document(petId)
        .collection("medicalHistory")
        .add(medicalData)
        .addOnSuccessListener {
            Toast.makeText(context, "Added to medical history", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to add medical history: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}
