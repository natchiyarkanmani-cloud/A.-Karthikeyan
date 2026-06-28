package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ScanHistory
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScanUiState
import com.example.ui.viewmodel.FoodSafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: FoodSafetyViewModel,
    scanHistory: List<ScanHistory>
) {
    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsState()
    var manualInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Predefined items list
    val presetProducts = listOf(
        "8901058002315" to "Mero Maggi Noodles",
        "5449000000996" to "Diet Coke",
        "0123456789012" to "Organic Peanut Butter",
        "8000500224326" to "Kinder Joy Chocolate",
        "8901719101032" to "Kurkure Masala Munch"
    )

    // Image Picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                selectedBitmap = bitmap
                // Automatically run image analysis using gemini-3.1-pro-preview
                viewModel.analyzeIngredientImage(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Title block
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Label & Barcode Analyzer",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
                Text(
                    text = "Verify additives, preservatives, and FSSAI health ratings of any product instantly.",
                    fontSize = 13.sp,
                    color = NaturalTextMuted
                )
            }
        }

        // --- SCAN OPTIONS TAB ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NaturalBorderGray),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "1. Select or Enter Product",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextPrimary
                    )

                    // Lazy row of preset scanner presets
                    Text(
                        text = "Quick Presets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextMuted,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetProducts) { (code, name) ->
                            Box(
                                modifier = Modifier
                                    .background(NaturalAccentGreenLight, RoundedCornerShape(12.dp))
                                    .clickable {
                                        manualInput = name
                                        viewModel.scanBarcodeOrProduct(code, name)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = NaturalPrimaryGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalPrimaryGreen
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = NaturalBorderGray)

                    // Manual Product Name or Barcode Search
                    Text(
                        text = "Or type Product Name or Barcode:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextMuted
                    )

                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        placeholder = { Text("e.g. Hershey's Cocoa Powder or 034000010077", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_barcode_input"),
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
                            if (manualInput.isNotBlank()) {
                                viewModel.scanBarcodeOrProduct("MANUAL_CODE", manualInput)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_scan_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimaryGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Product", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // --- IMAGE PHOTO SCANNER (Analyze Images Gemini 3.1 Pro) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Or Upload Ingredient Label Photo:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, NaturalPrimaryGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalPrimaryGreen),
                            modifier = Modifier.testTag("upload_label_photo_button")
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- LOADING / STATE DISPLAY ---
        item {
            when (val state = scanState) {
                is ScanUiState.Loading -> {
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
                                text = "Eatrite AI is processing product...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalPrimaryGreen,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Decoding ingredients, highlighting stabilizers/preservatives, checking allergen lists and matching user profile.",
                                fontSize = 11.sp,
                                color = NaturalTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is ScanUiState.Error -> {
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
                                Text("Analysis Failed", fontWeight = FontWeight.Bold, color = NaturalAlertRed, fontSize = 14.sp)
                                Text(state.message, fontSize = 12.sp, color = NaturalTextSlate)
                            }
                        }
                    }
                }
                is ScanUiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, NaturalPrimaryGreen),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header product name & FSSAI rating badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = state.productName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalTextPrimary
                                    )
                                    Text(
                                        text = "Code: ${state.barcode}",
                                        fontSize = 11.sp,
                                        color = NaturalTextMuted
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(NaturalAccentGreenMedium, RoundedCornerShape(100.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = state.fssaiRating,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalTextPrimary
                                    )
                                }
                            }

                            Divider(color = NaturalBorderGray)

                            // Ingredients section
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.List, contentDescription = null, tint = NaturalPrimaryGreen, modifier = Modifier.size(16.dp))
                                    Text("Added Ingredients:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NaturalTextPrimary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(state.addedIngredients, fontSize = 12.sp, color = NaturalTextSlate, lineHeight = 16.sp)
                            }

                            // Preservatives section
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = NaturalPrimaryGreen, modifier = Modifier.size(16.dp))
                                    Text("Preservatives & Additives:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NaturalTextPrimary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(state.preservatives, fontSize = 12.sp, color = NaturalTextSlate, lineHeight = 16.sp)
                            }

                            // Dietary & Health suitability (Personalized)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NaturalAccentGreenLight, RoundedCornerShape(16.dp))
                                    .border(1.dp, NaturalAccentGreenSoft, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Healing, contentDescription = null, tint = NaturalPrimaryGreen, modifier = Modifier.size(16.dp))
                                    Text("Health Security Advisory (Personalized):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NaturalPrimaryGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(state.healthCompatibilityTips, fontSize = 12.sp, color = NaturalTextPrimary, lineHeight = 17.sp)
                            }
                        }
                    }
                }
                is ScanUiState.Idle -> {
                    // Instruction layout or blank
                }
            }
        }

        // --- HISTORY OF PREVIOUS SCANS ---
        if (scanHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Scan Logs",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextPrimary
                    )
                    TextButton(
                        onClick = { viewModel.clearScanHistory() },
                        colors = ButtonDefaults.textButtonColors(contentColor = NaturalAlertRed)
                    ) {
                        Text("Clear Logs", fontSize = 12.sp)
                    }
                }
            }

            items(scanHistory.take(8)) { scan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.scanBarcodeOrProduct(scan.barcode, scan.productName)
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, NaturalBorderGray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = scan.productName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalTextPrimary
                            )
                            Text(
                                text = "Code: ${scan.barcode}",
                                fontSize = 10.sp,
                                color = NaturalTextMuted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(NaturalAccentGreenSoft, RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = scan.fssaiRating,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
