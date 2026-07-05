package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.model.Wallpaper
import com.example.data.repository.WallpaperRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseFirestoreService(
    private val context: Context,
    private val repository: WallpaperRepository,
    private val scope: CoroutineScope
) {
    private val TAG = "FirebaseFirestoreService"

    private val _customCategories = MutableStateFlow<List<String>>(emptyList())
    val customCategories: StateFlow<List<String>> = _customCategories.asStateFlow()

    private val _isFetchingWallpapers = MutableStateFlow(false)
    val isFetchingWallpapers: StateFlow<Boolean> = _isFetchingWallpapers.asStateFlow()

    val isFirebaseInitialized: Boolean by lazy {
        try {
            val apps = FirebaseApp.getApps(context)
            apps.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        if (isFirebaseInitialized) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FirebaseFirestore instance", e)
                null
            }
        } else {
            null
        }
    }

    private var wallpapersRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    private fun isAdmin(): Boolean {
        val email = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
        } catch (e: Exception) {
            null
        }
        return email != null && email.equals("kmatrixstudio@gmail.com", ignoreCase = true)
    }

    init {
        if (isFirebaseInitialized) {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener {
                    startSyncingWallpapers()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register FirebaseAuth state listener", e)
                startSyncingWallpapers()
            }
            startSyncingCategories()
        }
    }

    /**
     * Listens to the Firestore "images" collection and keeps the local database updated.
     */
    fun startSyncingWallpapers() {
        val fs = firestore ?: return
        
        wallpapersRegistration?.remove()
        _isFetchingWallpapers.value = true

        val isUserAdmin = isAdmin()
        val query = if (isUserAdmin) {
            fs.collection("images")
        } else {
            fs.collection("images").whereEqualTo("isPublic", true)
        }

        wallpapersRegistration = query.addSnapshotListener { snapshot, error ->
            _isFetchingWallpapers.value = false
            if (error != null) {
                Log.e(TAG, "Error listening to images in Firestore (isAdmin=$isUserAdmin)", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                scope.launch(Dispatchers.IO) {
                    try {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val id = doc.getLong("id")?.toInt() ?: 0
                            if (id <= 0) continue

                            when (change.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    val title = doc.getString("title") ?: ""
                                    val category = doc.getString("category") ?: "All"
                                    val imageUrl = doc.getString("imageUrl") ?: doc.getString("url") ?: ""
                                    val author = doc.getString("author") ?: "Firestore Creator"
                                    val downloads = doc.getLong("downloads")?.toInt() ?: 0
                                    val isCustom = doc.getBoolean("isCustom") ?: true

                                    if (imageUrl.isNotBlank()) {
                                        val existing = repository.getWallpaperById(id)
                                        if (existing == null) {
                                            val newWallpaper = Wallpaper(
                                                id = id,
                                                title = title,
                                                category = category,
                                                imageUrl = imageUrl,
                                                author = author,
                                                downloads = downloads,
                                                isFavorite = false,
                                                isCustom = isCustom
                                            )
                                            repository.insertWallpaper(newWallpaper)
                                        } else {
                                            if (existing.imageUrl != imageUrl || existing.category != category || existing.title != title || existing.author != author) {
                                                repository.updateWallpaper(existing.copy(
                                                    title = title,
                                                    category = category,
                                                    imageUrl = imageUrl,
                                                    author = author
                                                ))
                                            }
                                        }
                                    }
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    val existing = repository.getWallpaperById(id)
                                    if (existing != null) {
                                        repository.deleteWallpaper(existing)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing Firestore images to local database", e)
                    }
                }
            }
        }
    }

    /**
     * Syncs a single wallpaper metadata mapping to the "images" collection.
     */
    fun uploadWallpaper(wallpaper: Wallpaper, isPublic: Boolean = true) {
        val fs = firestore ?: return
        val targetId = if (wallpaper.id == 0) (System.currentTimeMillis().toInt() and 0xfffffff) else wallpaper.id
        val finalWallpaper = wallpaper.copy(id = targetId)

        val currentUser = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        } catch (e: Exception) {
            null
        }
        val uploadedByVal = currentUser?.email ?: currentUser?.uid ?: "admin"

        val data = hashMapOf(
            "id" to finalWallpaper.id,
            "title" to finalWallpaper.title,
            "category" to finalWallpaper.category,
            "imageUrl" to finalWallpaper.imageUrl,
            "url" to finalWallpaper.imageUrl,
            "author" to finalWallpaper.author,
            "downloads" to finalWallpaper.downloads,
            "isCustom" to finalWallpaper.isCustom,
            "timestamp" to Timestamp.now(),
            "createdAt" to Timestamp.now(),
            "uploadedBy" to uploadedByVal,
            "isPublic" to isPublic
        )

        fs.collection("images").document(targetId.toString())
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Wallpaper successfully synced to Firestore 'images' with schema keys!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed syncing wallpaper to Firestore 'images'", e)
            }
    }

    /**
     * Removes a wallpaper document from the "images" collection.
     */
    fun deleteWallpaperFromFirebase(wallpaperId: Int) {
        val fs = firestore ?: return
        fs.collection("images").document(wallpaperId.toString())
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Wallpaper '$wallpaperId' successfully removed from Firestore 'images'!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove wallpaper '$wallpaperId' from Firestore 'images'", e)
            }
    }

    /**
     * Listens to the Firestore "favorites" collection for a specific user and updates local favorites status.
     */
    fun startSyncingFavorites(userId: String) {
        val fs = firestore ?: return
        fs.collection("favorites")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to favorites in Firestore", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val firestoreFavIds = snapshot.documents.mapNotNull { doc ->
                                doc.getLong("wallpaperId")?.toInt()
                            }.toSet()

                            // Sync local favorite states
                            for (favId in firestoreFavIds) {
                                val localWp = repository.getWallpaperById(favId)
                                if (localWp != null && !localWp.isFavorite) {
                                    repository.toggleFavorite(favId, true)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing Firestore favorites to local database", e)
                        }
                    }
                }
            }
    }

    /**
     * Uploads user favorite pairing to the "favorites" collection.
     */
    fun uploadFavorite(userId: String, wallpaperId: Int) {
        val fs = firestore ?: return
        val favId = "${userId}_${wallpaperId}"
        val data = hashMapOf(
            "userId" to userId,
            "wallpaperId" to wallpaperId,
            "timestamp" to Timestamp.now()
        )

        fs.collection("favorites").document(favId)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Favorite registered in Firestore!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to upload favorite to Firestore", e)
            }
    }

    /**
     * Deletes user favorite pairing from the "favorites" collection.
     */
    fun removeFavorite(userId: String, wallpaperId: Int) {
        val fs = firestore ?: return
        val favId = "${userId}_${wallpaperId}"

        fs.collection("favorites").document(favId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Favorite deleted from Firestore!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to delete favorite from Firestore", e)
            }
    }

    /**
     * Feeds the admin-only upload stream for images inside the "admin_uploads" collection.
     */
    fun uploadToAdminStream(wallpaperId: Int, title: String, imageUrl: String, adminId: String) {
        val fs = firestore ?: return
        val data = hashMapOf(
            "id" to wallpaperId,
            "title" to title,
            "imageUrl" to imageUrl,
            "adminId" to adminId,
            "timestamp" to Timestamp.now()
        )

        fs.collection("admin_uploads").document(wallpaperId.toString())
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully added upload to admin_uploads collection stream")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync to admin_uploads stream", e)
            }
    }

    /**
     * Listens to the Firestore "categories" collection and populates our customCategories StateFlow.
     */
    fun startSyncingCategories() {
        val fs = firestore ?: return
        fs.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to categories in Firestore", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val categoryList = snapshot.documents.mapNotNull { doc ->
                        doc.getString("name")
                    }.distinct()
                    _customCategories.value = categoryList
                    Log.d(TAG, "Synced categories from Firestore: $categoryList")
                }
            }
    }

    /**
     * Uploads/Registers a custom category inside the Firestore "categories" collection.
     */
    fun uploadCategory(categoryName: String) {
        val fs = firestore ?: return
        val docId = categoryName.trim().lowercase()
        val data = hashMapOf(
            "name" to categoryName.trim(),
            "timestamp" to Timestamp.now()
        )
        fs.collection("categories").document(docId)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Category '$categoryName' successfully uploaded to Firestore!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed uploading category to Firestore", e)
            }
    }

    /**
     * Removes a custom category from the Firestore "categories" collection.
     */
    fun deleteCategoryFromFirestore(categoryName: String) {
        val fs = firestore ?: return
        val docId = categoryName.trim().lowercase()
        fs.collection("categories").document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Category '$categoryName' successfully deleted from Firestore!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to delete category from Firestore", e)
            }
    }
}
