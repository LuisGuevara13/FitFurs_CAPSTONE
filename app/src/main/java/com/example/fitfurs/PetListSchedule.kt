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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.set


@Composable
fun PetListSchedule(navController: NavHostController, username: String) {
    val db = FirebaseFirestore.getInstance()
    var pets by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val context = LocalContext.current

    // Helper function to generate public URL from Supabase Storage
    fun getSupabasePublicUrl(fileName: String): String {
        return SupabaseClientInstance.storage.from("pet_media").publicUrl(fileName)
    }

    DisposableEffect(username) {
        val registration = db.collection("users")
            .document(username)
            .collection("pets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(
                        context,
                        "Failed to load pets: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                pets = snapshot?.documents?.map { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    data
                } ?: emptyList()
            }
        onDispose { registration.remove() }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("bmi_form/$username") },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
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
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Pets",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title with icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_logo),
                    contentDescription = "FitFurs Logo",
                    modifier = Modifier.size(35.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Pets",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.width(6.dp))
                Image(
                    painter = painterResource(id = R.drawable.ico1),
                    contentDescription = "Dog Icon",
                    modifier = Modifier.size(35.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Choose a pet",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (pets.isEmpty()) {
                Text(
                    text = "No pets yet. Add one!",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pets) { pet ->
                        val petName = pet["petName"]?.toString() ?: "Unnamed Pet"
                        val petId = pet["id"]?.toString() ?: ""

                        // Extract file name or URL stored in Firestore, assuming mediaUrl holds filename or URL:
                        val mediaUrlRaw = pet["mediaUrl"]?.toString() ?: ""
                        val petImageUrl = if (mediaUrlRaw.isNotBlank()) {
                            if (mediaUrlRaw.startsWith("http")) {
                                mediaUrlRaw
                            } else {
                                // Assume only filename stored, get public URL from Supabase
                                getSupabasePublicUrl(mediaUrlRaw)
                            }
                        } else null

                        PetCardAct(
                            petName = petName,
                            petImageUrl = petImageUrl,
                            onClick = {
                                navController.navigate("appointment/$username/$petId")
                            }
                        )
                    }
                }
            }
        }
    }
}


