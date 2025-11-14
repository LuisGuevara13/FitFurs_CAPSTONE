package com.example.fitfurs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, username: String) {
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var profilePicUrl by remember { mutableStateOf<String?>(null) }

    // Fetch user data from Firestore
    LaunchedEffect(username) {
        db.collection("users").document(username)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    name = snapshot.getString("name") ?: username
                    email = snapshot.getString("email") ?: ""
                    profilePicUrl = snapshot.getString("profilePic")
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Profile Header ---
            Image(
                painter = if (profilePicUrl != null)
                    rememberAsyncImagePainter(profilePicUrl)
                else painterResource(R.drawable.dog1),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(10.dp))
            Text(name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(email, color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(20.dp))

            // --- Personal Info Section ---
            SettingsGroup {
                SettingItem(
                    icon = Icons.Default.Person,
                    title = "Personal Information",
                    onClick = { navController.navigate("personal_info/$username") }
                )
                SettingItem(
                    icon = Icons.Default.Key,
                    title = "Password and Security",
                    onClick = { navController.navigate("security_settings/$username") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- App Settings Section ---
            SettingsGroup {
                SettingItem(
                    icon = Icons.Default.Email,
                    title = "Contacts",
                    onClick = { navController.navigate("contacts") }
                )
                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    onClick = { navController.navigate("notifications") }
                )
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "About us",
                    onClick = { navController.navigate("about") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- Logout Button ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color.Red
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
}



