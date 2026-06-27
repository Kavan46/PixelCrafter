package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.ui.components.PixelCrafterHeader
import com.example.ui.components.rememberImageModel
import com.example.ui.theme.*
import com.example.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddScreen(
    viewModel: WallpaperViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addStatus by viewModel.addWallpaperStatus.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    var localImagePreviewUri by remember { mutableStateOf<String?>(null) }
    var firebaseSyncBase64 by remember { mutableStateOf<String?>(null) }
    var isProcessingImage by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isProcessingImage = true
            viewModel.processSelectedImage(uri) { localPath, base64Str ->
                localImagePreviewUri = localPath
                firebaseSyncBase64 = base64Str
                isProcessingImage = false
                Toast.makeText(context, "Aesthetic artwork loaded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var categoryInput by remember { mutableStateOf("") }

    // Reset status on exit or creation
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAddWallpaperStatus()
        }
    }

    // Capture success response to reset fields
    LaunchedEffect(addStatus) {
        if (addStatus?.startsWith("Success") == true) {
            localImagePreviewUri = null
            firebaseSyncBase64 = null
            categoryInput = ""
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Craft Wallpaper", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_add_back")) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen explanation banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Direct Image Upload Deck",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Upload high-resolution images directly to the main user feed. No categories or titles are required — the system publishes your artwork instantly.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Input Validation Status Feedback Alerts
            addStatus?.let { status ->
                val isSuccess = status.startsWith("Success")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSuccess) Color(0xFF0F291B) else Color(0xFF33141E))
                        .border(1.dp, if (isSuccess) Color(0xFF22C55E) else Color(0xFFEF4444), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .testTag("add_status_alert")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = if (isSuccess) "Success badge" else "Error badge",
                            tint = if (isSuccess) Color(0xFF22C55E) else Color(0xFFEF4444)
                        )
                        Text(
                            text = status,
                            color = if (isSuccess) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }



            // Field Form 2.0: Aesthetic Category Selector
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Aesthetic Category",
                    color = IceBlueText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                TextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    placeholder = { Text("e.g. Cars, Heroes, Anime, Nature...", color = MutedSlateText) },
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null, tint = NeonCyan)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DeepMidnightBlue,
                        unfocusedContainerColor = DeepMidnightBlue,
                        focusedTextColor = IceBlueText,
                        unfocusedTextColor = IceBlueText,
                        focusedIndicatorColor = PremiumElectricBlue,
                        unfocusedIndicatorColor = DeepBlueBorder,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DeepBlueBorder, RoundedCornerShape(12.dp))
                        .testTag("admin_input_category")
                )

                // Quick Suggestion Chips
                val suggestions = listOf("Cars", "Heroes", "Abstract", "Nature", "Space", "Anime", "Minimalist")
                val chipsScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(chipsScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { tag ->
                        val isSelected = categoryInput.equals(tag, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ActivePillBlue else SlateNavy.copy(alpha = 0.5f))
                                .border(1.dp, if (isSelected) NeonCyan else DeepBlueBorder, RoundedCornerShape(8.dp))
                                .clickable { categoryInput = tag }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("admin_suggestion_chip_$tag")
                        ) {
                            Text(
                                text = tag,
                                color = if (isSelected) Color.White else MutedSlateText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Field Form 2.5: Local Image Picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Aesthetic Media Artwork",
                    color = IceBlueText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isProcessingImage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepMidnightBlue)
                            .border(1.dp, DeepBlueBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(36.dp))
                            Text(
                                text = "Adapting Artwork Vector...",
                                color = IceBlueText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (localImagePreviewUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, NeonCyan, RoundedCornerShape(16.dp))
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = rememberImageModel(localImagePreviewUri!!),
                            contentDescription = "Selected Local Image Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Info Overlay & Reset action
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ready to Compile",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Resized & cached successfully",
                                    color = NeonCyan,
                                    fontSize = 10.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SlateNavy,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        localImagePreviewUri = null
                                        firebaseSyncBase64 = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4A1A23),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DeepMidnightBlue)
                            .border(
                                width = 1.dp,
                                color = DeepBlueBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { galleryLauncher.launch("image/*") }
                            .padding(16.dp)
                            .testTag("select_from_device_dash"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Select Artwork",
                                tint = NeonCyan,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "TAP TO SELECT LOCAL IMAGE FROM DEVICE",
                                color = IceBlueText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Compiles direct photos simple & offline-ready",
                                color = MutedSlateText,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CRAFT MASTERPIECE Submit Trigger
            Button(
                onClick = {
                    if (localImagePreviewUri == null) {
                        Toast.makeText(context, "Please select an image file to upload.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val finalCategory = categoryInput.trim().ifBlank { "All" }
                    val randomNum = (100..999).random()
                    val generatedTitle = "$finalCategory Masterpiece #$randomNum"

                    viewModel.addWallpaper(
                        title = generatedTitle,
                        category = finalCategory,
                        url = localImagePreviewUri!!,
                        author = "PixelCrafter Admin",
                        firebaseSyncUrl = firebaseSyncBase64
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(14.dp))
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .testTag("admin_submit_button")
            ) {
                Text(
                    text = "COMPILE WORKSPACE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun AdminFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    tag: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = IceBlueText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MutedSlateText) },
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = NeonCyan)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DeepMidnightBlue,
                unfocusedContainerColor = DeepMidnightBlue,
                focusedTextColor = IceBlueText,
                unfocusedTextColor = IceBlueText,
                focusedIndicatorColor = PremiumElectricBlue,
                unfocusedIndicatorColor = DeepBlueBorder,
                cursorColor = NeonCyan
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DeepBlueBorder, RoundedCornerShape(12.dp))
                .testTag(tag)
        )
    }
}
