package com.example.vetapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(navController: androidx.navigation.NavHostController) {

    // FIX: Use Strings instead of TextFieldValue
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var clinicName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // TOP TITLE BAR
        TopAppBar(
            title = {
                Text(
                    "Veterinary Clinic",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            },
            modifier = Modifier.background(Color.White),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // CONTACT CARD WITH SHADOW
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(12.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Text("Add New Contact", fontWeight = FontWeight.Bold, color = Color.Black)

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = clinicName,
                    onValueChange = { clinicName = it },
                    label = { Text("Vet Clinic Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        isSaving = true
                        saveContact(db, name, phone, clinicName) {
                            isSaving = false
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (isSaving) "Saving..." else "Save Contact")
                }
            }
        }
    }
}

private fun saveContact(
    db: FirebaseFirestore,
    name: String,
    phone: String,
    clinicName: String,
    onDone: () -> Unit
) {
    val contactData = hashMapOf(
        "name" to name,
        "phone" to phone,
        "clinicName" to clinicName
    )

    db.collection("contacts")
        .add(contactData)
        .addOnSuccessListener { onDone() }
        .addOnFailureListener { onDone() }
}
