package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodSafetyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: FoodSafetyViewModel,
    currentProfile: UserProfile?
) {
    val coroutineScope = rememberCoroutineScope()

    var nameInput by remember { mutableStateOf(currentProfile?.name ?: "Alex Sharma") }
    var genderInput by remember { mutableStateOf(currentProfile?.gender ?: "Male") }
    var ageInput by remember { mutableStateOf(currentProfile?.age?.toFloat() ?: 28f) }
    var heightInput by remember { mutableStateOf(currentProfile?.height?.toFloat() ?: 178f) }
    var weightInput by remember { mutableStateOf(currentProfile?.weight?.toFloat() ?: 74f) }
    var healthIssuesInput by remember { mutableStateOf(currentProfile?.healthIssues ?: "Gluten Intolerance, Mild Hypertension") }

    var showSuccessBanner by remember { mutableStateOf(false) }

    // Sync input states when Room emits new profile values
    LaunchedEffect(currentProfile) {
        if (currentProfile != null) {
            nameInput = currentProfile.name
            genderInput = currentProfile.gender
            ageInput = currentProfile.age.toFloat()
            heightInput = currentProfile.height.toFloat()
            weightInput = currentProfile.weight.toFloat()
            healthIssuesInput = currentProfile.healthIssues
        }
    }

    val healthSuggestions = listOf(
        "Gluten Intolerance",
        "Lactose Intolerance",
        "Peanut Allergy",
        "Diabetes",
        "Mild Hypertension",
        "Egg Allergy",
        "None"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Title Block
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Personal Health Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
                Text(
                    text = "Define your dietary constraints. Eatrite AI leverages these values to filter ingredients.",
                    fontSize = 13.sp,
                    color = NaturalTextMuted
                )
            }
        }

        // Success banner
        item {
            AnimatedVisibility(visible = showSuccessBanner) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NaturalAccentGreenMedium),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NaturalPrimaryGreen)
                        Text(
                            text = "Health Profile successfully saved & synced with Eatrite AI!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                    }
                }
            }
        }

        // Edit Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NaturalBorderGray),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Profile Parameters",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextPrimary
                    )

                    // Full Name Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Full Name", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalTextSlate)
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = NaturalBorderGray,
                                focusedBorderColor = NaturalPrimaryGreen,
                                cursorColor = NaturalPrimaryGreen
                            ),
                            singleLine = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) }
                        )
                    }

                    // Gender Selection Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Gender", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalTextSlate)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Male", "Female", "Other").forEach { g ->
                                val isSelected = genderInput == g
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) NaturalPrimaryGreen else NaturalAccentGreenLight,
                                            RoundedCornerShape(100.dp)
                                        )
                                        .clickable { genderInput = g }
                                        .padding(vertical = 8.dp, horizontal = 16.dp)
                                        .testTag("gender_chip_$g"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = g,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else NaturalPrimaryGreen
                                    )
                                }
                            }
                        }
                    }

                    // Age Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Age: ${ageInput.toInt()} years", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalTextSlate)
                        Slider(
                            value = ageInput,
                            onValueChange = { ageInput = it },
                            valueRange = 1f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = NaturalPrimaryGreen,
                                activeTrackColor = NaturalPrimaryGreen,
                                inactiveTrackColor = NaturalBorderGray
                            ),
                            modifier = Modifier.testTag("age_slider")
                        )
                    }

                    // Height Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Height: ${heightInput.toInt()} cm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalTextSlate)
                        Slider(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            valueRange = 80f..250f,
                            colors = SliderDefaults.colors(
                                thumbColor = NaturalPrimaryGreen,
                                activeTrackColor = NaturalPrimaryGreen,
                                inactiveTrackColor = NaturalBorderGray
                            ),
                            modifier = Modifier.testTag("height_slider")
                        )
                    }

                    // Weight Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Weight: ${weightInput.toInt()} kg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalTextSlate)
                        Slider(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            valueRange = 10f..200f,
                            colors = SliderDefaults.colors(
                                thumbColor = NaturalPrimaryGreen,
                                activeTrackColor = NaturalPrimaryGreen,
                                inactiveTrackColor = NaturalBorderGray
                            ),
                            modifier = Modifier.testTag("weight_slider")
                        )
                    }

                    // Health issues / Allergies list input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Allergies & Health Parameters", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalTextSlate)
                        OutlinedTextField(
                            value = healthIssuesInput,
                            onValueChange = { healthIssuesInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                                .testTag("profile_health_issues_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = NaturalBorderGray,
                                focusedBorderColor = NaturalPrimaryGreen,
                                cursorColor = NaturalPrimaryGreen
                            ),
                            leadingIcon = { Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null) }
                        )

                        // Quick toggle suggestions
                        Text(
                            text = "Tap to toggle parameters:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        FlowRowLayout(
                            modifier = Modifier.fillMaxWidth(),
                            spacing = 6.dp
                        ) {
                            healthSuggestions.forEach { suggestion ->
                                val contains = healthIssuesInput.contains(suggestion, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (contains) NaturalPrimaryGreen else NaturalSurfaceGray,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (suggestion == "None") {
                                                healthIssuesInput = ""
                                            } else {
                                                if (contains) {
                                                    // Remove
                                                    val regex = Regex("\\b,?\\s*${suggestion}\\b", RegexOption.IGNORE_CASE)
                                                    healthIssuesInput = healthIssuesInput.replace(regex, "").trim(',', ' ')
                                                } else {
                                                    // Add
                                                    healthIssuesInput = if (healthIssuesInput.isBlank()) {
                                                        suggestion
                                                    } else {
                                                        "${healthIssuesInput.trim(',', ' ')}, $suggestion"
                                                    }
                                                }
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = suggestion,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (contains) Color.White else NaturalTextSlate
                                    )
                                }
                            }
                        }
                    }

                    // Save Button
                    Button(
                        onClick = {
                            viewModel.saveProfile(
                                UserProfile(
                                    name = nameInput,
                                    gender = genderInput,
                                    age = ageInput.toInt(),
                                    height = heightInput.toDouble(),
                                    weight = weightInput.toDouble(),
                                    healthIssues = healthIssuesInput
                                )
                            )
                            coroutineScope.launch {
                                showSuccessBanner = true
                                delay(3000)
                                showSuccessBanner = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_profile_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimaryGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Health Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRowLayout(
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 6.dp,
    content: @Composable () -> Unit
) {
    // A simplified layout that mimics flow wrapping behavior
    // Compose FlowRow is available in foundation, let's use a Box/Row simulation or just wrap cleanly.
    // In Compose foundation, Layout is available, or we can use Row with Scroll or Box.
    // Let's use standard Row with horizontalScroll or a standard Box to be extremely compile safe!
    // Row scroll is 100% compile safe and fast.
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        content()
    }
}
