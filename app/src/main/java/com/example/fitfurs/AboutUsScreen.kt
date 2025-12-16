package com.example.fitfurs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AboutUsContent(
    val title: String = "",
    val section1: String = "",
    val section2: String = "",
    val mission: String = "",
    val vision: String = "",
    val features: List<String> = emptyList()
)

@Composable
fun AboutUsScreen(navController: NavHostController) {

    val scrollState = rememberScrollState()

    var content by remember { mutableStateOf<AboutUsContent?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // FIRESTORE LOAD
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("app_content")
                .document("about_us")
                .get()
                .await()

            content = snapshot.toObject(AboutUsContent::class.java)
            loading = false
        } catch (e: Exception) {
            loading = false
            errorMsg = e.message
        }
    }

    // 🔥 Force whole screen background to white
    Scaffold(
        containerColor = Color.White,
        contentColor = Color.Black
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)  // <- still ensure white
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {

            // BACK BUTTON + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "About Us",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LOADING
            if (loading) {
                CircularProgressIndicator(color = Color.Black)
                return@Column
            }

            // ERROR MESSAGE
            if (errorMsg != null) {
                Text("Failed to load content: $errorMsg", color = Color.Red)
                return@Column
            }

            // CONTENT LOADED SUCCESSFULLY
            content?.let { data ->

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(8.dp),

                    // 🔥 Force card background to pure white
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Column(modifier = Modifier.padding(20.dp)) {

                        Text(
                            text = data.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = data.section1, fontSize = 14.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = data.section2, fontSize = 14.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "What FitFurs Offers:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        data.features.forEach { feature ->
                            Bullet(text = feature)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Our Mission:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = data.mission, fontSize = 14.sp, color = Color.Black)

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Our Vision:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = data.vision, fontSize = 14.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun Bullet(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
        Text(text = "•  ", fontSize = 14.sp, color = Color.Black)
        Text(text = text, fontSize = 14.sp, color = Color.Black)
    }
}

