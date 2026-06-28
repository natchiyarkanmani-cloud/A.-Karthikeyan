package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FoodSafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: FoodSafetyViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val userProfile by viewModel.userProfile.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .height(84.dp)
                    .background(Color.White)
                    .testTag("app_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = if (selectedTab == 0) Icons.Default.Home else Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalPrimaryGreen,
                        selectedTextColor = NaturalPrimaryGreen,
                        unselectedIconColor = NaturalTextMuted,
                        unselectedTextColor = NaturalTextMuted,
                        indicatorColor = NaturalAccentGreenMedium
                    ),
                    modifier = Modifier.testTag("nav_home_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = if (selectedTab == 1) Icons.Default.QrCodeScanner else Icons.Default.QrCodeScanner, contentDescription = "Scanner") },
                    label = { Text("Scanner", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalPrimaryGreen,
                        selectedTextColor = NaturalPrimaryGreen,
                        unselectedIconColor = NaturalTextMuted,
                        unselectedTextColor = NaturalTextMuted,
                        indicatorColor = NaturalAccentGreenMedium
                    ),
                    modifier = Modifier.testTag("nav_scanner_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = if (selectedTab == 2) Icons.Default.Explore else Icons.Default.Explore, contentDescription = "Dine Out") },
                    label = { Text("Dine Out", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalPrimaryGreen,
                        selectedTextColor = NaturalPrimaryGreen,
                        unselectedIconColor = NaturalTextMuted,
                        unselectedTextColor = NaturalTextMuted,
                        indicatorColor = NaturalAccentGreenMedium
                    ),
                    modifier = Modifier.testTag("nav_dine_out_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(imageVector = if (selectedTab == 3) Icons.Default.AutoAwesome else Icons.Default.AutoAwesome, contentDescription = "Deep AI") },
                    label = { Text("Deep AI", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalPrimaryGreen,
                        selectedTextColor = NaturalPrimaryGreen,
                        unselectedIconColor = NaturalTextMuted,
                        unselectedTextColor = NaturalTextMuted,
                        indicatorColor = NaturalAccentGreenMedium
                    ),
                    modifier = Modifier.testTag("nav_deep_ai_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(imageVector = if (selectedTab == 4) Icons.Default.AccountCircle else Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalPrimaryGreen,
                        selectedTextColor = NaturalPrimaryGreen,
                        unselectedIconColor = NaturalTextMuted,
                        unselectedTextColor = NaturalTextMuted,
                        indicatorColor = NaturalAccentGreenMedium
                    ),
                    modifier = Modifier.testTag("nav_profile_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    userProfile = userProfile,
                    onNavigateToScan = { selectedTab = 1 },
                    onNavigateToPlaces = { selectedTab = 2 },
                    onNavigateToProfile = { selectedTab = 4 },
                    onNavigateToAssistant = { selectedTab = 3 }
                )
                1 -> ScanScreen(
                    viewModel = viewModel,
                    scanHistory = scanHistory
                )
                2 -> PlacesScreen(
                    viewModel = viewModel
                )
                3 -> AssistantScreen(
                    viewModel = viewModel
                )
                4 -> ProfileScreen(
                    viewModel = viewModel,
                    currentProfile = userProfile
                )
            }
        }
    }
}
