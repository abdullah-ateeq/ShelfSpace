package com.example.shelfspace.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val shelfProgress = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate alpha and shelf building
        alphaAnim.animateTo(1f, animationSpec = tween(400))
        shelfProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        delay(200) // Total duration ~ 1.2 seconds
        onSplashFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val woodColor = Color(0xFFC4824A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ShelfSpace",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = primaryColor
            )

            Spacer(Modifier.height(24.dp))

            // Animated Shelf Illustration
            Canvas(modifier = Modifier.size(160.dp, 200.dp)) {
                val frameW = size.width
                val frameH = size.height
                val wallThick = 6f
                val shelfThick = 5f

                // Outer cabinet outline
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(frameW, frameH),
                    style = Stroke(wallThick)
                )

                // 4 animated shelves appearing from bottom to top
                val totalShelves = 4
                val compH = (frameH - wallThick * 2) / (totalShelves + 1)
                val progress = shelfProgress.value

                for (i in 1..totalShelves) {
                    val shelfThreshold = i.toFloat() / totalShelves.toFloat()
                    if (progress >= (i - 1).toFloat() / totalShelves.toFloat()) {
                        val subProgress = ((progress - (i - 1).toFloat() / totalShelves.toFloat()) * totalShelves).coerceIn(0f, 1f)
                        val y = frameH - wallThick - (i * compH)
                        val currentW = (frameW - wallThick * 2) * subProgress

                        drawRect(
                            color = woodColor,
                            topLeft = Offset(wallThick, y),
                            size = Size(currentW, shelfThick)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Storage Space Calculator",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            )
        }
    }
}
