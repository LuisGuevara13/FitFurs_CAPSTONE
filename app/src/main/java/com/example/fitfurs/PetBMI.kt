package com.example.fitfurs

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.draw.shadow

fun updatePetCount(userId: String) {
    val db = FirebaseFirestore.getInstance()

    val userRef = db.collection("users").document(userId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)

        val currentCount = snapshot.getLong("numberOfPets") ?: 0L
        val newCount = currentCount + 1

        transaction.update(userRef, "numberOfPets", newCount)
    }.addOnSuccessListener {
        Log.d("PetCount", "Updated numberOfPets successfully")
    }.addOnFailureListener {
        Log.e("PetCount", "Failed to update numberOfPets: ${it.message}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetBMI(navController: NavHostController, userId: String) {

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }  // ✅ ADDED
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fileUri = uri
            fileName = "pet_${System.currentTimeMillis()}.jpg"
        }
    }

    var expanded by remember { mutableStateOf(false) }
    val speciesOptions = listOf("Dog", "Cat")

    // 🔥 Gender dropdown state
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female")

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
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // SPECIES DROPDOWN
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = species,
                    onValueChange = {},
                    label = { Text("Species") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    speciesOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                species = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // ✅ GENDER DROPDOWN
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gender = option
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Insert picture/video of your pet", fontSize = 14.sp, color = Color.Gray)

            OutlinedButton(
                onClick = { pickFileLauncher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(10.dp))
            ) {
                Text(if (fileUri == null) "Choose File" else "File Selected: $fileName")
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {

                    if (petName.isBlank() || species.isBlank() || breed.isBlank() ||
                        gender.isBlank() || age.isBlank() || weight.isBlank()
                    ) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // ❌ User MUST select a picture
                    if (fileUri == null) {
                        Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        var uploadedUrl: String? = null

                        try {
                            val bytes = context.contentResolver
                                .openInputStream(fileUri!!)!!
                                .readBytes()

                            SupabaseClientInstance.storage
                                .from("pet_media")
                                .upload(fileName, bytes, upsert = true)

                            uploadedUrl = SupabaseClientInstance.storage
                                .from("pet_media")
                                .publicUrl(fileName)

                        } catch (e: Exception) {
                            Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show()
                        }

                        val petData = hashMapOf(
                            "petName" to petName,
                            "species" to species,
                            "breed" to breed,
                            "gender" to gender,   // ✅ ALSO SAVED
                            "age" to age,
                            "weight" to weight,
                            "mediaUrl" to (uploadedUrl ?: "")
                        )

                        db.collection("users").document(userId)
                            .collection("pets")
                            .add(petData)
                            .addOnSuccessListener {

                                updatePetCount(userId)

                                Toast.makeText(context, "Pet saved!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home/$userId")
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to save", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
