@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitfurs
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            PolicyCard(
                title = "1. Introduction",
                text = """
                    Welcome to FitFurs Pet Health Management App.
                    This Privacy Policy explains how we collect, use, and protect your information.
                """.trimIndent()
            )

            PolicyCard(
                title = "2. Information We Collect",
                text = """
                    • Personal Information
                    • Pet Health Information
                    • App usage analytics
                    • Technical data
                """.trimIndent()
            )

            PolicyCard(
                title = "3. How We Use Your Information",
                text = """
                    • Improve app experience
                    • Save pet records
                    • Send reminders
                """.trimIndent()
            )

            PolicyCard(
                title = "4. Sharing Your Information",
                text = """
                    We do not sell your data.
                    We only share with trusted service providers.
                """.trimIndent()
            )

            PolicyCard(
                title = "5. Data Security",
                text = """
                    We use encryption and secure storage methods.
                """.trimIndent()
            )
        }
    }
}


@Composable
fun PolicyCard(title: String, text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(18.dp),
                clip = false
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = Color.DarkGray
            )
        }
    }
}

