package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantUiState
import com.example.ui.viewmodel.FoodSafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FoodSafetyViewModel,
    userProfile: UserProfile?,
    onNavigateToScan: () -> Unit,
    onNavigateToPlaces: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAssistant: () -> Unit
) {
    var quickQuestionText by remember { mutableStateOf("") }
    val assistantState by viewModel.assistantState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // --- HEADER ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NaturalAccentGreenMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile?.name?.take(2)?.uppercase() ?: "AS",
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text(
                            text = "EATRITE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextSlate,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = userProfile?.name ?: "Alex Sharma",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { /* Notification action */ },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, NaturalBorderGray, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = NaturalTextPrimary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToScan,
                        modifier = Modifier
                            .size(44.dp)
                            .background(NaturalPrimaryGreen, RoundedCornerShape(12.dp))
                            .testTag("scan_label_quick_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = "Scan Label",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // --- HEALTH PROFILE CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfile() },
                colors = CardDefaults.cardColors(containerColor = NaturalAccentGreenLight),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Health Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(NaturalPrimaryGreen, RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PERSONALIZED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Grid stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileMiniStat(
                            label = "Age",
                            value = "${userProfile?.age ?: 28}y",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMiniStat(
                            label = "Height",
                            value = "${userProfile?.height?.toInt() ?: 178}cm",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileMiniStat(
                            label = "Weight",
                            value = "${userProfile?.weight?.toInt() ?: 74}kg",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Alerts row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Medical Alert",
                            tint = NaturalPrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Alerts: ${userProfile?.healthIssues ?: "None listed"}",
                            fontSize = 12.sp,
                            color = NaturalTextSlate,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- QUICK ACTIONS GRID ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Scan Label",
                    subtitle = "FSSAI & Preservatives",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = onNavigateToScan,
                    modifier = Modifier.weight(1f),
                    testTag = "home_scan_label_card"
                )
                QuickActionCard(
                    title = "Dine Out",
                    subtitle = "Hygiene & Ratings",
                    icon = Icons.Default.Restaurant,
                    onClick = onNavigateToPlaces,
                    modifier = Modifier.weight(1f),
                    testTag = "home_dine_out_card"
                )
            }
        }

        // --- PERSISTENT MINI ASSISTANT SECTION ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NaturalBorderGray, RoundedCornerShape(topStart = 0.dp, topEnd = 32.dp, bottomStart = 32.dp, bottomEnd = 32.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 32.dp, bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(NaturalPrimaryGreen, CircleShape)
                        )
                        Text(
                            text = "Eatrite AI Assistant",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                    }

                    // Welcome assistant message
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NaturalBackground, RoundedCornerShape(16.dp))
                            .border(1.dp, NaturalAccentGreenSoft, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Hello! Based on your diet parameters and profile, I will automatically analyze food items and guide you about allergens, temperature risks, and preservatives. Try asking me a question below!",
                            fontSize = 13.sp,
                            color = NaturalTextPrimary,
                            lineHeight = 18.sp
                        )
                    }

                    // Interactive quick question box
                    OutlinedTextField(
                        value = quickQuestionText,
                        onValueChange = { quickQuestionText = it },
                        placeholder = { Text("Ask about spoilage, safety, or storage...", fontSize = 13.sp, color = NaturalTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_quick_question_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = NaturalSurfaceGray,
                            focusedContainerColor = NaturalSurfaceGray,
                            unfocusedBorderColor = NaturalBorderGray,
                            focusedBorderColor = NaturalPrimaryGreen,
                            cursorColor = NaturalPrimaryGreen
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (quickQuestionText.isNotBlank()) {
                                        viewModel.askAssistant(quickQuestionText)
                                        quickQuestionText = ""
                                        onNavigateToAssistant()
                                    }
                                },
                                modifier = Modifier.testTag("home_quick_question_submit")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = NaturalPrimaryGreen
                                )
                            }
                        },
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                color = NaturalTextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalTextPrimary
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NaturalBorderGray),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(NaturalAccentGreenSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NaturalPrimaryGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = NaturalTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
