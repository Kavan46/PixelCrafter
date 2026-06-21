package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    val categoriesList by viewModel.categoriesState.collectAsStateWithLifecycle()
    val availableCategories = remember(categoriesList) { categoriesList.filter { it != "All" } }

    var title by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var isNewCategoryMode by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(availableCategories) {
        if (selectedCategory.isBlank() && availableCategories.isNotEmpty()) {
            selectedCategory = availableCategories.first()
        }
    }

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
                imageUrl = "Local Gallery Selection"
                isProcessingImage = false
                Toast.makeText(context, "Aesthetic artwork loaded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Reset status on exit or creation
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAddWallpaperStatus()
        }
    }

    // Capture success response to reset fields
    LaunchedEffect(addStatus) {
        if (addStatus?.startsWith("Success") == true) {
            title = ""
            imageUrl = ""
            localImagePreviewUri = null
            firebaseSyncBase64 = null
            newCategoryName = ""
            isNewCategoryMode = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = { Text("Craft Wallpaper", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_add_back")) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBlack,
                    titleContentColor = Color.White
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
                    .background(SlateNavy.copy(alpha = 0.5f))
                    .border(1.dp, DeepBlueBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Aesthetic Compiler",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Fill in the parameters below to catalog and compile high-resolution artwork vectors directly into the user-facing database.",
                            color = MutedSlateText,
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

            // Field Form 1: Wallpaper Title (Image Name)
            AdminFormField(
                value = title,
                onValueChange = { title = it },
                label = "Image Name",
                placeholder = "e.g. Neon Dreams Void",
                icon = Icons.Default.TextFields,
                tag = "admin_input_title"
            )

            // Category Selection Mode Selector
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Aesthetic Category Mode",
                    color = IceBlueText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isNewCategoryMode) PremiumElectricBlue.copy(alpha = 0.2f) else DeepMidnightBlue)
                            .border(
                                width = 1.dp,
                                color = if (!isNewCategoryMode) NeonCyan else DeepBlueBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { isNewCategoryMode = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select Category",
                            color = if (!isNewCategoryMode) Color.White else MutedSlateText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNewCategoryMode) PremiumElectricBlue.copy(alpha = 0.2f) else DeepMidnightBlue)
                            .border(
                                width = 1.dp,
                                color = if (isNewCategoryMode) NeonCyan else DeepBlueBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { isNewCategoryMode = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Create Custom",
                            color = if (isNewCategoryMode) Color.White else MutedSlateText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Category input based on mode
            if (!isNewCategoryMode) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Aesthetic Category",
                        color = IceBlueText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth().testTag("admin_input_category_box")
                    ) {
                        TextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(Icons.Default.Category, contentDescription = null, tint = NeonCyan)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = DeepMidnightBlue,
                                unfocusedContainerColor = DeepMidnightBlue,
                                focusedTextColor = IceBlueText,
                                unfocusedTextColor = IceBlueText,
                                focusedIndicatorColor = PremiumElectricBlue,
                                unfocusedIndicatorColor = DeepBlueBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .border(1.dp, DeepBlueBorder, RoundedCornerShape(12.dp))
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.background(DeepMidnightBlue)
                        ) {
                            availableCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, color = IceBlueText) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("category_option_$category")
                                )
                            }
                        }
                    }
                }
            } else {
                AdminFormField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = "New Category Name",
                    placeholder = "e.g. Synthwave Dreams",
                    icon = Icons.Default.Category,
                    tag = "admin_input_new_category"
                )
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
                                        imageUrl = ""
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
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter an Image Name.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val finalCategory = if (isNewCategoryMode) {
                        val trimmedCategory = newCategoryName.trim()
                        if (trimmedCategory.isBlank()) {
                            Toast.makeText(context, "Please enter a custom category name.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addCategory(trimmedCategory)
                        trimmedCategory
                    } else {
                        if (selectedCategory.isBlank()) {
                            Toast.makeText(context, "Please select an existing category.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        selectedCategory
                    }

                    if (localImagePreviewUri == null) {
                        Toast.makeText(context, "Please select an image file to upload.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.addWallpaper(
                        title = title,
                        category = finalCategory,
                        url = localImagePreviewUri!!,
                        author = "PixelCrafter Admin",
                        firebaseSyncUrl = firebaseSyncBase64
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumElectricBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, NeonCyan, RoundedCornerShape(14.dp))
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
