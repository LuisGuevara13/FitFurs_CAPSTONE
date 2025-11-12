package com.example.fitfurs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalTrackingScreen(
    navController: NavHostController,
    username: String,
    petId: String
) {
    val db = FirebaseFirestore.getInstance()
    var petName by remember { mutableStateOf("") }
    var appointmentList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    // Fetch pet name
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

    // Real-time updates for appointments
    LaunchedEffect(Unit) {
        db.collection("users").document(username)
            .collection("pets").document(petId)
            .collection("appointments")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    appointmentList = snapshot.documents.mapNotNull { it.data }
                }
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("scheduleAppointment/$username/$petId")
                },
                containerColor = Color(0xFF4A90E2)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Appointment", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 🐾 Pet Name from Firebase
            Text(petName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Medical Tracking", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))
            MedicalCard(
                title = "Vet Visit",
                rows = listOf("Last Exam" to "May 15, 2025", "Heartworm / Rx" to "Current")
            )

            Spacer(modifier = Modifier.height(16.dp))
            MedicalCard(
                title = "Upcoming",
                rows = listOf("June 18" to "10:30 AM - 4:00 PM")
            )

            Spacer(modifier = Modifier.height(16.dp))
            MedicalCard(
                title = "History",
                rows = listOf("Vet Visit" to "Nov 9, 2024", "Ear Infection" to "Prescribed Eardrops")
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Scheduled Appointments", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            LazyColumn {
                items(appointmentList) { appointment ->
                    AppointmentItem(appointment)
                }
            }
        }
    }
}

@Composable
fun MedicalCard(title: String, rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            rows.forEach { (label, value) ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label)
                    Text(value, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: Map<String, Any>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Date: ${appointment["date"]}")
            Text("Time: ${appointment["time"]}")
            Text("Reason: ${appointment["reason"]}")
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentScreen(navController: NavHostController, username: String, petId: String) {
    val db = FirebaseFirestore.getInstance()
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Set Appointment", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") })

        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            val appointment = hashMapOf(
                "date" to date,
                "time" to time,
                "reason" to reason
            )
            db.collection("users").document(username)
                .collection("pets").document(petId)
                .collection("appointments")
                .add(appointment)
                .addOnSuccessListener {
                    Toast.makeText(context, "Appointment Added!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error adding appointment", Toast.LENGTH_SHORT).show()
                }
        }) {
            Text("Save Appointment")
        }
    }
}

