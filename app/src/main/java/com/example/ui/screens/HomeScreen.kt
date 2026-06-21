package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Wallpaper
import com.example.ui.components.CategorySelector
import com.example.ui.components.PixelCrafterHeader
import com.example.ui.components.PixelCrafterSearchBar
import com.example.ui.components.rememberImageModel
import com.example.ui.components.rememberCardImageModel
import com.example.ui.theme.*
import com.example.viewmodel.WallpaperViewModel

@Composable
fun HomeScreen(
    viewModel: WallpaperViewModel,
    onWallpaperSelected: (Wallpaper) -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wallpapers by viewModel.wallpapers.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf<Wallpaper?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MidnightBlack,
        floatingActionButton = {
            if (isAdminMode) {
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = PremiumElectricBlue,
                    contentColor = Color.White,
                    modifier = Modifier
                        .testTag("admin_add_fab")
                        .border(1.dp, NeonCyan, FloatingActionButtonDefaults.shape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Craft Wallpaper"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Title with Neon Accented Subtitle
            PixelCrafterHeader(
                title = "PixelCrafter",
                subtitle = if (isAdminMode) "Administrative Crafting Deck activated" else "High Definition Wallpaper Studio"
            )

            // Live Search Field
            PixelCrafterSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                placeholder = "Search wallpapers, creators..."
            )

            // Category scrolling tabs - Only visible and selectable by Administrators
            if (isAdminMode) {
                CategorySelector(
                    categories = viewModel.categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )
            }

            if (wallpapers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Wallpapers Found",
                            fontSize = 18.sp,
                            color = IceBlueText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing search or upload new ones as Admin",
                            fontSize = 14.sp,
                            color = MutedSlateText
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(wallpapers, key = { it.id }) { wallpaper ->
                        WallpaperCard(
                            wallpaper = wallpaper,
                            isAdmin = isAdminMode,
                            onClick = { onWallpaperSelected(wallpaper) },
                            onLongClick = {
                                if (isAdminMode) {
                                    showDeleteConfirmDialog = wallpaper
                                }
                            },
                            onFavoriteClick = { viewModel.toggleFavorite(wallpaper) }
                        )
                    }
                }
            }
        }

        // Admin-triggered removal confirmation
        showDeleteConfirmDialog?.let { wallpaper ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                title = { Text("Purge Masterpiece?", color = Color.White) },
                text = {
                    Text(
                        "Are you sure you want to permanently delete '${wallpaper.title}' from the gallery database?",
                        color = IceBlueText
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteWallpaper(wallpaper)
                            showDeleteConfirmDialog = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("PURGE")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text("CANCEL", color = IceBlueText)
                    }
                },
                containerColor = DeepMidnightBlue,
                textContentColor = IceBlueText,
                modifier = Modifier.border(1.dp, DeepBlueBorder, RoundedCornerShape(28.dp))
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperCard(
    wallpaper: Wallpaper,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DeepBlueBorder, RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("wallpaper_card_${wallpaper.id}")
    ) {
        // Optimized visual asset loading using Coil with safe downscaling
        AsyncImage(
            model = rememberCardImageModel(wallpaper.imageUrl),
            contentDescription = "Wallpaper title: ${wallpaper.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for layout legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            TranslucentBlack
                        )
                    )
                )
        )

        // Metadata and actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = wallpaper.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "by ${wallpaper.author}",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("favorite_toggle_${wallpaper.id}")
                ) {
                    Icon(
                        imageVector = if (wallpaper.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite status",
                        tint = if (wallpaper.isFavorite) Color.Red else IceBlueText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Custom marker badging
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isAdmin) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepMidnightBlue.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = wallpaper.category,
                        color = IceBlueText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
