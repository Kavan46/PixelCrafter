package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PixelCrafterHeader
import com.example.ui.theme.*
import com.example.viewmodel.WallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: WallpaperViewModel,
    onBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val categories by viewModel.categoriesState.collectAsStateWithLifecycle()
    val wallpapers by viewModel.wallpapers.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    var newCategoryText by remember { mutableStateOf("") }
    var workflowStepSelected by remember { mutableStateOf(1) }

    // Guard Screen for Non-Admin
    if (!isAdminMode) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MidnightBlack)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.widthIn(max = 400.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B1F24)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Restricted",
                        tint = Color.Red,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Text(
                    text = "ACCESS RESTRICTED",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Text(
                    text = "The Admin Dashboard is only accessible to authorized developers. Please authentication with an administrator account to continue.",
                    color = MutedSlateText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateNavy),
                    modifier = Modifier.fillMaxWidth().testTag("unauthorized_admin_back_btn")
                ) {
                    Text("Go Back", color = Color.White)
                }
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard Hub", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_dashboard_back")) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live stats panel card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepMidnightBlue)
                        .border(1.dp, DeepBlueBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Wallpapers", color = MutedSlateText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${wallpapers.size}", color = NeonCyan, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(DeepBlueBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Categories", color = MutedSlateText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${categories.size}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(DeepBlueBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Favorites", color = MutedSlateText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${favorites.size}", color = Color(0xFFF43F5E), fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Quick navigation row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToAdd,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_quick_add_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PremiumElectricBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Craft Screen", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECTION 1: SECURE CATEGORIES MANAGEMENT
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DeepMidnightBlue)
                        .border(1.dp, DeepBlueBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Categories Manager",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Secure Category Deck",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // TextField + Add Button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextField(
                            value = newCategoryText,
                            onValueChange = { newCategoryText = it },
                            placeholder = { Text("E.g. Synthwave", color = MutedSlateText, fontSize = 13.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MidnightBlack,
                                unfocusedContainerColor = MidnightBlack,
                                focusedTextColor = IceBlueText,
                                unfocusedTextColor = IceBlueText,
                                focusedIndicatorColor = PremiumElectricBlue,
                                unfocusedIndicatorColor = DeepBlueBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, DeepBlueBorder, RoundedCornerShape(10.dp))
                                .testTag("add_category_input")
                        )

                        Button(
                            onClick = {
                                if (viewModel.addCategory(newCategoryText)) {
                                    Toast.makeText(context, "Added category '$newCategoryText'", Toast.LENGTH_SHORT).show()
                                    newCategoryText = ""
                                } else {
                                    Toast.makeText(context, "Failed/Duplicate category tag", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PremiumElectricBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_category_button")
                        ) {
                            Text("ADD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Categories List
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateNavy.copy(alpha = 0.3f))
                                .border(1.dp, DeepBlueBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(cat, color = IceBlueText, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                            if (cat.equals("All", ignoreCase = true)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DeepBlueBorder)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("DEFAULT", color = MutedSlateText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        if (viewModel.removeCategory(cat)) {
                                            Toast.makeText(context, "Purged category '$cat'", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Protected category", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(24.dp).testTag("delete_category_$cat")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: COMPRESSION & UPLOAD WORKFLOW VISUALIZER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DeepMidnightBlue)
                        .border(1.dp, DeepBlueBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Workflow Diagram",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Aesthetic Upload Workflow",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Interactive system representation of our compression and dual-sync architecture.",
                        color = MutedSlateText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step Selector Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 3, 4).forEach { stepNum ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (workflowStepSelected == stepNum) ActivePillBlue else SlateNavy.copy(alpha = 0.5f))
                                    .border(1.dp, if (workflowStepSelected == stepNum) NeonCyan else DeepBlueBorder, RoundedCornerShape(8.dp))
                                    .clickable { workflowStepSelected = stepNum }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Step $stepNum",
                                    color = if (workflowStepSelected == stepNum) Color.White else MutedSlateText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Workflow graphic card rendering based on selection
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MidnightBlack)
                            .border(1.dp, DeepBlueBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            when (workflowStepSelected) {
                                1 -> {
                                    WorkflowDetailStep(
                                        title = "1. LOCAL MEDIA GRAB",
                                        desc = "Retrieves file paths & content streams locally with absolute Android device storage permissions securely.",
                                        detail = "Input Stream → Copied to app persistent internal workspace directory (/custom_wallpapers). Saved as a local physical JPEG snapshot.",
                                        icon = Icons.Default.FolderOpen,
                                        tintColor = NeonCyan
                                    )
                                }
                                2 -> {
                                    WorkflowDetailStep(
                                        title = "2. INTELLIGENT COMPRESSION",
                                        desc = "Scales rendering resolution below 320px constraints using standard Android BitmapFactory downsampling logic.",
                                        detail = "Avoids Realtime Database buffer overflows. Preserves 100% original aspect ratio using iterative sample powers-of-two.",
                                        icon = Icons.Default.SettingsSuggest,
                                        tintColor = Color(0xFFA855F7)
                                    )
                                }
                                3 -> {
                                    WorkflowDetailStep(
                                        title = "3. BASE64 TRANSFORM",
                                        desc = "Encodes compressed ByteStream into strict high-contrast data:image/jpeg;base64 representation.",
                                        detail = "Compressed to 75% quality JPEG byte array first, then passed to Android Base64 encoder using NO_WRAP configurations.",
                                        icon = Icons.Default.Settings,
                                        tintColor = Color(0xFFEAB308)
                                    )
                                }
                                4 -> {
                                    WorkflowDetailStep(
                                        title = "4. PERSISTENT HYBRID SYNC",
                                        desc = "Publishes generated ID details directly into standard Room SQLite Database while publishing base64 vectors.",
                                        detail = "Local database reads load original uncompressed high-resolution files instantly. Remote connections sync fallback base64 streams live.",
                                        icon = Icons.Default.CloudUpload,
                                        tintColor = Color(0xFF22C55E)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full workflow map connector line visualization
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WorkflowBullet(1, workflowStepSelected == 1)
                        Spacer(modifier = Modifier.weight(1f).height(2.dp).background(if (workflowStepSelected > 1) NeonCyan else DeepBlueBorder))
                        WorkflowBullet(2, workflowStepSelected == 2)
                        Spacer(modifier = Modifier.weight(1f).height(2.dp).background(if (workflowStepSelected > 2) NeonCyan else DeepBlueBorder))
                        WorkflowBullet(3, workflowStepSelected == 3)
                        Spacer(modifier = Modifier.weight(1f).height(2.dp).background(if (workflowStepSelected > 3) NeonCyan else DeepBlueBorder))
                        WorkflowBullet(4, workflowStepSelected == 4)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun WorkflowDetailStep(
    title: String,
    desc: String,
    detail: String,
    icon: ImageVector,
    tintColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tintColor.copy(alpha = 0.15f))
                .border(1.dp, tintColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(22.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = tintColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Text(desc, color = IceBlueText, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SlateNavy.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "SPEC DETAILS: $detail",
                    color = MutedSlateText,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WorkflowBullet(num: Int, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isActive) ActivePillBlue else SlateNavy)
            .border(1.dp, if (isActive) NeonCyan else DeepBlueBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$num",
            color = if (isActive) Color.White else MutedSlateText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
