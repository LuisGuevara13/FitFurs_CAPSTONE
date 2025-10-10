package com.example.fitfurs

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            FitFursTheme {
                AppNavigation(navController)
            }
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
        composable("meal") { DogAppUI(navController) }
        composable("petlist") { PetListScreen(navController) }
        composable("contacts") { ContactsScreen(navController) }   // <-- new route
    }
}

/* ---------------------------------------------------------
   WELCOME SCREEN
--------------------------------------------------------- */
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
            Text(text = "FitFurs", fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
                    SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                ) { append("Pet Care Service for Your ") }
                withStyle(
                    SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                ) { append("Best Friend.") }
            },
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Login")
            }

            Button(
                onClick = { navController.navigate("signup") },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Sign Up", color = Color.White)
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
        Text("Login", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))

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
                    email.isBlank() || password.isBlank() ->
                        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    savedEmail == null || savedPassword == null ->
                        Toast.makeText(context, "No local account found. Please Sign Up first.", Toast.LENGTH_SHORT).show()
                    email == savedEmail && password == savedPassword -> {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    }
                    else -> Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Login", color = Color.White)
        }
    }
}

/* ---------------------------------------------------------
   SIGNUP SCREEN
--------------------------------------------------------- */
@Composable
fun SignupScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
        Text("Sign Up", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))

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
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                    password != confirmPassword ->
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    else -> {
                        val prefs = context.getSharedPreferences("fitfurs_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("email", email).putString("password", password).apply()
                        Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate("login")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Sign Up", color = Color.White)
        }
    }
}


@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hello, K", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
        Spacer(modifier = Modifier.height(55.dp))

        Row(
            modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
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
            onClick = { Toast.makeText(context, "Medical Tracking", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.width(380.dp).height(75.dp),
            shape = RoundedCornerShape(16.dp),

            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3D3D3))
        ) {
            Icon(painter = painterResource(id = R.drawable.icon2), contentDescription = "Medical", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Medical Tracking", color = Color.Black, fontSize = 23.sp)
        }

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {navController.navigate("petlist")},
            modifier = Modifier.width(380.dp).height(75.dp),
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
            modifier = Modifier.width(380.dp).height(75.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3D3D3))
        ) {
            Icon(painter = painterResource(id = R.drawable.icon4), contentDescription = "Contacts", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contacts", color = Color.Black, fontSize = 23.sp)
        }
    }
}


@Composable
fun ContactsScreen(navController: NavHostController) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add new contact */ },
                containerColor = Color.White,
                shape = CircleShape
            ) { Text("+", fontSize = 28.sp, color = Color.Black) }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Contacts", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            ContactCard("Dr. Joever Bidone", "Veterinarian", "+63-999-999-9999")
            Spacer(Modifier.height(12.dp))
            ContactCard("Malolos Animal Clinic", "Veterinary Clinic", "+63-999-999-9999")
            Spacer(Modifier.height(12.dp))
            ContactCard("Dr. Ben Dover", "Veterinarian", "+63-999-999-9999")
        }
    }
}

@Composable
fun DogAppUI(navController: NavHostController) {
    Scaffold(
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // ✅ Back Button added here
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            // Profile Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.dogprof),
                    contentDescription = "Dog Profile",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Bailey", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))


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
fun ContactCard(name: String, role: String, phone: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Text(role, fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Number", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            Text(phone, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
@Composable
fun PetListScreen(navController: NavHostController) {
    val context = LocalContext.current
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { Toast.makeText(context, "Add new pet clicked", Toast.LENGTH_SHORT).show() },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Pet")
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF5F5F5), shape = CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_logo),
                        contentDescription = "FitFurs Logo",
                        modifier = Modifier.size(32.dp)
                    )
                    Text(" Pets ", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Image(
                        painter = painterResource(id = R.drawable.dog),
                        contentDescription = "Pet Icon",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Choose a pet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            PetCard("Choco", R.drawable.dog1, context,navController)
            Spacer(modifier = Modifier.height(12.dp))
            PetCard("Coco", R.drawable.dog2, context, navController)
            Spacer(modifier = Modifier.height(12.dp))
            PetCard("Bailey", R.drawable.dog3, context, navController)
        }
    }
}

@Composable
fun PetCard(petName: String, petImage: Int, context: Context, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (petName) {
                    "Choco" -> navController.navigate("meal")
                    "Coco" -> navController.navigate("meal")
                    "Bailey" -> navController.navigate("meal")
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = petImage),
                contentDescription = petName,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                petName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.Black)
        }
    }
}
