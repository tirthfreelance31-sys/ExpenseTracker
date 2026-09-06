package com.expense.tracker.ui.screens.categories

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<CategoryEntity>,
    onAddCategory: (String, String) -> Unit = { _, _ -> },
    onUpdateCategory: (CategoryEntity) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var newCategoryName by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("Category") }

    val availableIcons = listOf(
        "Restaurant" to Icons.Default.Restaurant,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "ReceiptLong" to Icons.AutoMirrored.Filled.ReceiptLong,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "Movie" to Icons.Default.Movie,
        "MedicalServices" to Icons.Default.MedicalServices,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "School" to Icons.Default.School,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "Flight" to Icons.Default.Flight,
        "Pets" to Icons.Default.Pets,
        "Star" to Icons.Default.Star
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Subtle ambient glow at top
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (colors.isDark) Color(0xFF163E3A).copy(alpha = 0.35f) else Color(0xFF8AF8BE).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.25f),
                    radius = size.width * 0.65f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            ),
                            color = colors.textPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            newCategoryName = ""
                            selectedIconName = "Category"
                            showAddDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Category",
                                tint = colors.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Information Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Spending Categories",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Standard categories to organize and track your outgoing expenses. Tap + at the top to add a custom category.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = Inter,
                                    fontSize = 13.sp
                                ),
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "ALL CATEGORIES (${categories.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.textSecondary
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column {
                            categories.forEachIndexed { index, category ->
                                val (icon, tint, bg) = resolveCategoryMeta(category.name, category.iconRes, colors)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!category.isDefault) {
                                                editingCategory = category
                                                newCategoryName = category.name
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = bg,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = category.name,
                                                    tint = tint,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = category.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = Inter,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp
                                                ),
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (!category.isDefault) {
                                                Text(
                                                    text = "Custom Category",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = Inter,
                                                        fontSize = 11.sp
                                                    ),
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }

                                    if (category.isDefault) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = colors.primary.copy(alpha = 0.12f),
                                            border = BorderStroke(0.6.dp, colors.primary.copy(alpha = 0.25f))
                                        ) {
                                            Text(
                                                text = "Default",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = Inter,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp
                                                ),
                                                color = colors.primary
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Category",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (index < categories.size - 1) {
                                    HorizontalDivider(
                                        color = colors.border.copy(alpha = 0.5f),
                                        thickness = 0.8.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // Add Category Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = colors.surface,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text(
                        "Add Custom Category",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = colors.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Category Name", style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                cursorColor = colors.primary
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "Choose Icon",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = colors.textSecondary
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableIcons) { (name, icon) ->
                                val isSelected = selectedIconName == name
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) colors.primary.copy(alpha = 0.20f) else colors.surfaceSecondary,
                                    border = if (isSelected) BorderStroke(1.5.dp, colors.primary) else null,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable { selectedIconName = name }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = name,
                                            tint = if (isSelected) colors.primary else colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = newCategoryName.trim()
                            if (trimmed.isEmpty()) {
                                Toast.makeText(context, "Please enter a category name", Toast.LENGTH_SHORT).show()
                            } else {
                                onAddCategory(trimmed, selectedIconName)
                                showAddDialog = false
                                Toast.makeText(context, "Category added!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = if (colors.isDark) Color(0xFF0F2625) else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add", fontWeight = FontWeight.SemiBold, fontFamily = Inter)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showAddDialog = false },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = colors.textSecondary, fontFamily = Inter)
                    }
                }
            )
        }

        // Edit Category Dialog
        editingCategory?.let { category ->
            AlertDialog(
                onDismissRequest = { editingCategory = null },
                containerColor = colors.surface,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text(
                        "Edit Category",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = colors.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Category Name", style = MaterialTheme.typography.bodySmall.copy(fontFamily = Inter)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                cursorColor = colors.primary
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = newCategoryName.trim()
                            if (trimmed.isEmpty()) {
                                Toast.makeText(context, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                            } else {
                                onUpdateCategory(category.copy(name = trimmed))
                                editingCategory = null
                                Toast.makeText(context, "Category updated!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = if (colors.isDark) Color(0xFF0F2625) else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold, fontFamily = Inter)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { editingCategory = null },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = colors.textSecondary, fontFamily = Inter)
                    }
                }
            )
        }
    }
}

private fun resolveCategoryMeta(categoryName: String, iconRes: String?, colors: AppColors): Triple<ImageVector, Color, Color> {
    return when (categoryName.lowercase(Locale.getDefault())) {
        "food", "dining" -> Triple(Icons.Default.Restaurant, colors.expense, colors.softCoralBg)
        "travel", "transport" -> Triple(Icons.Default.DirectionsCar, colors.sky, colors.sky.copy(alpha = 0.15f))
        "bills", "utilities" -> Triple(Icons.AutoMirrored.Filled.ReceiptLong, colors.amber, colors.softAmberBg)
        "shopping" -> Triple(Icons.Default.ShoppingBag, Color(0xFFD6778D), Color(0xFFD6778D).copy(alpha = 0.15f))
        "entertainment" -> Triple(Icons.Default.Movie, colors.primary, colors.softTealBg)
        "health", "medical" -> Triple(Icons.Default.MedicalServices, colors.expense, colors.softCoralBg)
        "groceries" -> Triple(Icons.Default.ShoppingCart, colors.income, colors.softGreenBg)
        else -> {
            val icon = when (iconRes) {
                "Restaurant" -> Icons.Default.Restaurant
                "DirectionsCar" -> Icons.Default.DirectionsCar
                "ReceiptLong" -> Icons.AutoMirrored.Filled.ReceiptLong
                "ShoppingBag" -> Icons.Default.ShoppingBag
                "Movie" -> Icons.Default.Movie
                "MedicalServices" -> Icons.Default.MedicalServices
                "ShoppingCart" -> Icons.Default.ShoppingCart
                "School" -> Icons.Default.School
                "FitnessCenter" -> Icons.Default.FitnessCenter
                "Flight" -> Icons.Default.Flight
                "Pets" -> Icons.Default.Pets
                "Star" -> Icons.Default.Star
                else -> Icons.Default.Category
            }
            Triple(icon, colors.sky, colors.sky.copy(alpha = 0.15f))
        }
    }
}
