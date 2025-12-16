package com.example.fitfurs

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import kotlinx.coroutines.launch

fun updatePetCount(userId: String) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)
        val currentCount = snapshot.getLong("numberOfPets") ?: 0L
        transaction.update(userRef, "numberOfPets", currentCount + 1)
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
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }

    // 🚫 NEW FLAG: prevents double submission
    var hasSubmitted by remember { mutableStateOf(false) }

    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Black,
        cursorColor = Color.Black,
        focusedBorderColor = Color.Black,
        unfocusedBorderColor = Color.Black,
        containerColor = Color.White
    )

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fileUri = uri
            fileName = "pet_${System.currentTimeMillis()}.jpg"
        }
    }

    var speciesExpanded by remember { mutableStateOf(false) }
    val speciesOptions = listOf("Dog", "Cat")

    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female")

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = {
                    Text(
                        "BMI Fill Up Form",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { if (!isSaving) navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                label = { Text("Pet Name", color = Color.Black) },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )

            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = speciesExpanded,
                onExpandedChange = { if (!isSaving) speciesExpanded = !speciesExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = species,
                    onValueChange = {},
                    label = { Text("Species", color = Color.Black) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(speciesExpanded) },
                    colors = textFieldColors,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = !isSaving
                )

                ExposedDropdownMenu(
                    expanded = speciesExpanded,
                    onDismissRequest = { speciesExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    speciesOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.Black) },
                            onClick = {
                                species = option
                                speciesExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed", color = Color.Black) },
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )

            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { if (!isSaving) genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = gender,
                    onValueChange = {},
                    label = { Text("Gender", color = Color.Black) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) },
                    colors = textFieldColors,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = !isSaving
                )

                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.Black) },
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
                label = { Text("Age", color = Color.Black) },
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)", color = Color.Black) },
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )

            Spacer(Modifier.height(16.dp))

            Text("Insert picture/video of your pet", fontSize = 14.sp, color = Color.Black)

            OutlinedButton(
                onClick = { if (!isSaving) pickFileLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text(
                    if (fileUri == null) "Choose File" else "File Selected: $fileName",
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {

                    // 🚫 Prevent double tap instantly!
                    if (hasSubmitted) return@Button
                    hasSubmitted = true

                    if (petName.isBlank() || species.isBlank() || breed.isBlank() ||
                        gender.isBlank() || age.isBlank() || weight.isBlank()
                    ) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        hasSubmitted = false
                        return@Button
                    }

                    if (fileUri == null) {
                        Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        hasSubmitted = false
                        return@Button
                    }

                    isSaving = true

                    scope.launch {
                        var uploadedUrl: String? = null

                        try {
                            val bytes =
                                context.contentResolver.openInputStream(fileUri!!)!!.readBytes()

                            SupabaseClientInstance.storage
                                .from("pet_media")
                                .upload(fileName, bytes, upsert = false)

                            uploadedUrl = SupabaseClientInstance.storage
                                .from("pet_media")
                                .publicUrl(fileName)

                        } catch (e: Exception) {
                            isSaving = false
                            hasSubmitted = false
                            Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        val petData = hashMapOf(
                            "petName" to petName,
                            "species" to species,
                            "breed" to breed,
                            "gender" to gender,
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
                                isSaving = false
                            }
                            .addOnFailureListener {
                                isSaving = false
                                hasSubmitted = false
                                Toast.makeText(context, "Failed to save", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isSaving) Color.Black else Color.Gray
                )
            ) {
                Text(
                    if (!isSaving) "Next" else "Saving… Please wait",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
