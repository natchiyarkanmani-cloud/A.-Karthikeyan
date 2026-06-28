package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantUiState
import com.example.ui.viewmodel.FoodSafetyViewModel
import com.example.ui.viewmodel.ImageGenUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    viewModel: FoodSafetyViewModel
) {
    val assistantState by viewModel.assistantState.collectAsState()
    val chatHistory by viewModel.assistantChatHistory.collectAsState()
    val useHighThinking by viewModel.useHighThinking.collectAsState()
    val imageGenState by viewModel.imageGenState.collectAsState()

    var queryInput by remember { mutableStateOf("") }
    var imagePromptInput by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("1K") }

    // Temperature slider states
    var currentTemp by remember { mutableStateOf(15f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Main Title
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Eatrite Deep AI",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
                Text(
                    text = "Consult deep-thinking safety reports, storage dangers, and generate visual guidelines.",
                    fontSize = 13.sp,
                    color = NaturalTextMuted
                )
            }
        }

        // --- CHAT CONVERSATION BLOCK ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NaturalBorderGray),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Safety Consultation",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )

                        // High Thinking Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "High Thinking",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (useHighThinking) NaturalPrimaryGreen else NaturalTextMuted
                            )
                            Switch(
                                checked = useHighThinking,
                                onCheckedChange = { viewModel.useHighThinking.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NaturalPrimaryGreen,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = NaturalBorderGray
                                ),
                                modifier = Modifier
                                    .scale(0.75f)
                                    .testTag("high_thinking_toggle")
                            )
                        }
                    }

                    Divider(color = NaturalBorderGray)

                    // Quick prompt chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Risks of 15°C cold storage?",
                            "How to detect spoiled chicken?",
                            "Safe storage time for leftover rice?"
                        ).forEach { sample ->
                            Box(
                                modifier = Modifier
                                    .background(NaturalAccentGreenLight, RoundedCornerShape(100.dp))
                                    .clickable {
                                        queryInput = sample
                                        viewModel.askAssistant(sample)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = sample, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NaturalPrimaryGreen)
                            }
                        }
                    }

                    // Chat messages scroll list container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (chatHistory.isEmpty()) {
                            Text(
                                text = "Ask about spoilage, food preservation, chemical preservatives, temperature risks, and get personalized dietary advice with cited health guidelines.",
                                fontSize = 12.sp,
                                color = NaturalTextMuted,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            chatHistory.forEach { (role, text) ->
                                val isUser = role == "user"
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .background(
                                                if (isUser) NaturalAccentGreenMedium else NaturalSurfaceGray,
                                                RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                                )
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isUser) "You" else "Eatrite AI",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUser) NaturalPrimaryGreen else NaturalTextSlate
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = text,
                                                fontSize = 12.sp,
                                                color = NaturalTextPrimary,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Query execution display
                    when (val state = assistantState) {
                        is AssistantUiState.Loading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NaturalAccentGreenLight, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = NaturalPrimaryGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (useHighThinking) "Eatrite AI is doing deep safety analysis..." else "Drafting answer...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NaturalPrimaryGreen
                                )
                            }
                        }
                        is AssistantUiState.Error -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NaturalAlertRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = NaturalAlertRed, modifier = Modifier.size(16.dp))
                                Text(text = state.message, fontSize = 11.sp, color = NaturalAlertRed)
                            }
                        }
                        else -> {}
                    }

                    // Input Text
                    OutlinedTextField(
                        value = queryInput,
                        onValueChange = { queryInput = it },
                        placeholder = { Text("Ask about spoilage or safety...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("assistant_query_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = NaturalBorderGray,
                            focusedBorderColor = NaturalPrimaryGreen,
                            cursorColor = NaturalPrimaryGreen
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (queryInput.isNotBlank()) {
                                        viewModel.askAssistant(queryInput)
                                        queryInput = ""
                                    }
                                },
                                modifier = Modifier.testTag("assistant_submit_button")
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = NaturalPrimaryGreen)
                            }
                        },
                        singleLine = true
                    )
                }
            }
        }

        // --- TEMPERATURE SAFETY WIDGET ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NaturalBorderGray),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = if (currentTemp in 4.0f..60.0f) NaturalAlertRed else NaturalPrimaryGreen
                        )
                        Text(
                            text = "Food Temperature Hazard Guide",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                    }

                    Text(
                        text = "Slide the thermometer to inspect how storage temperature changes food security risks and bacterial growth.",
                        fontSize = 12.sp,
                        color = NaturalTextMuted
                    )

                    // Slider
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NaturalBackground, RoundedCornerShape(16.dp))
                            .border(1.dp, NaturalBorderGray, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selected Storage: ${currentTemp.toInt()}°C",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentTemp in 4f..60f) NaturalAlertRed else NaturalPrimaryGreen
                        )

                        Slider(
                            value = currentTemp,
                            onValueChange = { currentTemp = it },
                            valueRange = -10f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = if (currentTemp in 4f..60f) NaturalAlertRed else NaturalPrimaryGreen,
                                activeTrackColor = if (currentTemp in 4f..60f) NaturalAlertRed else NaturalPrimaryGreen,
                                inactiveTrackColor = NaturalBorderGray
                            ),
                            modifier = Modifier.testTag("temperature_slider")
                        )

                        // Condition evaluation description box
                        val isDangerZone = currentTemp in 4f..60f
                        val riskTitle = if (isDangerZone) "⚠️ THE DANGER ZONE" else if (currentTemp < 4f) "❄️ Safe Cold Storage" else "🔥 Safe Hot Holding"
                        val riskDesc = if (isDangerZone) {
                            "Bacteria multiply exponentially (doubling every 20 minutes) between 4°C and 60°C. Food must not be kept here for more than 2 hours. Spoilage risks are critical!"
                        } else if (currentTemp < 4f) {
                            "Temperatures below 4°C significantly slow bacterial replication. Safe for perishable products like fresh dairy, meats, and leafy vegetables."
                        } else {
                            "Temperatures above 60°C destroy active vegetative bacteria cells. Recommended for maintaining buffet dishes and warm servings safely."
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(
                                    if (isDangerZone) NaturalAlertRed.copy(alpha = 0.1f) else NaturalAccentGreenLight,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = riskTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDangerZone) NaturalAlertRed else NaturalPrimaryGreen
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = riskDesc,
                                    fontSize = 11.sp,
                                    color = NaturalTextPrimary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- IMAGE GENERATOR (PRO IMAGE PREVIEW 1K, 2K, 4K) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NaturalBorderGray),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = NaturalPrimaryGreen)
                        Text(
                            text = "Visual Safety Infographic Maker",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                    }

                    Text(
                        text = "Generate a custom high-quality food safety poster or guideline visual choosing your size (1K, 2K, or 4K).",
                        fontSize = 12.sp,
                        color = NaturalTextMuted
                    )

                    // Size Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1K", "2K", "4K").forEach { size ->
                            val isSelected = size == selectedSize
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) NaturalPrimaryGreen else NaturalAccentGreenLight,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedSize = size }
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                                    .testTag("size_select_$size"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = size,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else NaturalPrimaryGreen
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = imagePromptInput,
                        onValueChange = { imagePromptInput = it },
                        placeholder = { Text("e.g. Storage guide for dairy and meat", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_prompt_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = NaturalBorderGray,
                            focusedBorderColor = NaturalPrimaryGreen,
                            cursorColor = NaturalPrimaryGreen
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (imagePromptInput.isNotBlank()) {
                                viewModel.generateSafetyImage(imagePromptInput, selectedSize)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_image_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimaryGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Safety Graphic ($selectedSize)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Image result display
                    when (val state = imageGenState) {
                        is ImageGenUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(NaturalBackground, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(color = NaturalPrimaryGreen)
                                    Text("Generating high quality safety visual...", fontSize = 12.sp, color = NaturalPrimaryGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        is ImageGenUiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NaturalAlertRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Text(text = state.message, fontSize = 12.sp, color = NaturalAlertRed)
                            }
                        }
                        is ImageGenUiState.Success -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    bitmap = state.bitmap.asImageBitmap(),
                                    contentDescription = "Generated Safety Poster",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, NaturalBorderGray, RoundedCornerShape(16.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NaturalPrimaryGreen, modifier = Modifier.size(16.dp))
                                    Text("Visual Generated successfully ($selectedSize resolution)", fontSize = 11.sp, color = NaturalTextSlate, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
