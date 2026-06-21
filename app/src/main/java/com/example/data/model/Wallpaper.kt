package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "wallpapers")
data class Wallpaper(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val imageUrl: String,
    val author: String = "PixelCrafter AI",
    val downloads: Int = 0,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
) : Serializable
