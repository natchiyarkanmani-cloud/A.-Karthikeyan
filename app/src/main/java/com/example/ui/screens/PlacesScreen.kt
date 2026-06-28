package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodSafetyViewModel
import com.example.ui.viewmodel.PlacesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    viewModel: FoodSafetyViewModel
) {
    var locationInput by remember { mutableStateOf("") }
    val placesState by viewModel.placesState.collectAsState()

    val popularLocations = listOf(
        "Connaught Place, New Delhi",
        "HSR Layout, Bangalore",
        "Bandra West, Mumbai",
        "Powell St, San Francisco"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Heading block
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Dine Out Safe",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
                Text(
                    text = "Search dining options anywhere with Google Ratings & AI Food-Handling audit.",
                    fontSize = 13.sp,
                    color = NaturalTextMuted
                )
            }
        }

        // Search Form Card
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
                    Text(
                        text = "Explore Dining Locations",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextPrimary
                    )

                    // Popular Locations
                    Text(
                        text = "Quick Locations:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularLocations.take(2).forEach { loc ->
                            Box(
                                modifier = Modifier
                                    .background(NaturalAccentGreenLight, RoundedCornerShape(12.dp))
                                    .clickable {
                                        locationInput = loc
                                        viewModel.searchRestaurants(loc)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = loc.substringBefore(","),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalPrimaryGreen
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        placeholder = { Text("Enter locality or city (e.g. Bandra, Mumbai)", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("places_location_input"),
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
                            if (locationInput.isNotBlank()) {
                                viewModel.searchRestaurants(locationInput)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("search_places_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimaryGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search Nearby Places", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // States results view
        item {
            when (val state = placesState) {
                is PlacesUiState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NaturalAccentGreenLight),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = NaturalPrimaryGreen)
                            Text(
                                text = "Querying live Google Maps database...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalPrimaryGreen,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Gemini is searching restaurants, fetching live people ratings, and performing safety/cleanliness reviews audit.",
                                fontSize = 11.sp,
                                color = NaturalTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is PlacesUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NaturalAlertRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = NaturalAlertRed)
                            Column {
                                Text("Search Error", fontWeight = FontWeight.Bold, color = NaturalAlertRed, fontSize = 14.sp)
                                Text(state.message, fontSize = 12.sp, color = NaturalTextSlate)
                            }
                        }
                    }
                }
                is PlacesUiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, NaturalBorderGray),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(NaturalAccentGreenSoft, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = NaturalPrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Google & AI Grounded Safety Report",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalTextPrimary
                                )
                            }

                            Divider(color = NaturalBorderGray)

                            // Display rich markdown text parsed beautifully
                            Text(
                                text = state.restaurantsResult,
                                fontSize = 13.sp,
                                color = NaturalTextPrimary,
                                lineHeight = 19.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                is PlacesUiState.Idle -> {
                    // Show introductory tips
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NaturalAccentGreenLight.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = NaturalPrimaryGreen)
                                Text("Why Grounding?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NaturalPrimaryGreen)
                            }
                            Text(
                                text = "Grounded Places Search leverages live Google search data and Google Maps API toolsets to extract real, up-to-date information on restaurant ratings, reviews, menu safety, hygiene certificates, and location coordinates instantly.",
                                fontSize = 12.sp,
                                color = NaturalTextPrimary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
