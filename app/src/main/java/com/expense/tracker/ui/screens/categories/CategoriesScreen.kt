package com.expense.tracker.ui.screens.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expense.tracker.data.local.entity.CategoryEntity
import com.expense.tracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<CategoryEntity>,
    onBack: () -> Unit
) {
    val colors = AppTheme.colors

    Scaffold(
        containerColor = colors.background,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
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
                            text = "Expense Categories",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Standard categories to organize and track your outgoing expenses.",
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
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
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
                            val (icon, tint, bg) = resolveCategoryMeta(category.name, colors)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = category.name,
                                                tint = tint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        ),
                                        color = colors.textPrimary
                                    )
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
}

private fun resolveCategoryMeta(categoryName: String, colors: AppColors): Triple<ImageVector, Color, Color> {
    return when (categoryName.lowercase()) {
        "food", "dining" -> Triple(Icons.Default.Restaurant, colors.expense, colors.softCoralBg)
        "travel", "transport" -> Triple(Icons.Default.DirectionsCar, colors.sky, colors.sky.copy(alpha = 0.15f))
        "bills", "utilities" -> Triple(Icons.AutoMirrored.Filled.ReceiptLong, colors.amber, colors.softAmberBg)
        "shopping" -> Triple(Icons.Default.ShoppingBag, Color(0xFFD6778D), Color(0xFFD6778D).copy(alpha = 0.15f))
        "entertainment" -> Triple(Icons.Default.Movie, colors.primary, colors.softTealBg)
        "health", "medical" -> Triple(Icons.Default.MedicalServices, colors.expense, colors.softCoralBg)
        "groceries" -> Triple(Icons.Default.ShoppingCart, colors.income, colors.softGreenBg)
        else -> Triple(Icons.Default.Category, colors.sky, colors.sky.copy(alpha = 0.15f))
    }
}
