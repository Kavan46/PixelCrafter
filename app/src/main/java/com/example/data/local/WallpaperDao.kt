package com.example.data.local

import androidx.room.*
import com.example.data.model.Wallpaper
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers ORDER BY id DESC")
    fun getAllWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE category = :category ORDER BY id DESC")
    fun getWallpapersByCategory(category: String): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    suspend fun getWallpaperById(id: Int): Wallpaper?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: Wallpaper): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallpapers(wallpapers: List<Wallpaper>)

    @Update
    suspend fun updateWallpaper(wallpaper: Wallpaper)

    @Delete
    suspend fun deleteWallpaper(wallpaper: Wallpaper)

    @Query("UPDATE wallpapers SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFav: Boolean)

    @Query("UPDATE wallpapers SET downloads = downloads + 1 WHERE id = :id")
    suspend fun incrementDownloadCount(id: Int)
}
