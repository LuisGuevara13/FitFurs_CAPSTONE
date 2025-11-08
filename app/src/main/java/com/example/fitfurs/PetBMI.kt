package com.example.fitfurs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetBMI(navController: NavHostController, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BMI Fill Up Form", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                label = { Text("Pet Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Species") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Insert picture of your pet", fontSize = 14.sp, color = Color.Gray)
            OutlinedButton(
                onClick = {
                    Toast.makeText(
                        context,
                        "Upload not yet available",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose Files")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (petName.isBlank() || species.isBlank() || breed.isBlank() || age.isBlank() || weight.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val petData = hashMapOf(
                            "petName" to petName.trim(),
                            "species" to species.trim(),
                            "breed" to breed.trim(),
                            "age" to age.trim(),
                            "weight" to weight.trim()
                        )

                        val petDocRef = db.collection("users")
                            .document(userId)
                            .collection("pets")
                            .document(petName.lowercase()) // ✅ use pet name as document ID

                        petDocRef.get().addOnSuccessListener { document ->
                            if (document.exists()) {
                                // 🔄 Update existing pet info
                                petDocRef.update(petData as Map<String, Any>)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Pet info updated successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("home/$userId") {
                                            popUpTo("bmi_form/$userId") { inclusive = true }
                                        }
                                    }
                            } else {
                                // 🆕 Add new pet info
                                petDocRef.set(petData)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Pet added successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("home/$userId") {
                                            popUpTo("bmi_form/$userId") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error: ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Next", color = Color.White)
            }
        }
    }
}
