package com.example.fitfurs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

// 🐾 Enums for categories
enum class PetAgeCategory { PUPPY, ADULT, SENIOR }
enum class PetActivityLevel { LOW, MODERATE, HIGH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetStylePetFoodCalculatorScreen(onBack: () -> Unit = {}) {
    var petType by remember { mutableStateOf("Dog") }
    var ageCategory by remember { mutableStateOf(PetAgeCategory.ADULT) }
    var activityLevel by remember { mutableStateOf(PetActivityLevel.MODERATE) }
    var weight by remember { mutableStateOf("") }
    var kcalPerCup by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    val petTypes = listOf("Dog", "Cat")
    val ageCategories = PetAgeCategory.values().toList()
    val activityLevels = PetActivityLevel.values().toList()

    // ✅ Add scroll state
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // 👈 Enables scrolling
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🐶 Pet Type Dropdown
        DropdownField("Pet Type", petTypes, petType) { petType = it }

        Spacer(Modifier.height(8.dp))

        // 📅 Age Category
        DropdownFieldEnum("Age Category", ageCategories, ageCategory) { ageCategory = it }

        Spacer(Modifier.height(8.dp))

        // ⚡ Activity Level
        DropdownFieldEnum("Activity Level", activityLevels, activityLevel) { activityLevel = it }

        Spacer(Modifier.height(8.dp))

        // ⚖️ Weight Input
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // 🍽️ kcal per cup
        OutlinedTextField(
            value = kcalPerCup,
            onValueChange = { kcalPerCup = it },
            label = { Text("Food Energy Density (kcal per cup)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 🧮 Calculate Button
        Button(
            onClick = {
                result = vetCalculateFood(
                    petType,
                    ageCategory,
                    activityLevel,
                    weight.toFloatOrNull() ?: 0f,
                    kcalPerCup.toFloatOrNull() ?: 0f
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Calculate Daily Food", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))

        // 📊 Result
        if (result.isNotEmpty()) {
            Text(result, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "⚠️ Vet-approved guidelines (AAFCO/WSAVA). Always consult your veterinarian for precise needs.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Close", color = Color.White)
        }
    }
}

/**
 * Dropdown for basic String list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, items: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Dropdown for enum lists
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> DropdownFieldEnum(
    label: String,
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Vet calculation logic
 */
fun vetCalculateFood(
    petType: String,
    ageCategory: PetAgeCategory,
    activityLevel: PetActivityLevel,
    weightKg: Float,
    kcalPerCup: Float
): String {
    if (weightKg <= 0 || kcalPerCup <= 0) {
        return "Please enter valid weight and kcal per cup."
    }

    val rer = 70f * weightKg.pow(0.75f)

    val activityFactor = when (petType) {
        "Dog" -> when (ageCategory) {
            PetAgeCategory.PUPPY -> if (weightKg < 10) 3.0f else 2.0f
            PetAgeCategory.ADULT -> when (activityLevel) {
                PetActivityLevel.LOW -> 1.4f
                PetActivityLevel.MODERATE -> 1.6f
                PetActivityLevel.HIGH -> 2.0f
            }
            PetAgeCategory.SENIOR -> 1.3f
        }
        "Cat" -> when (ageCategory) {
            PetAgeCategory.PUPPY -> 2.5f
            PetAgeCategory.ADULT -> when (activityLevel) {
                PetActivityLevel.LOW -> 1.0f
                PetActivityLevel.MODERATE -> 1.2f
                PetActivityLevel.HIGH -> 1.4f
            }
            PetAgeCategory.SENIOR -> 1.0f
        }
        else -> 1.0f
    }

    val der = rer * activityFactor
    val cupsPerDay = der / kcalPerCup

    return buildString {
        appendLine("🔹 Estimated Daily Energy Requirement (DER): ${der.toInt()} kcal/day")
        appendLine("🔹 Approx. Food Amount: ${"%.2f".format(cupsPerDay)} cups/day")
    }
}
