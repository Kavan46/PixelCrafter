package com.example.viewmodel

import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.local.WallpaperDatabase
import com.example.data.model.Wallpaper
import com.example.data.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WallpaperDatabase.getDatabase(application, viewModelScope)
    private val repository = WallpaperRepository(db.wallpaperDao())

    // Base user details
    val accountName = MutableStateFlow("PixelCrafter Creator")
    val accountEmail = MutableStateFlow("pixelcrafter@example.com")
    val isGoogleConnected = MutableStateFlow(true)
    val isEmailPasswordSetup = MutableStateFlow(false)

    // UI States
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = combine(accountEmail, _isAdminMode) { email, manual ->
        email.equals("kmatrixstudio@gmail.com", ignoreCase = true) || manual
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    private val _addWallpaperStatus = MutableStateFlow<String?>(null)
    val addWallpaperStatus: StateFlow<String?> = _addWallpaperStatus.asStateFlow()

    private val _operationLoading = MutableStateFlow(false)
    val operationLoading: StateFlow<Boolean> = _operationLoading.asStateFlow()

    // Real Firebase Authentication Service
    val authService = com.example.data.FirebaseAuthService(application)
    val firebaseDbService = com.example.data.FirebaseDatabaseService(application, repository, viewModelScope)
    val firebaseFirestoreService = com.example.data.FirebaseFirestoreService(application, repository, viewModelScope)

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            authService.currentUserFlow.collect { firebaseUser ->
                if (firebaseUser != null) {
                    accountEmail.value = firebaseUser.email ?: ""
                    accountName.value = firebaseUser.displayName ?: "PixelCrafter Member"
                    isGoogleConnected.value = firebaseUser.providerData.any { it.providerId == "google.com" }
                    isEmailPasswordSetup.value = firebaseUser.providerData.any { it.providerId == "password" }
                    if (firebaseFirestoreService.isFirebaseInitialized) {
                        firebaseFirestoreService.startSyncingFavorites(firebaseUser.uid)
                    }
                } else {
                    // Reset if signed out from Firebase
                    accountName.value = "Guest Creator"
                    accountEmail.value = "guest@pixelcrafter.org"
                    isGoogleConnected.value = false
                    isEmailPasswordSetup.value = false
                }
            }
        }
    }

    // Dynamic Theme Selector state ("Midnight Cosmic", "Cyberpunk Neon", "Emerald Minimalist")
    val selectedTheme = MutableStateFlow("Midnight Cosmic")

    fun updateTheme(theme: String) {
        selectedTheme.value = theme
    }

    fun setupEmailPassword(password: String) {
        if (password.length >= 6) {
            isEmailPasswordSetup.value = true
        }
    }

    fun toggleGoogleConnection(connected: Boolean) {
        isGoogleConnected.value = connected
        if (connected && accountEmail.value == "guest@pixelcrafter.org") {
            accountName.value = "Google Voyager"
            accountEmail.value = "voyager.google@gmail.com"
        }
    }

    fun signUpWithEmailFirebase(email: String, username: String, pass: String) {
        viewModelScope.launch {
            _operationLoading.value = true
            _authError.value = null
            when (val res = authService.signUpWithEmail(email, pass)) {
                is com.example.data.AuthResult.Success -> {
                    if (username.isNotBlank()) {
                        authService.updateDisplayName(username)
                    }
                    accountName.value = username
                    accountEmail.value = email
                    isEmailPasswordSetup.value = true
                    Toast.makeText(getApplication(), "Logged in successfully!", Toast.LENGTH_SHORT).show()
                }
                is com.example.data.AuthResult.Error -> {
                    _authError.value = res.message
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_LONG).show()
                }
            }
            _operationLoading.value = false
        }
    }

    fun signInWithEmailFirebase(email: String, pass: String) {
        viewModelScope.launch {
            _operationLoading.value = true
            _authError.value = null
            when (val res = authService.signInWithEmail(email, pass)) {
                is com.example.data.AuthResult.Success -> {
                    val user = res.user
                    if (user != null) {
                        accountEmail.value = user.email ?: email
                        accountName.value = user.displayName ?: "Authorized User"
                    }
                    isEmailPasswordSetup.value = true
                    Toast.makeText(getApplication(), "Logged in successfully!", Toast.LENGTH_SHORT).show()
                }
                is com.example.data.AuthResult.Error -> {
                    _authError.value = res.message
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_LONG).show()
                }
            }
            _operationLoading.value = false
        }
    }

    fun signInWithGoogleTokenFirebase(idToken: String) {
        viewModelScope.launch {
            _operationLoading.value = true
            _authError.value = null
            when (val res = authService.signInWithGoogleIdToken(idToken)) {
                is com.example.data.AuthResult.Success -> {
                    val user = res.user
                    if (user != null) {
                        accountEmail.value = user.email ?: "googleuser@gmail.com"
                        accountName.value = user.displayName ?: "Google User"
                    }
                    isGoogleConnected.value = true
                    Toast.makeText(getApplication(), "Logged in successfully!", Toast.LENGTH_SHORT).show()
                }
                is com.example.data.AuthResult.Error -> {
                    _authError.value = res.message
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_LONG).show()
                }
            }
            _operationLoading.value = false
        }
    }

    fun signOut() {
        if (authService.isFirebaseInitialized) {
            authService.signOut()
        }
        accountName.value = "Guest Creator"
        accountEmail.value = "guest@pixelcrafter.org"
        isGoogleConnected.value = false
        isEmailPasswordSetup.value = false
        _isAdminMode.value = false
        Toast.makeText(getApplication(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
    }

    fun signIn(username: String, email: String) {
        if (username.isNotBlank()) accountName.value = username
        if (email.isNotBlank()) accountEmail.value = email
        isGoogleConnected.value = false
        isEmailPasswordSetup.value = true
        Toast.makeText(getApplication(), "Logged in successfully!", Toast.LENGTH_SHORT).show()
    }

    // Dynamic user-created categories state flow to supplement DB-derived categories
    private val _customCategories = MutableStateFlow<List<String>>(emptyList())

    // Master categories list derived dynamically from database wallpapers and newly uploaded categories
    val categoriesState: StateFlow<List<String>> = combine(
        repository.allWallpapers,
        _customCategories,
        firebaseFirestoreService.customCategories
    ) { all, custom, customFirestore ->
        val dynamicCategories = all.map { it.category }.distinct().filter { it.isNotBlank() && !it.equals("All", ignoreCase = true) }
        (listOf("All") + dynamicCategories + custom + customFirestore).distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = listOf("All")
    )

    val categories: List<String>
        get() = categoriesState.value

    fun addCategory(category: String): Boolean {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return false
        // Avoid duplicate additions
        if (categoriesState.value.any { it.equals(trimmed, ignoreCase = true) }) return false
        
        if (firebaseFirestoreService.isFirebaseInitialized) {
            firebaseFirestoreService.uploadCategory(trimmed)
        } else {
            _customCategories.value = _customCategories.value + trimmed
        }
        return true
    }

    fun removeCategory(category: String): Boolean {
        // Protect "All" category from being deleted
        if (category.equals("All", ignoreCase = true)) return false
        
        if (firebaseFirestoreService.isFirebaseInitialized) {
            firebaseFirestoreService.deleteCategoryFromFirestore(category)
        } else {
            _customCategories.value = _customCategories.value.filter { !it.equals(category, ignoreCase = true) }
        }
        
        // If current selected category was the one deleted, fall back to "All"
        if (_selectedCategory.value.equals(category, ignoreCase = true)) {
            _selectedCategory.value = "All"
        }
        return true
    }

    // Reactive Wallpapers Feed with enhanced search filtering by category and metadata
    val wallpapers: StateFlow<List<Wallpaper>> = combine(
        repository.allWallpapers,
        _selectedCategory,
        _searchQuery
    ) { all, category, query ->
        var list = all
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.id.toString().contains(query) ||
                        (it.isCustom && "custom".contains(query, ignoreCase = true)) ||
                        (it.isFavorite && "favorite".contains(query, ignoreCase = true))
            }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Favorites List
    val favorites: StateFlow<List<Wallpaper>> = repository.favoriteWallpapers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
    }

    fun clearAddWallpaperStatus() {
        _addWallpaperStatus.value = null
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            val newFavStatus = !wallpaper.isFavorite
            repository.toggleFavorite(wallpaper.id, newFavStatus)
            if (firebaseFirestoreService.isFirebaseInitialized) {
                val userId = authService.currentUser?.uid ?: "anonymous"
                if (newFavStatus) {
                    firebaseFirestoreService.uploadFavorite(userId, wallpaper.id)
                } else {
                    firebaseFirestoreService.removeFavorite(userId, wallpaper.id)
                }
            }
        }
    }

    fun deleteWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.deleteWallpaper(wallpaper)
            if (firebaseFirestoreService.isFirebaseInitialized) {
                firebaseFirestoreService.deleteWallpaperFromFirebase(wallpaper.id)
            }
        }
    }

    fun updateWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.updateWallpaper(wallpaper)
            if (firebaseFirestoreService.isFirebaseInitialized) {
                firebaseFirestoreService.uploadWallpaper(wallpaper)
            }
        }
    }

    // Add high resolution wallpapers (Admin mode) - Supports local file paths and base64 strings
    fun addWallpaper(title: String, category: String, url: String, author: String, firebaseSyncUrl: String? = null) {
        if (title.isBlank() || url.isBlank() || category.isBlank()) {
            _addWallpaperStatus.value = "Error: All fields are required to craft."
            return
        }
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://") && !url.startsWith("data:")) {
            _addWallpaperStatus.value = "Error: Must provide a valid image URL, file link or pick from gallery."
            return
        }

        viewModelScope.launch {
            try {
                val newWallpaper = Wallpaper(
                    title = title,
                    category = category,
                    imageUrl = url,
                    author = author.ifBlank { "PixelCrafter Admin" },
                    downloads = (100..999).random(),
                    isCustom = true
                )
                val generatedId = repository.insertWallpaper(newWallpaper).toInt()
                if (firebaseFirestoreService.isFirebaseInitialized) {
                    val finalFirebaseUrl = firebaseSyncUrl ?: url
                    val finalWp = newWallpaper.copy(id = generatedId, imageUrl = finalFirebaseUrl)
                    firebaseFirestoreService.uploadWallpaper(finalWp)
                    val adminId = authService.currentUser?.uid ?: "admin"
                    firebaseFirestoreService.uploadToAdminStream(generatedId, title, finalFirebaseUrl, adminId)
                }
                _addWallpaperStatus.value = "Success: Wallpaper '$title' was crafted successfully!"
            } catch (e: Exception) {
                _addWallpaperStatus.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    // Process selected local Uri: copies the image to internal persistent storage and generates a resized Base64 version for syncing.
    fun processSelectedImage(uri: Uri, callback: (localPath: String, base64String: String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                // 1. Copy image to a persistent local file
                val customFolder = File(context.filesDir, "custom_wallpapers")
                if (!customFolder.exists()) {
                    customFolder.mkdirs()
                }
                val localFile = File(customFolder, "wp_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(localFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val localPath = "file://${localFile.absolutePath}"

                // 2. Generate a compact resized jpeg base64 representation for Firebase sync
                var base64Result: String? = null
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val options = android.graphics.BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        android.graphics.BitmapFactory.decodeStream(input, null, options)
                        
                        // Target about 300px limit for efficient Realtime DB storage
                        var scale = 1
                        val limit = 320
                        while (options.outWidth / scale / 2 >= limit || options.outHeight / scale / 2 >= limit) {
                            scale *= 2
                        }
                        
                        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                            inSampleSize = scale
                        }
                        
                        context.contentResolver.openInputStream(uri)?.use { nextInput ->
                            val decoded = android.graphics.BitmapFactory.decodeStream(nextInput, null, decodeOptions)
                            if (decoded != null) {
                                val outStream = java.io.ByteArrayOutputStream()
                                decoded.compress(Bitmap.CompressFormat.JPEG, 75, outStream)
                                val byteArray = outStream.toByteArray()
                                val encoded = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
                                base64Result = "data:image/jpeg;base64,$encoded"
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                withContext(Dispatchers.Main) {
                    callback(localPath, base64Result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Failed to process selected image.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun resolveImageModel(url: String): Any {
        return if (url.startsWith("data:image/jpeg;base64,") || url.startsWith("data:image/png;base64,")) {
            val base64Data = url.substringAfter("base64,")
            try {
                android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                url
            }
        } else if (url.startsWith("file://") || url.startsWith("content://") || url.startsWith("http://") || url.startsWith("https://")) {
            try {
                android.net.Uri.parse(url)
            } catch (e: Exception) {
                url
            }
        } else {
            url
        }
    }

    // Helper functions for Set Wallpaper, Download and Share
    // Fetch and decode image using Coil's ImageLoader
    private suspend fun fetchBitmapFromUrl(context: Context, url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(resolveImageModel(url))
                    .allowHardware(false) // Required to draw to Canvas or load into WallpaperManager
                    .build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    (result.drawable as? BitmapDrawable)?.bitmap
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun setWallpaper(context: Context, wallpaper: Wallpaper, home: Boolean, lock: Boolean) {
        _operationLoading.value = true
        viewModelScope.launch {
            val bitmap = fetchBitmapFromUrl(context, wallpaper.imageUrl)
            if (bitmap != null) {
                val wallpaperManager = WallpaperManager.getInstance(context)
                try {
                    withContext(Dispatchers.IO) {
                        if (home) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                            } else {
                                wallpaperManager.setBitmap(bitmap)
                            }
                        }
                        if (lock) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                            } else {
                                // For API < 24 lockscreen falls back
                                wallpaperManager.setBitmap(bitmap)
                            }
                        }
                        repository.incrementDownloads(wallpaper.id)
                    }
                    Toast.makeText(context, "Wallpaper successfully applied!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error applying: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Failed to download image data. Check network connection.", Toast.LENGTH_LONG).show()
            }
            _operationLoading.value = false
        }
    }

    fun downloadWallpaper(context: Context, wallpaper: Wallpaper) {
        _operationLoading.value = true
        viewModelScope.launch {
            val bitmap = fetchBitmapFromUrl(context, wallpaper.imageUrl)
            if (bitmap != null) {
                val savedFilename = "PixelCrafter_${wallpaper.title.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
                var success = false
                try {
                    withContext(Dispatchers.IO) {
                        val resolver = context.contentResolver
                        // In modern android write to public picture/download dir
                        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val file = File(imagesDir, savedFilename)
                        val out: OutputStream = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.flush()
                        out.close()
                        
                        // Force media scan
                        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        val contentUri = Uri.fromFile(file)
                        mediaScanIntent.setData(contentUri)
                        context.sendBroadcast(mediaScanIntent)
                        
                        repository.incrementDownloads(wallpaper.id)
                        success = true
                    }
                    if (success) {
                        Toast.makeText(context, "Saved to Gallery: Pictures/$savedFilename", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    // Fallback to internal/cache directory saving
                    try {
                        withContext(Dispatchers.IO) {
                            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            val file = File(directory, savedFilename)
                            val out: OutputStream = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            out.flush()
                            out.close()
                            repository.incrementDownloads(wallpaper.id)
                        }
                        Toast.makeText(context, "Saved to Downloads: $savedFilename", Toast.LENGTH_LONG).show()
                    } catch (ex: Exception) {
                        Toast.makeText(context, "Storage Error: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Failed to acquire image. Check connection.", Toast.LENGTH_SHORT).show()
            }
            _operationLoading.value = false
        }
    }

    fun shareWallpaper(context: Context, wallpaper: Wallpaper) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out this wallpaper on PixelCrafter!")
                putExtra(Intent.EXTRA_TEXT, "Hey! I metadata-discovered this beautiful high-definition wallpaper '${wallpaper.title}' by ${wallpaper.author} on PixelCrafter.\n\nView or download: ${wallpaper.imageUrl}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Wallpaper via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening sharing client", Toast.LENGTH_SHORT).show()
        }
    }
}
