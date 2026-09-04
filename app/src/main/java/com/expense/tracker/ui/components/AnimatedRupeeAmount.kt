package com.expense.tracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedRupeeAmount(
    targetAmount: Double,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier: Modifier = Modifier,
    prefix: String = "₹"
) {
    val animatedValue = remember { Animatable(targetAmount.toFloat()) }
    val previousAmount = remember { mutableStateOf(targetAmount) }

    LaunchedEffect(targetAmount) {
        if (previousAmount.value != targetAmount) {
            previousAmount.value = targetAmount
            animatedValue.animateTo(
                targetValue = targetAmount.toFloat(),
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        } else {
            animatedValue.snapTo(targetAmount.toFloat())
        }
    }

    Text(
        text = "$prefix${"%.2f".format(animatedValue.value)}",
        style = style,
        color = color,
        modifier = modifier
    )
}
