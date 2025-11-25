package com.example.fitfurs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContactCard(
    contact: ContactItem,
    onCall: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(18.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White   // <-- FORCED WHITE BACKGROUND
        )
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {

            // NAME (header)
            Text(
                text = contact.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(12.dp))

            // CLINIC NAME BOX
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Clinic Name:",
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(contact.clinicName, color = Color.Black)
                }
            }

            Spacer(Modifier.height(12.dp))

            // PHONE NUMBER BOX
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCall(contact.phone) }
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Number:",
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(contact.phone, color = Color.Black)
                }
            }
        }
    }
}
