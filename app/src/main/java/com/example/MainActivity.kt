package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.WallpaperViewModel
import com.google.android.gms.ads.MobileAds
import com.example.data.AdMobManager

class MainActivity : ComponentActivity() {
    override fun getAttributionTag(): String? {
        // Return a declared attribution tag to satisfy AppOps context auditing on Android 12+
        return "attributionTag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AdMob / Google Mobile Ads SDK 
        try {
            MobileAds.initialize(this.applicationContext) {}
            AdMobManager.loadRewardedAd(this.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        enableEdgeToEdge()
        setContent {
            val viewModel: WallpaperViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val selectedThemeName by viewModel.selectedTheme.collectAsState()

            PixelCrafterTheme(themeName = selectedThemeName) {
                PixelCrafterApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PixelCrafterApp(viewModel: WallpaperViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val navController = rememberNavController()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide navigation bar when viewing fullscreen wallpapers or uploading as administrator
    val isBottomBarVisible = when (currentRoute) {
        "home", "favorite", "settings" -> true
        else -> false
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack),
        containerColor = MidnightBlack,
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = DeepMidnightBlue,
                    contentColor = IceBlueText,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .border(
                            width = 1.dp,
                            color = DeepBlueBorder,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .testTag("app_bottom_nav_bar")
                ) {
                    // Home tab item
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "home") Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home screen feed icon"
                            )
                        },
                        label = { Text("Discovery", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = MutedSlateText,
                            unselectedTextColor = MutedSlateText,
                            indicatorColor = ActivePillBlue
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    // Favorites tab item
                    NavigationBarItem(
                        selected = currentRoute == "favorite",
                        onClick = {
                            if (currentRoute != "favorite") {
                                navController.navigate("favorite") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "favorite") Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite wallpapers icon"
                            )
                        },
                        label = { Text("Collector", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = MutedSlateText,
                            unselectedTextColor = MutedSlateText,
                            indicatorColor = ActivePillBlue
                        ),
                        modifier = Modifier.testTag("nav_item_favorite")
                    )

                    // Settings Tab
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = {
                            if (currentRoute != "settings") {
                                navController.navigate("settings") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "settings") Icons.Default.Settings else Icons.Outlined.Settings,
                                contentDescription = "System configuration settings icon"
                            )
                        },
                        label = { Text("Studio Hub", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = MutedSlateText,
                            unselectedTextColor = MutedSlateText,
                            indicatorColor = ActivePillBlue
                        ),
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize(),
            builder = {
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onWallpaperSelected = { wallpaper ->
                            navController.navigate("detail/${wallpaper.id}")
                        },
                        onNavigateToAdd = {
                            navController.navigate("admin_add")
                        },
                        modifier = Modifier.padding(bottom = if (isBottomBarVisible) 60.dp else 0.dp)
                    )
                }

                composable("favorite") {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onWallpaperSelected = { wallpaper ->
                            navController.navigate("detail/${wallpaper.id}")
                        },
                        modifier = Modifier.padding(bottom = if (isBottomBarVisible) 60.dp else 0.dp)
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAdminDashboard = { navController.navigate("admin_dashboard") },
                        modifier = Modifier.padding(bottom = if (isBottomBarVisible) 60.dp else 0.dp)
                    )
                }

                composable(
                    route = "detail/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val wallpaperId = backStackEntry.arguments?.getInt("id") ?: 0
                    DetailScreen(
                        viewModel = viewModel,
                        wallpaperId = wallpaperId,
                        onBack = { navController.navigateUp() }
                    )
                }

                composable("admin_add") {
                    AdminAddScreen(
                        viewModel = viewModel,
                        onBack = { navController.navigateUp() }
                    )
                }

                composable("admin_dashboard") {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onBack = { navController.navigateUp() },
                        onNavigateToAdd = { navController.navigate("admin_add") }
                    )
                }
            }
        )
    }
}
