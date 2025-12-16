@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitfurs

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import com.google.firebase.Timestamp
import java.util.*
import kotlin.time.Duration.Companion.hours

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MedicalTrackingScreen(
    navController: NavHostController,
    username: String,
    petId: String
) {
    val db = FirebaseFirestore.getInstance()

    var petName by remember { mutableStateOf<String?>(null) }
    var petImageUrl by remember { mutableStateOf<String?>(null) }

    var medicalHistory by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    val context = LocalContext.current

    /* ---------------- FILTERS ---------------- */

    var selectedReason by remember { mutableStateOf("All") }
    val reasonOptions = listOf("All", "Vaccination", "Grooming", "Checkup", "Emergency", "Others")

    var selectedDateFilter by remember { mutableStateOf("All") }
    val dateOptions = listOf("All", "Today", "This Week", "This Month", "Past Week", "Past Month")

    /* ---------------- SUPABASE PUBLIC URL ---------------- */
    fun getSupabasePublicUrl(fileName: String): String {
        return SupabaseClientInstance.storage.from("pet_media").publicUrl(fileName)
    }

    /* ---------------- LOAD PET PROFILE ---------------- */
    LaunchedEffect(username, petId) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users").document(username)
                .collection("pets").document(petId)
                .get()
                .await()

            petName = snapshot.getString("petName") ?: "Unknown Pet"
            val storedPath = snapshot.getString("mediaUrl")

            petImageUrl = when {
                storedPath.isNullOrBlank() -> null
                storedPath.startsWith("http") -> storedPath
                else -> getSupabasePublicUrl(storedPath)
            }

        } catch (_: Exception) {
            Toast.makeText(context, "Failed to load pet image", Toast.LENGTH_SHORT).show()
        }
    }

    /* ---------------- LOAD MEDICAL HISTORY ---------------- */
    DisposableEffect(username, petId) {
        val reg = db.collection("users").document(username)
            .collection("pets").document(petId)
            .collection("medicalHistory")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                medicalHistory = snapshot?.documents?.map { it.data ?: emptyMap() } ?: emptyList()
            }

        onDispose { reg.remove() }
    }

    /* ---------------- FILTER LOGIC ---------------- */
    val filteredHistory = medicalHistory.filter { record ->
        val reasonValue =
            (record["reason"] ?: record["notes"] ?: record["title"] ?: "").toString()

        val reasonMatch = when (selectedReason) {
            "All" -> true
            else -> reasonValue.contains(selectedReason, ignoreCase = true)
        }

        val dateMatch = matchesDateFilter(record, selectedDateFilter)

        reasonMatch && dateMatch
    }

    /* ---------------- UI ---------------- */
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Medical History", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .background(Color.White)
                .padding(16.dp)
        ) {

            /* ---------------- PET HEADER ---------------- */
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    // Left side: Pet image + name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (petImageUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(petImageUrl),
                                contentDescription = "Pet Image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = petName ?: "Unknown Pet",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    // Right side: Logo
                    Image(
                        painter = painterResource(id = R.drawable.icon_logo), // replace with your logo drawable
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            /* ---------------- FILTER HEADER ---------------- */
            item {
                Text("Filters", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    DateFilterDropdown(
                        selected = selectedDateFilter,
                        options = dateOptions,
                        onSelected = { selectedDateFilter = it }
                    )

                    ReasonFilterDropdown(
                        selected = selectedReason,
                        options = reasonOptions,
                        onSelected = { selectedReason = it }
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            /* ---------------- MEDICAL HISTORY ---------------- */
            item {
                Text(
                    "Medical History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(10.dp))
            }

            if (filteredHistory.isEmpty()) {
                item {
                    Text("No medical history matches your filters.", color = Color.Black)
                }
            } else {
                items(filteredHistory) { record ->

                    /* --- Extract Reason --- */
                    val reason = when {
                        record["reason"] != null -> record["reason"].toString()
                        record["notes"] != null -> record["notes"].toString()
                        record["title"] != null -> record["title"].toString()
                        else -> "No Reason"
                    }

                    /* --- Extract Date --- */
                    var date = record["date"]?.toString() ?: "Unknown Date"

                    if (record["timestamp"] != null && date == "Unknown Date") {
                        val ts = record["timestamp"] as Timestamp
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(ts.toDate())
                    }

                    /* --- Extract Time --- */
                    val time = record["time"]?.toString() ?: "Unknown Time"

                    MedicalCard(
                        title = reason, // TITLE = REASON
                        rows = listOf(
                            "Date" to date,
                            "Time" to time
                        ),
                        backgroundColor = Color.White,
                        titleColor = Color.Black
                    )

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}




/* -------------------- REASON FILTER DROPDOWN -------------------- */

@Composable
fun ReasonFilterDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text(selected, color = Color.Black)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {

            options.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onSelected(item)
                        expanded = false
                    },
                    text = { Text(item, color = Color.Black) }
                )
            }
        }
    }
}





/* -------------------- DATE FILTER LOGIC -------------------- */

fun matchesDateFilter(item: Map<String, Any>, filter: String): Boolean {
    if (filter == "All") return true

    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateString = item["date"]?.toString() ?: return false
    val date = format.parse(dateString) ?: return false

    val today = Calendar.getInstance()
    val cal = Calendar.getInstance().apply { time = date }

    return when (filter) {
        "Today" -> cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        "This Week" -> {
            val start = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            }
            cal.time >= start.time && cal.time <= today.time
        }

        "This Month" -> cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)

        "Past Week" -> {
            val start = Calendar.getInstance().apply {
                add(Calendar.WEEK_OF_YEAR, -1)
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            }
            val end = Calendar.getInstance().apply {
                add(Calendar.WEEK_OF_YEAR, -1)
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                add(Calendar.DAY_OF_YEAR, 6)
            }
            cal.time >= start.time && cal.time <= end.time
        }

        "Past Month" -> {
            val start = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val end = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            cal.time >= start.time && cal.time <= end.time
        }

        else -> true
    }
}

