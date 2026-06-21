package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.model.Wallpaper
import com.example.data.repository.WallpaperRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseDatabaseService(
    private val context: Context,
    private val repository: WallpaperRepository,
    private val scope: CoroutineScope
) {
    private val TAG = "FirebaseDatabaseService"

    val isFirebaseInitialized: Boolean by lazy {
        try {
            val apps = FirebaseApp.getApps(context)
            apps.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private val database: FirebaseDatabase? by lazy {
        if (isFirebaseInitialized) {
            try {
                FirebaseDatabase.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FirebaseDatabase instance", e)
                null
            }
        } else {
            null
        }
    }

    init {
        startSyncing()
    }

    fun startSyncing() {
        val db = database ?: return
        val wallpapersRef = db.getReference("wallpapers")

        wallpapersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(Dispatchers.IO) {
                    try {
                        for (child in snapshot.children) {
                            val idVal = child.child("id").getValue()
                            val id = when (idVal) {
                                is Number -> idVal.toInt()
                                is String -> idVal.toIntOrNull() ?: 0
                                else -> 0
                            }
                            val title = child.child("title").getValue(String::class.java) ?: ""
                            val category = child.child("category").getValue(String::class.java) ?: ""
                            val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                            val author = child.child("author").getValue(String::class.java) ?: "Firebase Creator"
                            
                            val downloadsVal = child.child("downloads").getValue()
                            val downloads = when (downloadsVal) {
                                is Number -> downloadsVal.toInt()
                                is String -> downloadsVal.toIntOrNull() ?: 0
                                else -> 0
                            }
                            val isCustom = child.child("isCustom").getValue(Boolean::class.java) ?: true

                            if (imageUrl.isNotBlank() && title.isNotBlank() && id > 0) {
                                // Check if already exists in local database
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
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing Firebase wallpapers to local database", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Firebase wallpapers listener cancelled: ${error.message}")
            }
        })
    }

    fun uploadWallpaper(wallpaper: Wallpaper) {
        val db = database ?: return
        val wallpapersRef = db.getReference("wallpapers")
        
        // Generate ID if zero
        val targetId = if (wallpaper.id == 0) (System.currentTimeMillis().toInt() and 0xfffffff) else wallpaper.id
        val finalWallpaper = wallpaper.copy(id = targetId)

        val data = mapOf(
            "id" to finalWallpaper.id,
            "title" to finalWallpaper.title,
            "category" to finalWallpaper.category,
            "imageUrl" to finalWallpaper.imageUrl,
            "author" to finalWallpaper.author,
            "downloads" to finalWallpaper.downloads,
            "isCustom" to finalWallpaper.isCustom
        )

        wallpapersRef.child(targetId.toString()).setValue(data)
            .addOnSuccessListener {
                Log.d(TAG, "Wallpaper successfully synced to Firebase Realtime Database!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed syncing to Firebase database", e)
            }
    }

    fun deleteWallpaperFromFirebase(wallpaperId: Int) {
        val db = database ?: return
        val wallpapersRef = db.getReference("wallpapers")
        wallpapersRef.child(wallpaperId.toString()).removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Wallpaper '$wallpaperId' successfully removed from Firebase Realtime Database!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove wallpaper '$wallpaperId' from Firebase Database", e)
            }
    }
}
