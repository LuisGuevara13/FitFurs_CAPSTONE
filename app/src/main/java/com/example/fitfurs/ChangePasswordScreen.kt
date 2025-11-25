package com.example.fitfurs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavHostController, username: String) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible1 by remember { mutableStateOf(false) }
    var passwordVisible2 by remember { mutableStateOf(false) }
    var passwordVisible3 by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Change Password",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {

            PasswordCard(
                label = "Current Password",
                value = currentPassword,
                visible = passwordVisible1,
                onValueChange = { currentPassword = it },
                onVisibilityChange = { passwordVisible1 = !passwordVisible1 },
                placeholder = "Current Password"
            )

            Spacer(Modifier.height(20.dp))

            PasswordCard(
                label = "New Password",
                value = newPassword,
                visible = passwordVisible2,
                onValueChange = { newPassword = it },
                onVisibilityChange = { passwordVisible2 = !passwordVisible2 },
                placeholder = "New Password"
            )

            Spacer(Modifier.height(20.dp))

            PasswordCard(
                label = "Confirm Password",
                value = confirmPassword,
                visible = passwordVisible3,
                onValueChange = { confirmPassword = it },
                onVisibilityChange = { passwordVisible3 = !passwordVisible3 },
                placeholder = "Confirm Password"
            )

            Spacer(Modifier.height(30.dp))

            // BLACK BUTTON
            Button(
                onClick = {
                    if (user == null) {
                        Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    when {
                        currentPassword.isBlank() -> {
                            Toast.makeText(context, "Enter current password!", Toast.LENGTH_SHORT).show()
                        }
                        newPassword != confirmPassword -> {
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                        }
                        newPassword.length < 6 -> {
                            Toast.makeText(context, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val credential = EmailAuthProvider.getCredential(
                                user.email ?: "",
                                currentPassword
                            )

                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Incorrect current password!", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,    // BLACK BUTTON
                    contentColor = Color.White       // WHITE TEXT
                ),
                modifier = Modifier
                    .align(Alignment.End)
                    .height(45.dp)
            ) {
                Text("Change", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PasswordCard(
    label: String,
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onVisibilityChange: () -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Visibility"
                    )
                }
            }
        )
    }
}
