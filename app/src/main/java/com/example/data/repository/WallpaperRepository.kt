package com.example.data.repository

import com.example.data.local.WallpaperDao
import com.example.data.model.Wallpaper
import kotlinx.coroutines.flow.Flow

class WallpaperRepository(private val wallpaperDao: WallpaperDao) {

    val allWallpapers: Flow<List<Wallpaper>> = wallpaperDao.getAllWallpapers()
    
    val favoriteWallpapers: Flow<List<Wallpaper>> = wallpaperDao.getFavoriteWallpapers()

    fun getWallpapersByCategory(category: String): Flow<List<Wallpaper>> {
        return wallpaperDao.getWallpapersByCategory(category)
    }

    suspend fun getWallpaperById(id: Int): Wallpaper? {
        return wallpaperDao.getWallpaperById(id)
    }

    suspend fun insertWallpaper(wallpaper: Wallpaper): Long {
        return wallpaperDao.insertWallpaper(wallpaper)
    }

    suspend fun updateWallpaper(wallpaper: Wallpaper) {
        wallpaperDao.updateWallpaper(wallpaper)
    }

    suspend fun deleteWallpaper(wallpaper: Wallpaper) {
        wallpaperDao.deleteWallpaper(wallpaper)
    }

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        wallpaperDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun incrementDownloads(id: Int) {
        wallpaperDao.incrementDownloadCount(id)
    }
}
