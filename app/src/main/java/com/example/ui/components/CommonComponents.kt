package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PixelCrafterHeader(
    title: String,
    subtitle: String,
    actionButton: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MidnightBlack, DeepMidnightBlue)
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = NeonCyan,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (actionButton != null) {
            actionButton()
        }
    }
}

@Composable
fun PixelCrafterSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Explore high-res artwork...",
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                color = MutedSlateText,
                fontSize = 15.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                tint = NeonCyan
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Search",
                        tint = MutedSlateText
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SlateNavy,
            unfocusedContainerColor = DeepMidnightBlue,
            disabledContainerColor = DeepMidnightBlue,
            focusedTextColor = IceBlueText,
            unfocusedTextColor = IceBlueText,
            cursorColor = NeonCyan,
            focusedIndicatorColor = PremiumElectricBlue,
            unfocusedIndicatorColor = DeepBlueBorder
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .border(1.dp, DeepBlueBorder, RoundedCornerShape(16.dp))
            .testTag("search_bar_input")
    )
}

@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val backgroundGradient = if (isSelected) {
                Brush.horizontalGradient(listOf(PremiumElectricBlue, ActivePillBlue))
            } else {
                Brush.horizontalGradient(listOf(DeepMidnightBlue, DeepMidnightBlue))
            }
            val borderHex = if (isSelected) NeonCyan else DeepBlueBorder

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush = backgroundGradient)
                    .border(1.dp, borderHex, RoundedCornerShape(12.dp))
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .testTag("category_tab_$category")
            ) {
                Text(
                    text = category,
                    color = if (isSelected) Color.White else IceBlueText,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun rememberImageModel(url: String): Any {
    return androidx.compose.runtime.remember(url) {
        if (url.startsWith("data:image/jpeg;base64,") || url.startsWith("data:image/png;base64,")) {
            val base64Data = url.substringAfter("base64,")
            try {
                android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                url
            }
        } else if (url.startsWith("file://") || url.startsWith("content://") || url.startsWith("http://") || url.startsWith("https://")) {
            try {
                android.net.Uri.parse(url)
            } catch (e: Exception) {
                url
            }
        } else {
            url
        }
    }
}

@Composable
fun rememberCardImageModel(url: String, width: Int = 360, height: Int = 600): Any {
    val context = androidx.compose.ui.platform.LocalContext.current
    val parsedData = rememberImageModel(url)
    return androidx.compose.runtime.remember(url, parsedData) {
        coil.request.ImageRequest.Builder(context)
            .data(parsedData)
            .size(width, height)
            .crossfade(true)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
    }
}

@Composable
fun rememberDetailImageModel(url: String): Any {
    val context = androidx.compose.ui.platform.LocalContext.current
    val parsedData = rememberImageModel(url)
    return androidx.compose.runtime.remember(url, parsedData) {
        coil.request.ImageRequest.Builder(context)
            .data(parsedData)
            .crossfade(true)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
    }
}

