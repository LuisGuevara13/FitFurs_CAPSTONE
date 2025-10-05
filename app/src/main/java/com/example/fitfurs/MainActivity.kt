package com.example.fitfurs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitfurs.ui.theme.FitFursTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.MedicalServices


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AppNavigation(navController)
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("pet_care") { DogAppUI() } // ✅ New route
    }
}
@Composable
fun WelcomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.icon_logo),
                contentDescription = "App Logo",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "FitFurs",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Image(
            painter = painterResource(id = R.drawable.dog),
            contentDescription = "Dog Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
        )

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                ) {
                    append("Pet Care Service for Your ")
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                ) {
                    append("Best Friend.")
                }
            },
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Login")
            }

            Button(
                onClick = { navController.navigate("signup") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(text = "Sign Up", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("fitfurs_prefs", Context.MODE_PRIVATE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate("welcome") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.icon_logo),
            contentDescription = "Logo",
            tint = Color.Black,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val savedEmail = prefs.getString("email", null)
                val savedPassword = prefs.getString("password", null)

                when {
                    email.isBlank() || password.isBlank() -> {
                        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    }
                    savedEmail == null || savedPassword == null -> {
                        // no local account found
                        Toast.makeText(context, "No local account found. Please Sign Up first.", Toast.LENGTH_SHORT).show()
                    }
                    email == savedEmail && password == savedPassword -> {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    else -> {
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = "Login", color = Color.White)
        }
    }
}



@Composable
fun SignupScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate("welcome") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.icon_logo),
            contentDescription = "Logo",
            tint = Color.Black,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "Sign Up",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                    }
                    password != confirmPassword -> {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // ✅ Save account locally
                        val prefs = context.getSharedPreferences("fitfurs_prefs", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("email", email)
                            .putString("password", password)
                            .apply()

                        Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") // Go to login after signup
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = "Sign Up", color = Color.White)
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔹 Top Row (Profile + Settings)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Image
                Image(
                    painter = painterResource(id = R.drawable.dog), // Replace with your profile img
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hello, K",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = {
                Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Pet Illustration + Title
        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ico1), // Replace with your cat-dog image
                contentDescription = "Pet Illustration",
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pet Overview",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold

            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Medical Tracking Button
        Button(
            onClick = { Toast.makeText(context, "Medical Tracking", Toast.LENGTH_SHORT).show() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon2), // Replace with paw icon
                contentDescription = "Medical",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Medical Tracking", color = Color.Black)
        }

        // 🔹 Diet & Exercise Button
        // 🔹 Diet & Exercise Button
        Button(
            onClick = { navController.navigate("pet_care") }, // ✅ Navigate to PetCareScreen
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCED4DA))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon3), // Replace with your food/exercise icon
                contentDescription = "Exercise",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Diet & Exercise", color = Color.Black)
        }


        // 🔹 Contacts Button
        Button(
            onClick = { Toast.makeText(context, "Contacts", Toast.LENGTH_SHORT).show() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon4),
                contentDescription = "Contacts",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contacts", color = Color.Black)
        }
    }
}
@Composable
fun DogAppUI() {
    Scaffold(
        bottomBar = { BottomNavigationBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Profile Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.dogprof), // replace with your dog image
                    contentDescription = "Dog Profile",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Bailey", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Exercise Plan
            Text("Exercise Plan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(Modifier.padding(12.dp)) {
                    ExerciseItem("Walk", "30 mins", "Finished")
                    Spacer(Modifier.height(10.dp))
                    ExerciseItem("Play Fetch", "15 mins", "Ongoing")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Meal Plan
            Text("Meal Plan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(Modifier.padding(20.dp)) {
                    MealItem("Breakfast", "8:00 AM")
                    Spacer(Modifier.height(15.dp))
                    MealItem("Lunch", "12:30 PM")
                    Spacer(Modifier.height(15.dp))
                    MealItem("Dinner", "6:00 PM")
                    Spacer(Modifier.height(15.dp))
                    MealItem("Dry Food", "120 g")
                    Spacer(Modifier.height(15.dp))
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(name: String, duration: String, status: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.walking), // replace with icons
                contentDescription = name,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(name, fontWeight = FontWeight.Medium)
                Text(duration, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Text(
            text = status,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (status == "Finished") Color.Green else Color.Red
        )
    }
}

@Composable
fun MealItem(name: String, time: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.meal), // replace with meal icons
                contentDescription = name,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(name, fontWeight = FontWeight.Medium)
        }
        Text(time, fontSize = 14.sp, color = Color.Gray)
    }
}
@Composable
fun BottomNavigationBar() {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = true,
            onClick = { /* TODO: Handle Home */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home), // your home icon
                    contentDescription = "Home"
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Handle Calendar */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home), // your calendar icon
                    contentDescription = "Calendar"
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Handle Notifications */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home), // your notification icon
                    contentDescription = "Notifications"
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Handle Profile */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home), // your profile icon
                    contentDescription = "Profile"
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Handle Settings */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home), // your settings icon
                    contentDescription = "Settings"
                )
            }
        )
    }
}