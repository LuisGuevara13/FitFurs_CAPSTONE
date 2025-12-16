package com.example.fitfurs

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

data class Pet(
    val petName: String = "",
    val species: String = "",
    val breed: String = "",
    val age: String = "",
    val gender: String = "",
    val weight: String = "",
    val mediaUrl: String = "",
    val healthStatus: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    navController: NavHostController,
    username: String,
    petId: String,
    supabaseClient: SupabaseClientInstance
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var pet by remember { mutableStateOf<Pet?>(null) }
    var loading by remember { mutableStateOf(true) }

    // IMAGE PICKER
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                Toast.makeText(context, "Uploading image...", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val fileName = "pet_media/${petId}_${UUID.randomUUID()}.jpg"
                        val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: byteArrayOf()
                        supabaseClient.storage["pet_media"].upload(fileName, bytes, upsert = true)
                        val publicUrl = supabaseClient.storage["pet_media"].publicUrl(fileName)
                        db.collection("users").document(username)
                            .collection("pets").document(petId)
                            .update("mediaUrl", publicUrl)
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    // EXERCISE
    var exerciseName by remember { mutableStateOf("") }
    var exerciseDuration by remember { mutableStateOf("") }

    // MEAL
    var foodName by remember { mutableStateOf("") }
    var foodAmount by remember { mutableStateOf("") }
    var foodTime by remember { mutableStateOf("") }

    // ---------------- FIRESTORE LISTENERS ----------------
    DisposableEffect(username, petId) {
        val petListener = db.collection("users")
            .document(username)
            .collection("pets")
            .document(petId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    loading = false
                    Toast.makeText(context, "Failed to load pet", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    pet = Pet(
                        petName = snapshot.getString("petName") ?: "",
                        species = snapshot.getString("species") ?: "",
                        breed = snapshot.getString("breed") ?: "",
                        age = snapshot.get("age")?.toString() ?: "",
                        gender = snapshot.getString("gender") ?: "",
                        weight = snapshot.get("weight")?.toString() ?: "",
                        mediaUrl = snapshot.getString("mediaUrl") ?: "",
                        healthStatus = snapshot.getString("healthStatus")?.lowercase()?.trim() ?: "unknown"
                    )
                }
                loading = false
            }

        val exerciseListener = db.collection("users")
            .document(username)
            .collection("pets")
            .document(petId)
            .collection("exercise")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val hidden = doc.getBoolean("hidden") ?: false
                    if (!hidden) {
                        exerciseName = doc.getString("recommendation") ?: ""
                        exerciseDuration = "${doc.getLong("duration") ?: 0} minutes"
                    } else {
                        exerciseName = ""
                        exerciseDuration = ""
                    }
                } else {
                    exerciseName = ""
                    exerciseDuration = ""
                }
            }

        val foodListener = db.collection("users")
            .document(username)
            .collection("pets")
            .document(petId)
            .collection("mealtime")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val hidden = doc.getBoolean("hidden") ?: false
                    if (!hidden) {
                        foodName = doc.getString("meal") ?: "-"
                        foodAmount = doc.getString("amount") ?: "-"
                        foodTime = doc.getString("time") ?: "-"
                    } else {
                        foodName = ""
                        foodAmount = ""
                        foodTime = ""
                    }
                } else {
                    foodName = ""
                    foodAmount = ""
                    foodTime = ""
                }
            }

        onDispose {
            petListener.remove()
            exerciseListener.remove()
            foodListener.remove()
        }
    }

    // ---------------- UI ----------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black // <- back arrow color updated to black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        pet?.let { p ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // ---------------- HEADER ----------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }) {
                            AsyncImage(
                                model = selectedImageUri ?: p.mediaUrl,
                                contentDescription = "Pet Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                            )

                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Picture",
                                tint = Color.Black,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(26.dp)
                                    .background(Color.White, CircleShape)
                                    .padding(5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = p.petName.ifBlank { "-" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            HealthStatusBadge(p.healthStatus)
                        }
                    }

                    // Right: FitFur Logo
                    Icon(
                        painter = painterResource(id = R.drawable.icon_logo),
                        contentDescription = "FitFur Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(90.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---------------- CURRENT EXERCISE ----------------
                if (exerciseName.isNotBlank()) {
                    FullCard {
                        Text("CURRENT EXERCISE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Exercise: $exerciseName", fontSize = 14.sp)
                        Text("Duration: $exerciseDuration", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ---------------- LATEST MEAL ----------------
                if (foodName.isNotBlank()) {
                    FullCard {
                        Text("LATEST MEAL", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Food: $foodName", fontSize = 14.sp)
                        Text("Amount: $foodAmount", fontSize = 14.sp)
                        Text("Time: $foodTime", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ---------------- INFO GRID ----------------
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    InfoCardSmall(modifier = Modifier.weight(1f), title = "Gender", value = p.gender)
                    InfoCardSmall(modifier = Modifier.weight(1f), title = "Breed", value = p.breed)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    EditableInfoCard(
                        modifier = Modifier.weight(1f),
                        title = "Weight",
                        value = if (p.weight.isNotBlank()) "${p.weight} kg" else "-",
                        onEdit = { newWeight ->
                            db.collection("users")
                                .document(username)
                                .collection("pets")
                                .document(petId)
                                .update("weight", newWeight)
                            Toast.makeText(context, "Weight updated", Toast.LENGTH_SHORT).show()
                        }
                    )

                    InfoCardSmall(
                        modifier = Modifier.weight(1f),
                        title = "Age • Species",
                        value = "${p.age.ifBlank { "-" }} • ${p.species.ifBlank { "-" }}"
                    )
                }
            }
        }
    }
}

/* ---------------- COMPONENTS ---------------- */

@Composable
fun FullCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun EditableInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    onEdit: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    Card(
        modifier = modifier.height(95.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Black)
                IconButton(onClick = {
                    input = value.replace(" kg", "")
                    showDialog = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit $title", tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
            Text(value, fontSize = 13.sp, color = Color.Black)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Edit $title") },
            text = {
                OutlinedTextField(value = input, onValueChange = { input = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    onEdit(input)
                    showDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun InfoCardSmall(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier.height(95.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.Black)
            Text(value, fontSize = 13.sp, color = Color.Black)
        }
    }
}

@Composable
fun HealthStatusBadge(status: String) {
    val normalized = status.lowercase().trim()
    val (color, icon, label) = when (normalized) {
        "healthy" -> Triple(Color(0xFF2E7D32), Icons.Default.Favorite, "HEALTHY")
        "needs checkup" -> Triple(Color(0xFF1976D2), Icons.Default.Event, "NEEDS CHECKUP")
        "sick" -> Triple(Color(0xFFF57C00), Icons.Default.MedicalServices, "SICK")
        "injured" -> Triple(Color(0xFFC62828), Icons.Default.Healing, "INJURED")
        else -> Triple(Color.Gray, Icons.Default.HelpOutline, "UNKNOWN")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(color, RoundedCornerShape(22.dp)).padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
