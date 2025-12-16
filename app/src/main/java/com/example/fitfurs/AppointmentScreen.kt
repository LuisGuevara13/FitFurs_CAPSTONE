@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitfurs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppointmentScreen(
    navController: NavHostController,
    username: String,
    petId: String
) {
    val db = FirebaseFirestore.getInstance()

    var appointments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var petName by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }

    var selectedReasonFilter by remember { mutableStateOf("All") }
    var selectedDateFilter by remember { mutableStateOf("All") }

    val reasonOptions = listOf("All", "Vaccination", "Grooming", "Emergency", "Checkup", "Others")
    val dateOptions = listOf("All", "Today", "This Week", "This Month", "Past Week", "Past Month")

    // Load Pet Info
    DisposableEffect(username, petId) {
        val listener = db.collection("users")
            .document(username)
            .collection("pets")
            .document(petId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    petName = snapshot.getString("petName") ?: ""
                    mediaUrl = snapshot.getString("mediaUrl") ?: ""
                }
            }
        onDispose { listener.remove() }
    }

    // Load Appointments
    DisposableEffect(Unit) {
        val reg = db.collection("users")
            .document(username)
            .collection("pets")
            .document(petId)
            .collection("appointments")
            .addSnapshotListener { snapshot, _ ->
                appointments = snapshot?.documents?.map { doc ->
                    doc.data?.toMutableMap()?.apply { this["id"] = doc.id } ?: emptyMap()
                } ?: emptyList()
            }

        onDispose { reg.remove() }
    }

    // 🔥 AUTO-MOVE COMPLETED APPOINTMENTS INTO MEDICAL HISTORY
    LaunchedEffect(appointments) {
        appointments.forEach { appt ->
            val status = appt["status"]?.toString()?.lowercase() ?: ""
            val moved = appt["movedToHistory"] as? Boolean ?: false

            if (status == "completed" && !moved) {
                val apptId = appt["id"].toString()

                val historyData = mutableMapOf(
                    "appointmentId" to apptId,
                    "reason" to (appt["reason"] ?: "Unknown"),
                    "notes" to (appt["notes"] ?: ""),
                    "date" to (appt["date"] ?: ""),
                    "time" to (appt["time"] ?: ""),
                    "vet" to (appt["vet"] ?: ""),
                    "location" to (appt["location"] ?: ""),
                    "status" to "completed",
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("users")
                    .document(username)
                    .collection("pets")
                    .document(petId)
                    .collection("medicalHistory")
                    .document(apptId)
                    .set(historyData)

                db.collection("users")
                    .document(username)
                    .collection("pets")
                    .document(petId)
                    .collection("appointments")
                    .document(apptId)
                    .update("movedToHistory", true)
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Appointments", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("scheduleAppointment/$username/$petId") },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Appointment")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .background(Color.White)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Pet image + name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Pet Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp).clip(CircleShape)
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = petName.ifEmpty { "Loading..." },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Right: Logo image
                Image(
                    painter = painterResource(id = R.drawable.icon_logo), // replace with your logo drawable
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(20.dp))

            // FILTERS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DateFilterDropdown(
                    selected = selectedDateFilter,
                    options = dateOptions,
                    onSelected = { selectedDateFilter = it }
                )

                ReasonFilterDropdown(
                    selected = selectedReasonFilter,
                    options = reasonOptions,
                    onSelected = { selectedReasonFilter = it }
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Appointments",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )

            Spacer(Modifier.height(8.dp))

            // FILTERING
            val filteredAppointments = appointments.filter { a ->
                val status = a["status"]?.toString()?.lowercase() ?: ""
                val hidden = a["hidden"] as? Boolean ?: false

                if (hidden) return@filter false
                if (status == "cancelled" || status == "canceled") return@filter false

                val matchesReason =
                    selectedReasonFilter == "All" || a["reason"] == selectedReasonFilter

                val matchesDate = matchesDateFilter(a, selectedDateFilter)

                matchesReason && matchesDate
            }

            if (filteredAppointments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No appointments found",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            } else {
                LazyColumn {
                    items(filteredAppointments) { appointment ->
                        AppointmentItem(
                            appointment = appointment,
                            username = username,
                            petId = petId
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
