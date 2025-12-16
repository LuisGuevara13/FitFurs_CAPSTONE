package com.example.fitfurs
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun PetCardSchedule(
    petName: String,
    petImageUrl: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 10.dp,
                shape = MaterialTheme.shapes.medium,
                clip = false
            )
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!petImageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = petImageUrl),
                    contentDescription = "Pet Image",
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)   // 👈 MAKE IMAGE CIRCULAR
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.dog),
                    contentDescription = "No Image",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)   // 👈 CIRCULAR PLACEHOLDER
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = petName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
