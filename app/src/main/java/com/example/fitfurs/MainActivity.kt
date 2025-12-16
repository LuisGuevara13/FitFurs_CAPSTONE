package com.example.fitfurs

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitfurs.ui.theme.FitFursTheme
import com.example.vetapp.screens.AddContactScreen


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // ✅ 1. Create notification channel (needed for all versions)
        createNotificationChannel()

        // ✅ 2. Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // ✅ 3. Check if app can schedule exact alarms (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // 🔔 Ask user to allow exact alarms in system settings
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }


        // ✅ 4. Continue app setup
        setContent {
            val navController = rememberNavController()
            FitFursTheme {
                AppNavigation(navController)
            }
        }
    }

    // ✅ Notification channel setup
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pet_channel",
                "Pet Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for pet meals and activities"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("login") { LoginScreen(navController) }

        composable("home/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            HomeScreen(navController, username)
        }

        composable("petlist/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            PetListScreen(navController, username)
        }
        composable("petlistmed/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            PetListScreenMed(navController, username)
        }
        composable("petoverview/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            PetListOverview(navController, username)
        }
        composable("petschedule/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            PetListSchedule(navController, username)
        }

        composable("bmi_form/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            PetBMI(navController, username)
        }

        composable("pet_activity/{username}/{petId}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            PetActivityScreen(navController, username, petId)
        }

        composable("add_activity/{username}/{petId}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            AddActivityScreen(navController, username, petId)
        }
        composable("medicalTracking/{username}/{petId}") { backStack ->
            val username = backStack.arguments?.getString("username") ?: ""
            val petId = backStack.arguments?.getString("petId") ?: ""
            MedicalTrackingScreen(navController, username, petId)
        }

        composable("scheduleAppointment/{username}/{petId}") { backStack ->
            val username = backStack.arguments?.getString("username") ?: ""
            val petId = backStack.arguments?.getString("petId") ?: ""
            ScheduleAppointmentScreen(navController, username, petId)
        }
        composable("settings/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            SettingsScreen(navController, username)
        }
        composable("pet_profile/{username}/{petId}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            PetProfileScreen(navController, username, petId, supabaseClient = SupabaseClientInstance)
        }

        composable("contacts") { ContactsScreen(navController) }


        //ForgotPassword screen shit
        composable("forgot_password") { ForgotPassword(navController) }

        //ResetPassword screen shit
        composable("reset_password/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPassword(navController, email)
        }
        composable("personal_info/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            PersonalInfoScreen(navController, username)
        }
        composable("change_password/{username}") {
            val username = it.arguments?.getString("username") ?: ""
            ChangePasswordScreen(navController, username)
        }
        composable("appointment/{username}/{petId}") {
            AppointmentScreen(navController, it.arguments!!.getString("username")!!, it.arguments!!.getString("petId")!!)
        }

        composable("add_contact") {
            AddContactScreen(navController)
        }
        composable("policy") { PrivacyPolicyScreen (navController) }
        composable("about_us") { AboutUsScreen(navController) }
    }
}

