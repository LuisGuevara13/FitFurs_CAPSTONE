package com.example.fitfurs

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fitfurs.R
import com.example.fitfurs.NotificationReceiver
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetActivityScreen(navController: NavHostController, username: String, petId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var petName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val mealList = remember { mutableStateListOf<Pair<String, Map<String, Any>>>() }
    val exerciseList = remember { mutableStateListOf<Pair<String, Map<String, Any>>>() }

    // --- Load Pet Info ---
    DisposableEffect(username, petId) {
        val registration = db.collection("users")
            .document(username)
            .collection("pets")
            .document(petId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load pet: ${error.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addSnapshotListener
                }
                petName = snapshot?.getString("petName") ?: "Unknown Pet"
                isLoading = false
            }
        onDispose { registration.remove() }
    }

    // --- Load Meals ---
    DisposableEffect(Unit) {
        val mealReg = db.collection("users").document(username)
            .collection("pets").document(petId)
            .collection("mealtime")
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null) {
                    mealList.clear()
                    mealList.addAll(snap.documents.mapNotNull { doc -> doc.id to (doc.data ?: emptyMap()) })
                }
            }
        onDispose { mealReg.remove() }
    }

    // --- Load Exercises ---
    DisposableEffect(Unit) {
        val exReg = db.collection("users")
            .document(username)
            .collection("pets")
            .document(petId.lowercase())
            .collection("exercise")
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null) {
                    exerciseList.clear()
                    exerciseList.addAll(snap.documents.mapNotNull { doc -> doc.id to (doc.data ?: emptyMap()) })
                }
            }
        onDispose { exReg.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = petName?.let { "$it's Activity" } ?: "Loading...", style = MaterialTheme.typography.titleLarge, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_activity/$username/$petId") },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(R.drawable.dog1),
                    contentDescription = "Pet Image",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                Text(petName ?: "Unknown Pet", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(24.dp))

                // --- Meal Card ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Meal Plans", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))
                        if (mealList.isEmpty()) Text("No meals yet.", color = Color.Gray)
                        else {
                            mealList.forEach { (mealId, meal) ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column { Text("🍽 ${meal["meal"]} - ${meal["time"]} (${meal["amount"]})") }
                                    Button(
                                        onClick = {
                                            db.collection("users").document(username)
                                                .collection("pets").document(petId)
                                                .collection("mealtime").document(mealId)
                                                .delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Meal confirmed and removed!", Toast.LENGTH_SHORT).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                    ) { Text("Confirm Fed", color = Color.White, fontSize = 12.sp) }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- Exercise Card ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Exercises", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))
                        if (exerciseList.isEmpty()) Text("No exercises yet.", color = Color.Gray)
                        else {
                            exerciseList.forEach { (exId, ex) ->
                                var remainingMinutes by remember { mutableStateOf(0) }
                                var isRunning by remember { mutableStateOf(false) }
                                var isPaused by remember { mutableStateOf(false) }
                                var showConfirm by remember { mutableStateOf(false) }

                                Column(Modifier.fillMaxWidth()) {
                                    Text("🏃 ${ex["recommendation"] ?: "Exercise"}")
                                    Spacer(Modifier.height(6.dp))

                                    if (isRunning) Text("⏱ $remainingMinutes min remaining", color = Color.Red, fontWeight = FontWeight.SemiBold)
                                    else if (showConfirm) Text("✅ Exercise finished!", color = Color.Green, fontWeight = FontWeight.Bold)

                                    Spacer(Modifier.height(6.dp))

                                    when {
                                        isRunning && !isPaused -> {
                                            Button(
                                                onClick = { isPaused = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                            ) { Text("Pause", color = Color.White, fontSize = 12.sp) }
                                        }
                                        isRunning && isPaused -> {
                                            Button(
                                                onClick = { isPaused = false },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                            ) { Text("Resume", color = Color.White, fontSize = 12.sp) }
                                        }
                                        showConfirm -> {
                                            Button(
                                                onClick = {
                                                    db.collection("users").document(username)
                                                        .collection("pets").document(petId.lowercase())
                                                        .collection("exercise").document(exId)
                                                        .update("status", "Completed")
                                                    Toast.makeText(context, "Exercise confirmed!", Toast.LENGTH_SHORT).show()
                                                    showConfirm = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                            ) { Text("Confirm Completion", color = Color.White, fontSize = 12.sp) }
                                        }
                                        else -> {
                                            Button(
                                                onClick = {
                                                    val durationFromDb = ex["duration"]
                                                    val durationMinutes = when (durationFromDb) {
                                                        is Long -> durationFromDb.toInt()
                                                        is Double -> durationFromDb.toInt()
                                                        is String -> durationFromDb.toIntOrNull() ?: 1
                                                        else -> 1
                                                    }

                                                    remainingMinutes = durationMinutes
                                                    isRunning = true
                                                    isPaused = false

                                                    scope.launch {
                                                        while (remainingMinutes > 0) {
                                                            if (!isPaused) remainingMinutes--
                                                            delay(60_000) // delay 1 minute
                                                        }
                                                        isRunning = false
                                                        showConfirm = true
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                            ) { Text("Start Exercise", color = Color.White, fontSize = 12.sp) }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Alarm + Time Utility ---
fun setPetAlarm(context: Context, title: String, message: String, triggerTime: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("message", message)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                Toast.makeText(context, "Cannot schedule exact alarm", Toast.LENGTH_SHORT).show()
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        Toast.makeText(context, "Alarm permission required", Toast.LENGTH_SHORT).show()
    }
}

fun parseTimeToMillis(timeStr: String): Long {
    return try {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = sdf.parse(timeStr)
        val calendar = Calendar.getInstance().apply {
            time = date!!
            val now = Calendar.getInstance()
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
        }
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendar.timeInMillis
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}






