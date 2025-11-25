package com.example.fitfurs

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import coil.compose.rememberAsyncImagePainter

data class ContactItem(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val phone: String = "",
    val clinicName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // real-time listener
    DisposableEffect(Unit) {
        val registration = db.collection("contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMsg = error.message
                    loading = false
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    ContactItem(
                        id = doc.id,
                        name = data["name"]?.toString() ?: "",
                        type = data["type"]?.toString() ?: "",
                        phone = data["phone"]?.toString() ?: "",   // ✅ FIXED
                        clinicName = data["clinicName"]?.toString() ?: ""
                    )
                } ?: emptyList()

                contacts = items
                loading = false
            }

        onDispose { registration.remove() }
    }

    Scaffold(
        containerColor = Color.White,

        topBar = {
            TopAppBar(
                title = { Text("Contacts", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_contact") },
                containerColor = Color.Black,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(10.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            when {
                loading -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color.Black) }

                !errorMsg.isNullOrEmpty() -> Text("Error: $errorMsg", color = Color.Red)

                contacts.isEmpty() -> Text("No contacts found.", color = Color.Gray)

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(contacts) { contact ->
                            ContactCard(
                                contact = contact,
                                onCall = { number ->
                                    val intent = Intent(Intent.ACTION_DIAL)
                                    intent.data = Uri.parse("tel:$number")
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



