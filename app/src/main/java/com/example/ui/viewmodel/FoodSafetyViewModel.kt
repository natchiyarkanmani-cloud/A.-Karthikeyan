package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.ImageConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.api.ThinkingConfig
import com.example.data.local.AppDatabase
import com.example.data.local.FoodSafetyRepository
import com.example.data.local.ScanHistory
import com.example.data.local.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AssistantUiState {
    object Idle : AssistantUiState
    object Loading : AssistantUiState
    data class Success(val answer: String) : AssistantUiState
    data class Error(val message: String) : AssistantUiState
}

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Loading : ScanUiState
    data class Success(
        val productName: String,
        val fssaiRating: String,
        val preservatives: String,
        val addedIngredients: String,
        val healthCompatibilityTips: String,
        val barcode: String
    ) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

sealed interface PlacesUiState {
    object Idle : PlacesUiState
    object Loading : PlacesUiState
    data class Success(val restaurantsResult: String) : PlacesUiState
    data class Error(val message: String) : PlacesUiState
}

sealed interface ImageGenUiState {
    object Idle : ImageGenUiState
    object Loading : ImageGenUiState
    data class Success(val bitmap: Bitmap) : ImageGenUiState
    data class Error(val message: String) : ImageGenUiState
}

class FoodSafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodSafetyRepository

    val userProfile: StateFlow<UserProfile?>
    val scanHistory: StateFlow<List<ScanHistory>>

    private val _assistantState = MutableStateFlow<AssistantUiState>(AssistantUiState.Idle)
    val assistantState: StateFlow<AssistantUiState> = _assistantState.asStateFlow()

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _placesState = MutableStateFlow<PlacesUiState>(PlacesUiState.Idle)
    val placesState: StateFlow<PlacesUiState> = _placesState.asStateFlow()

    private val _imageGenState = MutableStateFlow<ImageGenUiState>(ImageGenUiState.Idle)
    val imageGenState: StateFlow<ImageGenUiState> = _imageGenState.asStateFlow()

    // UI Input states
    val assistantChatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList()) // Pair of (role, text)
    val useHighThinking = MutableStateFlow(true)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FoodSafetyRepository(database.userProfileDao(), database.scanHistoryDao())
        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )
        scanHistory = repository.scanHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate default user profile if none exists
        viewModelScope.launch {
            val current = repository.userProfile.firstOrNull()
            if (current == null) {
                repository.saveUserProfile(UserProfile())
            }
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun clearScanHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // AI Assistant: Ask questions incorporating health info
    fun askAssistant(query: String) {
        viewModelScope.launch {
            _assistantState.value = AssistantUiState.Loading
            assistantChatHistory.value = assistantChatHistory.value + ("user" to query)

            val profile = userProfile.value
            val healthContext = if (profile != null) {
                "User Health Context:\n" +
                "- Name: ${profile.name}\n" +
                "- Gender: ${profile.gender}\n" +
                "- Age: ${profile.age} years old\n" +
                "- Height: ${profile.height} cm\n" +
                "- Weight: ${profile.weight} kg\n" +
                "- Health Issues / Allergies: ${profile.healthIssues}\n" +
                "Provide personalized answers using these details where relevant. Always cite reputable health guidelines (like WHO, FDA, or FSSAI)."
            } else {
                "Always cite reputable health guidelines (like WHO, FDA, or FSSAI) for safety."
            }

            // High Thinking Mode uses gemini-3.1-pro-preview with thinkingLevel = HIGH
            val modelName = if (useHighThinking.value) "gemini-3.1-pro-preview" else "gemini-3.5-flash"
            val config = if (useHighThinking.value) {
                GenerationConfig(
                    thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH"),
                    temperature = 1.0f // Let model generate freely
                )
            } else {
                GenerationConfig(temperature = 0.5f)
            }

            val systemPrompt = "You are Eatrite AI, an expert Food Security & Safety Assistant. " +
                    "Your mission is to guide users about added ingredients, preservatives, shelf stability, " +
                    "indicators of spoilage, temperature safety limits, and FSSAI standards. " +
                    "Explain the scientific risks associated with temperature and storage. $healthContext"

            val contentsList = mutableListOf<Content>()
            // Include recent chat history
            assistantChatHistory.value.takeLast(10).forEach { (role, text) ->
                contentsList.add(Content(parts = listOf(Part(text = text)), role = role))
            }

            val request = GenerateContentRequest(
                contents = contentsList,
                generationConfig = config,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val response = RetrofitClient.service.generateContent(modelName, apiKey, request)
                val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Eatrite AI could not generate an answer. Please try again."

                assistantChatHistory.value = assistantChatHistory.value + ("model" to answer)
                _assistantState.value = AssistantUiState.Success(answer)
            } catch (e: Exception) {
                Log.e("FoodSafetyViewModel", "Error in assistant query", e)
                _assistantState.value = AssistantUiState.Error(e.message ?: "Network error")
            }
        }
    }

    // Barcode Scanning / Label Analysis
    fun scanBarcodeOrProduct(barcode: String, customName: String? = null) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Loading

            val profile = userProfile.value
            val healthIssues = profile?.healthIssues ?: "No issues listed"

            val prompt = """
                Analyze this food product for food security, ingredients safety, and health suitability.
                Product Name / Barcode: ${customName ?: barcode}
                
                Please return a structured summary with these fields clearly separated (in clear headers/markdown):
                1. Product Name
                2. FSSAI / Safety Rating (Explain with a clear rating from 1 to 5 stars, e.g., ⭐⭐⭐⭐)
                3. Added Ingredients list
                4. Preservatives (Specifically highlight synthetic, chemical, or health-sensitive ones)
                5. Health Compatibility and Recommendations (Explain how safe this product is for a person with health issues/allergies: "$healthIssues")
                
                Keep the tone professional, objective, and informative. Cite standard food safety parameters.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                val output = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Failed to analyze product."

                // Parse fields roughly using regex or display the rich markdown nicely.
                // To keep it robust, we'll parse the sections or pass them beautifully.
                val parsedName = parseSection(output, "Product Name") ?: customName ?: "Product $barcode"
                val parsedRating = parseSection(output, "FSSAI") ?: "⭐⭐⭐ (3/5)"
                val parsedPreservatives = parseSection(output, "Preservatives") ?: "Not highlighted or none found"
                val parsedIngredients = parseSection(output, "Added Ingredients") ?: "Check product label"
                val parsedTips = parseSection(output, "Health Compatibility") ?: "Safe to consume in moderation"

                val scanResult = ScanUiState.Success(
                    productName = parsedName,
                    fssaiRating = parsedRating,
                    preservatives = parsedPreservatives,
                    addedIngredients = parsedIngredients,
                    healthCompatibilityTips = parsedTips,
                    barcode = barcode
                )

                _scanState.value = scanResult

                // Save to Room DB scan history
                repository.addScanHistory(
                    ScanHistory(
                        barcode = barcode,
                        productName = parsedName,
                        fssaiRating = parsedRating,
                        preservatives = parsedPreservatives,
                        addedIngredients = parsedIngredients
                    )
                )

            } catch (e: Exception) {
                Log.e("FoodSafetyViewModel", "Error scanning label", e)
                _scanState.value = ScanUiState.Error(e.message ?: "Failed to connect")
            }
        }
    }

    // Analyze Uploaded Ingredient Image using gemini-3.1-pro-preview
    fun analyzeIngredientImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Loading

            val profile = userProfile.value
            val healthIssues = profile?.healthIssues ?: "No issues listed"

            // Helper to encode image
            val base64Image = withContext(Dispatchers.IO) {
                val byteStream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteStream)
                Base64.encodeToString(byteStream.toByteArray(), Base64.NO_WRAP)
            }

            val prompt = """
                You are looking at a photo of a food label, ingredient list, or food product.
                Analyze the visible ingredients, preservatives, and allergens.
                
                Please return a structured summary with these fields clearly separated (in clear markdown):
                1. Product Name (Estimate from label image)
                2. FSSAI / Safety Rating (Estimated rating from 1 to 5 stars, e.g., ⭐⭐⭐)
                3. Added Ingredients list
                4. Preservatives (Specifically highlight synthetic, chemical, or health-sensitive ones)
                5. Health Compatibility and Recommendations (Explain suitability for a person with health issues/allergies: "$healthIssues")
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                )
            )

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                // Analyze images requires gemini-3.1-pro-preview
                val response = RetrofitClient.service.generateContent("gemini-3.1-pro-preview", apiKey, request)
                val output = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Could not analyze label photo."

                val parsedName = parseSection(output, "Product Name") ?: "Analyzed Food Label"
                val parsedRating = parseSection(output, "FSSAI") ?: "⭐⭐⭐⭐"
                val parsedPreservatives = parseSection(output, "Preservatives") ?: "Extracted preservatives"
                val parsedIngredients = parseSection(output, "Added Ingredients") ?: "Extracted ingredients"
                val parsedTips = parseSection(output, "Health Compatibility") ?: "Matches diet parameters"

                _scanState.value = ScanUiState.Success(
                    productName = parsedName,
                    fssaiRating = parsedRating,
                    preservatives = parsedPreservatives,
                    addedIngredients = parsedIngredients,
                    healthCompatibilityTips = parsedTips,
                    barcode = "IMAGE_UPLOAD"
                )

                // Save to scan history
                repository.addScanHistory(
                    ScanHistory(
                        barcode = "PHOTO_SCAN",
                        productName = parsedName,
                        fssaiRating = parsedRating,
                        preservatives = parsedPreservatives,
                        addedIngredients = parsedIngredients
                    )
                )

            } catch (e: Exception) {
                Log.e("FoodSafetyViewModel", "Error analyzing label image", e)
                _scanState.value = ScanUiState.Error(e.message ?: "Failed to process photo")
            }
        }
    }

    // Dine Out: Search locations with Google Maps Grounding tool
    fun searchRestaurants(location: String) {
        viewModelScope.launch {
            _placesState.value = PlacesUiState.Loading

            val prompt = """
                Search for popular dining places or restaurants in or near "$location". 
                For each restaurant, fetch and explain:
                - Name
                - Location details / Address
                - Google Customer Ratings and reviews details
                - Food safety indicators or popular AI assessments of their hygiene/safety standards (like typical reviews mentioning food handling, temperature, freshness, or hygiene certificates).
                
                Format this as a clean, engaging list with markdown.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                tools = listOf(mapOf("googleMaps" to emptyMap<String, Any>())) // Google Maps Grounding Tool
            )

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                // Maps Grounding requires gemini-3.5-flash
                val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
                val output = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "No restaurant data returned."

                _placesState.value = PlacesUiState.Success(output)
            } catch (e: Exception) {
                Log.e("FoodSafetyViewModel", "Error fetching places grounding", e)
                _placesState.value = PlacesUiState.Error(e.message ?: "Failed to search dining places")
            }
        }
    }

    // Generate Visual Safety Infographic / Meal Image
    fun generateSafetyImage(prompt: String, size: String) {
        viewModelScope.launch {
            _imageGenState.value = ImageGenUiState.Loading

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = "A professional food safety infographic or high quality visual representing: $prompt. Soft, clean natural tones, educational and crisp.")))),
                generationConfig = GenerationConfig(
                    imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = size),
                    responseModalities = listOf("TEXT", "IMAGE")
                )
            )

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val response = RetrofitClient.service.generateContent("gemini-3-pro-image-preview", apiKey, request)
                
                // Find image part in the candidate's parts
                val inlinePart = response.candidates?.flatMap { it.content?.parts ?: emptyList() }
                    ?.firstOrNull { it.inlineData != null }

                if (inlinePart?.inlineData != null) {
                    val base64Data = inlinePart.inlineData.data
                    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (bitmap != null) {
                        _imageGenState.value = ImageGenUiState.Success(bitmap)
                    } else {
                        _imageGenState.value = ImageGenUiState.Error("Failed to decode generated image bytes.")
                    }
                } else {
                    _imageGenState.value = ImageGenUiState.Error("No image was generated by the model. Make sure you entered a descriptive visual prompt.")
                }
            } catch (e: Exception) {
                Log.e("FoodSafetyViewModel", "Error generating safety image", e)
                _imageGenState.value = ImageGenUiState.Error(e.message ?: "Failed to generate safety graphic")
            }
        }
    }

    private fun parseSection(text: String, sectionName: String): String? {
        // Simple helper to parse section lines from AI markdown
        val lines = text.lines()
        val index = lines.indexOfFirst { it.contains(sectionName, ignoreCase = true) }
        if (index != -1) {
            val contentLines = mutableListOf<String>()
            for (i in (index + 1) until lines.size) {
                val line = lines[i].trim()
                if (line.startsWith("#") || (line.contains(":") && line.substringBefore(":").length < 25 && (line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.") || line.startsWith("4.") || line.startsWith("5.")))) {
                    break
                }
                if (line.isNotEmpty()) {
                    contentLines.add(line)
                }
            }
            if (contentLines.isNotEmpty()) {
                return contentLines.joinToString("\n").replace(Regex("^[-*•]\\s*"), "")
            }
        }
        return null
    }
}
