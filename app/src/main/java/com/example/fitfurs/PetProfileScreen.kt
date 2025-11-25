package com.example.fitfurs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Pet(
    val petName: String = "",
    val species: String = "",
    val breed: String = "",
    val age: String = "",
    val gender: String = "",
    val weight: String = "",
    val mediaUrl: String = "",
    val currentExercise: String = "",
    val currentExerciseDuration: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(navController: NavHostController, username: String, petId: String) {

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var pet by remember { mutableStateOf<Pet?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Exercise Data
    var exerciseName by remember { mutableStateOf("") }
    var exerciseDuration by remember { mutableStateOf("") }

    // Food Data
    var foodMeal by remember { mutableStateOf("") }
    var foodAmount by remember { mutableStateOf("") }
    var foodTime by remember { mutableStateOf("") }

    // Listen to Pet Document
    DisposableEffect(username, petId) {
        val listener = db.collection("users")
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
                        age = snapshot.getString("age") ?: "",
                        gender = snapshot.getString("gender") ?: "",
                        weight = snapshot.getString("weight") ?: "",
                        mediaUrl = snapshot.getString("mediaUrl") ?: ""
                    )
                }

                loading = false
            }

        onDispose { listener.remove() }
    }

    // Listen to Latest Exercise
    DisposableEffect(username, petId) {
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
                    exerciseName = doc.getString("recommendation") ?: "Unknown"
                    exerciseDuration = "${doc.getLong("duration") ?: 0} minutes"
                }
            }

        onDispose { exerciseListener.remove() }
    }

    // Listen to Latest Food Entry
    DisposableEffect(username, petId) {
        val foodListener = db.collection("users")
            .document(username)
            .collection("pets")
            .document(petId)
            .collection("mealtime")
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, e ->

                if (e != null) return@addSnapshotListener

                if (snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    foodMeal = doc.getString("meal") ?: "-"
                    foodAmount = doc.getString("amount") ?: "-"
                    foodTime = doc.getString("time") ?: "-"
                }
            }

        onDispose { foodListener.remove() }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9F8FB)
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        pet?.let { p ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {

                // Header: Image + Name + LOGO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    // Pet Profile Pic + Name
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        AsyncImage(
                            model = p.mediaUrl,
                            contentDescription = "Pet Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )

                        Spacer(modifier = Modifier.width(15.dp))

                        Text(
                            text = p.petName.ifBlank { "-" },
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // FitFurs Logo (local file)
                    Icon(
                        painter = painterResource(id = R.drawable.icon_logo),
                        contentDescription = "FitFurs Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(85.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Current Exercise
                if (exerciseName.isNotBlank()) {
                    FullCard {
                        Text("CURRENT EXERCISE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Exercise: $exerciseName", fontSize = 14.sp)
                        Text("Duration: $exerciseDuration", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Latest Meal
                if (foodMeal.isNotBlank()) {
                    FullCard {
                        Text("LATEST MEAL", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Meal: $foodMeal", fontSize = 14.sp)
                        Text("Amount: $foodAmount", fontSize = 14.sp)
                        Text("Time: $foodTime", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // 2x2 Grid: Gender, Breed, Weight, Age+Species
                Column(modifier = Modifier.fillMaxWidth()) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        InfoCardSmall(
                            modifier = Modifier.weight(1f),
                            title = "Gender",
                            value = p.gender.ifBlank { "-" }
                        )
                        InfoCardSmall(
                            modifier = Modifier.weight(1f),
                            title = "Breed",
                            value = p.breed.ifBlank { "-" }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        InfoCardSmall(
                            modifier = Modifier.weight(1f),
                            title = "Weight",
                            value = if (p.weight.isNotBlank()) "${p.weight} kg" else "-"
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
}

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
            modifier = Modifier
                .padding(14.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(value, fontSize = 13.sp, color = Color.Gray)
        }
    }
}
