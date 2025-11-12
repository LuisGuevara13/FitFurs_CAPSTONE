package com.example.fitfurs

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController, username: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.lebin),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hello, $username", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = {navController.navigate("settings")}) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
        Spacer(modifier = Modifier.height(55.dp))

        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ico1),
                contentDescription = "Pet Illustration",
                modifier = Modifier.size(75.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pet Overview", fontSize = 35.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = {  navController.navigate("petlistmed/$username") },
            modifier = Modifier
                .width(380.dp)
                .height(75.dp),
            shape = RoundedCornerShape(16.dp),

            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3D3D3))
        ) {
            Icon(painter = painterResource(id = R.drawable.icon2), contentDescription = "Medical", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Medical Tracking", color = Color.Black, fontSize = 23.sp)
        }

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = { navController.navigate("petlist/$username") },
            modifier = Modifier
                .width(380.dp)
                .height(75.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3D3D3))
        ) {
            Icon(painter = painterResource(id = R.drawable.icon3), contentDescription = "Exercise", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Diet & Exercise", color = Color.Black, fontSize = 23.sp)
        }
        Spacer(modifier = Modifier.height(25.dp))
        Button(
            onClick = { navController.navigate("contacts") },
            modifier = Modifier
                .width(380.dp)
                .height(75.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3D3D3))
        ) {
            Icon(painter = painterResource(id = R.drawable.icon4), contentDescription = "Contacts", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contacts", color = Color.Black, fontSize = 23.sp)
        }
        Spacer(modifier = Modifier.height(25.dp))
        Button(
            onClick = { navController.navigate("petlist/$username") },
            modifier = Modifier
                .width(380.dp)
                .height(75.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3D3D3))
        ) {
            Icon(painter = painterResource(id = R.drawable.overview), contentDescription = "Petoverview", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pet Overview", color = Color.Black, fontSize = 23.sp)
        }
    }
}