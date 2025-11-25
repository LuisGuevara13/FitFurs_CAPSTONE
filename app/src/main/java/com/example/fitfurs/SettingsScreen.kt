package com.example.fitfurs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, userId: String) {

    val db = FirebaseFirestore.getInstance()

    var username by remember { mutableStateOf("User") }
    var email by remember { mutableStateOf("") }
    var profilePicUrl by remember { mutableStateOf<String?>(null) }

    // Fetch user data
    LaunchedEffect(userId) {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    username = snapshot.getString("username") ?: "User"
                    email = snapshot.getString("email") ?: ""
                    profilePicUrl = snapshot.getString("profilePic")
                }
            }
    }

    // Image picker launcher
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                uploadProfilePicture(userId, uri, db) { newUrl ->
                    profilePicUrl = newUrl
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Account",
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(16.dp))

            // ---------------- PROFILE WITH EDIT PEN ----------------
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = if (profilePicUrl != null)
                        rememberAsyncImagePainter(profilePicUrl)
                    else painterResource(R.drawable.dog1),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .shadow(8.dp, CircleShape),
                    contentScale = ContentScale.Crop
                )

                // PEN ICON
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(username, fontSize = 20.sp)
            Text(email, fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(24.dp))

            // ---------------- FIRST CARD GROUP ----------------
            SettingsGroup {
                SettingItem("Personal Information", Icons.Default.Person) {
                    navController.navigate("personal_info/$userId")
                }
                SettingItem("Password and Security", Icons.Default.Key) {
                    navController.navigate("change_password/$userId")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------------- SECOND CARD GROUP ----------------
            SettingsGroup {
                SettingItem("Contacts", Icons.Default.Email) {
                    navController.navigate("contacts")
                }
                SettingItem("Notifications", Icons.Default.Notifications) {
                    navController.navigate("notifications")
                }
                SettingItem("Privacy policy", Icons.Default.PrivacyTip) {
                    navController.navigate("policy")
                }
                SettingItem("About Us", Icons.Default.Info) {
                    navController.navigate("about_us")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------------- LOGOUT ----------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Logout", color = Color.Black)
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
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(vertical = 8.dp)) { content() }
    }
}

@Composable
fun SettingItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Black)
        Spacer(Modifier.width(12.dp))
        Text(title, color = Color.Black)
    }
}

suspend fun uploadFileToSupabase(uri: Uri): String {
    val storage = SupabaseClientInstance.storage

    val fileName = "profile_${UUID.randomUUID()}.jpg"

    val bucket = storage.from("profile-pictures")

    return withContext(Dispatchers.IO) {
        bucket.upload(fileName, uri, upsert = true)
        bucket.publicUrl(fileName)
    }
}

fun uploadProfilePicture(
    userId: String,
    uri: Uri,
    db: FirebaseFirestore,
    onUploaded: (String) -> Unit
) {
    kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
        val newUrl = uploadFileToSupabase(uri)

        db.collection("users").document(userId)
            .update("profilePic", newUrl)
            .addOnSuccessListener { onUploaded(newUrl) }
    }
}
