package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Wallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Wallpaper::class], version = 1, exportSchema = false)
abstract class WallpaperDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao

    companion object {
        @Volatile
        private var INSTANCE: WallpaperDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WallpaperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WallpaperDatabase::class.java,
                    "pixelcrafter_db"
                )
                .addCallback(WallpaperDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class WallpaperDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                INSTANCE?.let { database ->
                    populateDatabase(database.wallpaperDao())
                }
            }
        }

        suspend fun populateDatabase(dao: WallpaperDao) {
            val defaults = listOf(
                Wallpaper(
                    title = "Cyberpunk Neo Metropolis",
                    category = "Cyberpunk Neon",
                    imageUrl = "https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?q=80&w=1200",
                    author = "Luna Dusk",
                    downloads = 3450
                ),
                Wallpaper(
                    title = "Mystic Forest Glade",
                    category = "Mystic Nature",
                    imageUrl = "https://images.unsplash.com/photo-1511497584788-876760111969?q=80&w=1200",
                    author = "Echo Pine",
                    downloads = 1240
                ),
                Wallpaper(
                    title = "Dark Liquid Gold",
                    category = "Dark Minimalist",
                    imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1200",
                    author = "Vector Onyx",
                    downloads = 2890
                ),
                Wallpaper(
                    title = "Cosmic Void Dust",
                    category = "Cosmic Space",
                    imageUrl = "https://images.unsplash.com/photo-1464802686167-b939a6910659?q=80&w=1200",
                    author = "Astro Orion",
                    downloads = 4820
                ),
                Wallpaper(
                    title = "Aurora Borealis Night",
                    category = "Mystic Nature",
                    imageUrl = "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?q=80&w=1200",
                    author = "Sol Polar",
                    downloads = 5120
                ),
                Wallpaper(
                    title = "Futuristic Portal Room",
                    category = "Futuristic City",
                    imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1200",
                    author = "Nova Wave",
                    downloads = 1910
                ),
                Wallpaper(
                    title = "Midnight Mountain Peak",
                    category = "Dark Minimalist",
                    imageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=1200",
                    author = "Aura Peak",
                    downloads = 3100
                ),
                Wallpaper(
                    title = "Deep Ocean Abyss",
                    category = "Mystic Nature",
                    imageUrl = "https://images.unsplash.com/photo-1518837695005-2083093ee35b?q=80&w=1200",
                    author = "Deep Aqua",
                    downloads = 890
                ),
                Wallpaper(
                    title = "Sci-Fi Command Deck",
                    category = "Cosmic Space",
                    imageUrl = "https://images.unsplash.com/photo-1543722530-d2c3201371e7?q=80&w=1200",
                    author = "Apex Tech",
                    downloads = 2540
                ),
                Wallpaper(
                    title = "Infinite Neon Grid",
                    category = "Cyberpunk Neon",
                    imageUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=1200",
                    author = "Retro Synth",
                    downloads = 4420
                ),
                Wallpaper(
                    title = "Minimal Sphere Abstract",
                    category = "Dark Minimalist",
                    imageUrl = "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?q=80&w=1200",
                    author = "Gleam Studio",
                    downloads = 3670
                ),
                Wallpaper(
                    title = "Vibrant Galaxy Dust",
                    category = "Cosmic Space",
                    imageUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=1200",
                    author = "Astro Space",
                    downloads = 6120
                )
            )
            dao.insertWallpapers(defaults)
        }
    }
}
