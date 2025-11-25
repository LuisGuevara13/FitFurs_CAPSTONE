@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitfurs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.Timestamp
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(navController: NavHostController, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var userData by remember { mutableStateOf<UserData?>(null) }

    // dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var fieldToEdit by remember { mutableStateOf("") }
    var currentValue by remember { mutableStateOf("") }

    // 🔥 NEW — confirmation dialog for unlinking
    var showUnlinkDialog by remember { mutableStateOf(false) }
    var unlinkFieldName by remember { mutableStateOf("") }

    // Firestore listener for the user document
    DisposableEffect(userId) {
        val registration = db.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                userData = if (snapshot != null && snapshot.exists()) {
                    UserData(
                        username = snapshot.getString("username") ?: "",
                        phone = snapshot.getString("phone") ?: "",
                        email = snapshot.getString("email") ?: ""
                    )
                } else UserData()
            }
        onDispose { registration.remove() }
    }

    // ---- AuthStateListener: listens for auth changes and applies pending email updates ----
    DisposableEffect(Unit) {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val current = firebaseAuth.currentUser
            if (current != null) {

                current.reload().addOnSuccessListener {

                    val uid = current.uid

                    db.collection("pending_email_changes")
                        .whereEqualTo("uid", uid)
                        .get()
                        .addOnSuccessListener { query ->

                            for (doc in query.documents) {
                                val requestedEmail = doc.getString("newEmail") ?: ""

                                if (requestedEmail.isNotEmpty() && current.email == requestedEmail) {

                                    // ALWAYS update the real auth user's Firestore document
                                    db.collection("users").document(uid)
                                        .update("email", requestedEmail)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Email saved to profile.",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            doc.reference.delete()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Failed to save email to profile: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        }
                }
            }
        }

        FirebaseAuth.getInstance().addAuthStateListener(authListener)
        onDispose { FirebaseAuth.getInstance().removeAuthStateListener(authListener) }
    }

    if (userData == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Black)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Personal Information",
                fontSize = 24.sp,
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Username card
        InfoCard(
            label = "Username",
            value = userData!!.username,
            leftButtonText = null,
            rightButtonText = "Change",
            onLeftClick = {},
            onRightClick = {
                fieldToEdit = "username"
                currentValue = userData!!.username
                showEditDialog = true
            }
        )

        // Phone card
        InfoCard(
            label = "Phone Number",
            value = userData!!.phone,
            leftButtonText = "Unlink",
            rightButtonText = "Change",
            onLeftClick = { unlinkField("phone", userId, context) },
            onRightClick = {
                fieldToEdit = "phone"
                currentValue = userData!!.phone
                showEditDialog = true
            }
        )

        // Email card
        InfoCard(
            label = "Email",
            value = userData!!.email,
            leftButtonText = "Unlink",
            rightButtonText = "Change",
            onLeftClick = {
                if (!userData!!.email.isNullOrEmpty()) {
                    unlinkFieldName = "email"
                    showUnlinkDialog = true
                } else {
                    Toast.makeText(context, "No email to unlink", Toast.LENGTH_SHORT).show()
                }
            },
            onRightClick = {
                fieldToEdit = "email"
                currentValue = userData!!.email
                showEditDialog = true
            }
        )
    }

    // Edit dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit ${fieldToEdit.replaceFirstChar { it.uppercase() }}") },
            text = {
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { currentValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fieldToEdit == "email") {
                            // Request email change
                            requestEmailChange(userId, currentValue.trim(), context)
                        } else {
                            updateField(userId, fieldToEdit, currentValue, context)
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    // 🔥 NEW — unlink confirmation dialog
    if (showUnlinkDialog) {
        AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            title = { Text("Unlink Email") },
            text = { Text("Are you sure you want to unlink your email? You will not be able to log in with email anymore.") },
            confirmButton = {
                Button(
                    onClick = {
                        unlinkEmailFromAuthAndFirestore(userId, context)
                        showUnlinkDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Unlink", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- InfoCard (unchanged) ---

// --- existing helpers ---
fun updateField(userId: String, field: String, value: String, context: Context) {
    val updates = hashMapOf<String, Any>(field to value)
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .update(updates)
        .addOnSuccessListener {
            Toast.makeText(context, "${field.replaceFirstChar { it.uppercase() }} updated", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun unlinkField(field: String, userId: String, context: Context) {
    val updates = hashMapOf<String, Any>(field to "")
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .update(updates)
        .addOnSuccessListener {
            Toast.makeText(context, "${field.replaceFirstChar { it.uppercase() }} unlinked", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to unlink: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

// --- NEW unlink logic for email (AUTH + FIRESTORE) ---
fun unlinkEmailFromAuthAndFirestore(userId: String, context: Context) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    if (user == null) {
        Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        return
    }

    // Safety check: user must have another provider
    if (user.providerData.size <= 2) {
        // providerData[0] = firebase internal
        // providerData[1] = password/email
        Toast.makeText(context, "Cannot unlink email. No other login method exists.", Toast.LENGTH_LONG).show()
        return
    }

    // 1. Unlink from Firebase authentication
    user.unlink("password")
        .addOnSuccessListener {

            // 2. Remove email from Firestore
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("email", "")
                .addOnSuccessListener {
                    Toast.makeText(context, "Email successfully unlinked.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to remove email from Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to unlink email: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

// --- Email change flow ---
fun requestEmailChange(userId: String, newEmail: String, context: Context) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    if (user == null) {
        Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
        return
    }

    if (newEmail.isBlank()) {
        Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show()
        return
    }

    user.verifyBeforeUpdateEmail(newEmail)
        .addOnSuccessListener {
            Toast.makeText(context, "Verification sent to $newEmail. Please confirm.", Toast.LENGTH_LONG).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to send verification: ${e.message}", Toast.LENGTH_LONG).show()
            return@addOnFailureListener
        }

    val db = FirebaseFirestore.getInstance()
    val token = UUID.randomUUID().toString()
    val payload = hashMapOf(
        "uid" to user.uid,
        "newEmail" to newEmail,
        "token" to token,
        "timestamp" to Timestamp.now()
    )

    db.collection("pending_email_changes")
        .document(token)
        .set(payload)
        .addOnSuccessListener {}
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to record pending change: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun InfoCard(
    label: String,
    value: String,
    leftButtonText: String?,
    rightButtonText: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Column {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leftButtonText != null) {
                    Button(onClick = onLeftClick, modifier = Modifier.height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                        Text(leftButtonText, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = false,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Transparent,
                        disabledLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Button(onClick = onRightClick, modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                    Text(rightButtonText, color = Color.White)
                }
            }
        }
    }
}