/* -------------------- MEDICAL CARD UI -------------------- */

@Composable
fun MedicalCard(
    title: String,
    rows: List<Pair<String, String>>,
    backgroundColor: Color,
    titleColor: Color
) {
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
                    Text(label, color = Color.Black.copy(alpha = 0.6f))
                    Text(value, fontWeight = FontWeight.Medium, color = Color.Black)
                }
            }
        }
    }
}

// APPOINTMENT ITEM UI WITH FIXED BUTTON LOGIC
@Composable
fun AppointmentItem(
    appointment: Map<String, Any>,
    username: String,
    petId: String
) {
    val context = LocalContext.current
    val FitFursBlack = Color(0xFF000000)
    val FitFursLightGray = Color(0xFFF7F7F7)
    val db = FirebaseFirestore.getInstance()

    var showCancelDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    val rawStatus = (appointment["status"]?.toString() ?: "").lowercase()
    val status = when (rawStatus) {
        "scheduled" -> "Scheduled"
        "pending" -> "Pending"
        "completed" -> "Completed"
        "cancelled", "canceled" -> "Cancelled"
        else -> "Unknown"
    }

    val docId = appointment["id"].toString()

    // CANCEL DIALOG
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Appointment") },
            text = { Text("Are you sure you want to cancel this appointment?") },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("users").document(username)
                        .collection("pets").document(petId)
                        .collection("appointments").document(docId)
                        .update("status", "Cancelled")
                        .addOnSuccessListener {
                            Toast.makeText(context, "Appointment cancelled", Toast.LENGTH_SHORT).show()
                        }

                    showCancelDialog = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No") }
            }
        )
    }

    // COMPLETE DIALOG
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Mark as Completed") },
            text = { Text("Add this appointment to medical history?") },
            confirmButton = {
                TextButton(onClick = {
                    markAppointmentCompleted(appointment, username, petId, context)
                    showCompleteDialog = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) { Text("No") }
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
                        "Pending" -> Color(0xFFFF9800)
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

            // BUTTON LOGIC
            when (status) {

                "Scheduled" -> {
                    Button(
                        onClick = { showCancelDialog = true },
                        colors = ButtonDefaults.buttonColors(FitFursBlack, Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Cancel Appointment", fontWeight = FontWeight.Bold) }
                }

                "Pending" -> {
                    Button(
                        onClick = { showCompleteDialog = true },
                        colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50), Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Mark as Completed", fontWeight = FontWeight.Bold) }
                }

                "Completed" -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(Color.Gray, Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Completed", fontWeight = FontWeight.Bold) }
                }

                "Cancelled" -> {
                    Button(
                        onClick = {
                            db.collection("users").document(username)
                                .collection("pets").document(petId)
                                .collection("appointments").document(docId)
                                .update("hidden", true)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                                }
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

    val date = appointment["date"]?.toString() ?: ""
    val time = appointment["time"]?.toString() ?: ""
    val reason = appointment["reason"]?.toString() ?: ""

    // MARK AS COMPLETED
    db.collection("users").document(username)
        .collection("pets").document(petId)
        .collection("appointments")
        .document(docId)
        .update("status", "Completed")

    // ADD TO MEDICAL HISTORY
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
@Composable
fun DateFilterDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.background(Color.White)
        ) {
            Text(selected, color = Color.Black)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onSelected(item)
                        expanded = false
                    },
                    text = { Text(item, color = Color.Black) }
                )
            }
        }
    }
}



