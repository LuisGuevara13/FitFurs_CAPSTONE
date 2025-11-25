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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import io.github.jan.supabase.storage.storage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Helper to generate public URL correctly ---
fun getSupabasePublicUrl(fileName: String): String {
    return SupabaseClientInstance.storage
        .from("pet_media")
        .publicUrl(fileName)
}

// 🔥 Updated helper
fun resolvePetImageUrl(mediaUrl: String?): String? {
    if (mediaUrl.isNullOrBlank()) return null

    return if (mediaUrl.startsWith("http")) mediaUrl else getSupabasePublicUrl(mediaUrl)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetActivityScreen(navController: NavHostController, username: String, petId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var petName by remember { mutableStateOf<String?>(null) }
    var petImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val mealList = remember { mutableStateListOf<Pair<String, Map<String, Any>>>() }
    val exerciseList = remember { mutableStateListOf<Pair<String, Map<String, Any>>>() }

    // --- Load Pet Info ---
    DisposableEffect(username, petId) {
        val reg = db.collection("users").document(username)
            .collection("pets").document(petId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                petName = snapshot?.getString("petName") ?: "Unknown Pet"
                petImageUrl = resolvePetImageUrl(snapshot?.getString("mediaUrl"))

                isLoading = false
            }
        onDispose { reg.remove() }
    }

    // --- Load Meals ---
    DisposableEffect(Unit) {
        val mealReg = db.collection("users").document(username)
            .collection("pets").document(petId)
            .collection("mealtime")
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null) {
                    mealList.clear()
                    mealList.addAll(
                        snap.documents
                            .filter { it.getBoolean("hidden") != true }  // Hide flagged items
                            .map { it.id to (it.data ?: emptyMap()) }
                    )
                }
            }
        onDispose { mealReg.remove() }
    }

    // --- Load Exercises ---
    DisposableEffect(Unit) {
        val exReg = db.collection("users").document(username)
            .collection("pets").document(petId)
            .collection("exercise")
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null) {
                    exerciseList.clear()
                    exerciseList.addAll(
                        snap.documents
                            .filter { it.getBoolean("hidden") != true }  // Hide flagged items
                            .map { it.id to (it.data ?: emptyMap()) }
                    )
                }
            }
        onDispose { exReg.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = petName?.let { "$it's Activity" } ?: "Loading...",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
        }
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

                // --- Pet Header ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = petImageUrl ?: R.drawable.dog1,
                        contentDescription = "Pet Image",
                        modifier = Modifier.size(60.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(petName ?: "Unknown Pet", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }

                Spacer(Modifier.height(24.dp))

                // =============== MEAL PLANS ===============
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text("Meal Plans", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))

                        if (mealList.isEmpty()) {
                            Text("No meals yet.", color = Color.Gray)
                        } else mealList.forEach { (mealId, meal) ->

                            val mealName = meal["meal"]?.toString() ?: "Unknown meal"
                            val mealTime = meal["time"]?.toString() ?: "Unknown time"
                            val amount = meal["amount"]?.toString() ?: "Unknown amount"

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🍽 $mealName – $mealTime ($amount)")

                                Button(
                                    onClick = {
                                        db.collection("users").document(username)
                                            .collection("pets").document(petId)
                                            .collection("mealtime").document(mealId)
                                            .update("hidden", true)

                                        Toast.makeText(context, "Meal confirmed!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                ) {
                                    Text("Confirm Fed", color = Color.White, fontSize = 12.sp)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // =============== EXERCISES ===============
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text("Exercises", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))

                        if (exerciseList.isEmpty()) {
                            Text("No exercises yet.", color = Color.Gray)
                        } else exerciseList.forEach { (exId, ex) ->

                            val rec = ex["recommendation"]?.toString() ?: "Exercise"
                            val duration = when (val d = ex["duration"]) {
                                is Long -> d.toInt()
                                is Double -> d.toInt()
                                is String -> d.toIntOrNull() ?: 1
                                else -> 1
                            }

                            var remainingSeconds by remember { mutableStateOf(0) }
                            var isRunning by remember { mutableStateOf(false) }
                            var isPaused by remember { mutableStateOf(false) }
                            var showConfirm by remember { mutableStateOf(false) }

                            Column(Modifier.fillMaxWidth()) {

                                Text("🏃 $rec")
                                Spacer(Modifier.height(6.dp))

                                if (isRunning) {
                                    Text(
                                        "⏱ %02d:%02d remaining".format(
                                            remainingSeconds / 60,
                                            remainingSeconds % 60
                                        ),
                                        color = Color.Red
                                    )
                                } else if (showConfirm) {
                                    Text("✅ Exercise finished!", color = Color.Green)
                                }

                                Spacer(Modifier.height(6.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                                    when {
                                        isRunning && !isPaused -> {
                                            Button(
                                                onClick = { isPaused = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                            ) {
                                                Text("Pause", color = Color.White, fontSize = 12.sp)
                                            }
                                        }

                                        isRunning && isPaused -> {
                                            Button(
                                                onClick = { isPaused = false },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                            ) {
                                                Text("Resume", color = Color.White, fontSize = 12.sp)
                                            }
                                        }

                                        showConfirm -> {
                                            Button(
                                                onClick = {
                                                    db.collection("users").document(username)
                                                        .collection("pets").document(petId)
                                                        .collection("exercise").document(exId)
                                                        .update(mapOf(
                                                            "status" to "Completed",
                                                            "hidden" to true
                                                        ))

                                                    showConfirm = false
                                                    Toast.makeText(context, "Exercise marked done!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                            ) {
                                                Text("Confirm", color = Color.White, fontSize = 12.sp)
                                            }
                                        }

                                        else -> {
                                            Button(
                                                onClick = {
                                                    remainingSeconds = duration * 60
                                                    isRunning = true
                                                    isPaused = false

                                                    scope.launch {
                                                        while (remainingSeconds > 0) {
                                                            if (!isPaused) remainingSeconds--
                                                            delay(1000)
                                                        }
                                                        isRunning = false
                                                        showConfirm = true

                                                        setPetAlarm(
                                                            context,
                                                            "Exercise Finished!",
                                                            "${petName ?: "Your pet"} has completed the exercise!",
                                                            System.currentTimeMillis()
                                                        )
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                            ) {
                                                Text("Start Exercise", color = Color.White, fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            db.collection("users").document(username)
                                                .collection("pets").document(petId)
                                                .collection("exercise").document(exId)
                                                .update("hidden", true)

                                            Toast.makeText(context, "Exercise removed!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                    ) {
                                        Text("Remove", color = Color.White)
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}
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
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
        Toast.makeText(context, "Alarm permission required", Toast.LENGTH_SHORT).show()
    }
}
