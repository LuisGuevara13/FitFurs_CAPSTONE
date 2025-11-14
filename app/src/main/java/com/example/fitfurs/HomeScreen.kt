package com.example.fitfurs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController, username: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.lebin),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.Gray)) { append("Hello, ") }
                        withStyle(SpanStyle(color = Color.Black, fontWeight = FontWeight.Bold)) {
                            append(username)
                        }
                    },
                    fontSize = 18.sp
                )
            }

            // ✅ Pass username when navigating to Settings
            IconButton(onClick = { navController.navigate("settings/$username") }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // --- Title Section: Dog + Activities Side by Side ---
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ico1),
                contentDescription = "Pet Illustration",
                modifier = Modifier
                    .size(90.dp)
                    .padding(end = 8.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "Activities",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        // --- Buttons Section ---
        ActivityButton(
            iconRes = R.drawable.icon2,
            text = "Medical Tracking",
            onClick = { navController.navigate("petlistmed/$username") }
        )
        Spacer(modifier = Modifier.height(24.dp))
        ActivityButton(
            iconRes = R.drawable.icon3,
            text = "Diet & Exercise",
            onClick = { navController.navigate("petlist/$username") }
        )
        Spacer(modifier = Modifier.height(24.dp))
        ActivityButton(
            iconRes = R.drawable.overview,
            text = "Pet Overview",
            onClick = { navController.navigate("petlist/$username") }
        )
    }
}

@Composable
fun ActivityButton(iconRes: Int, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(330.dp)
            .height(70.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            ),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = Color.Black,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text,
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
