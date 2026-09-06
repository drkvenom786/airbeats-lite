package com.darkxvenom.airbeats.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassBottomNavigationBar(
    modifier: Modifier = Modifier,
    items: List<CurvedBottomNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    backdrop: PlatformBackdrop
) {
    val layer = rememberGraphicsLayer()
    val luminanceAnimation = remember { Animatable(0.3f) }
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val glassBorderBrush = remember(isDarkTheme) {
        if (isDarkTheme) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.9f),
                    Color.Black.copy(alpha = 0.05f)
                )
            )
        }
    }

    val indicatorBgColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    val indicatorBorderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.25f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }

    val unselectedIconColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.7f)
    } else {
        Color.Black.copy(alpha = 0.6f)
    }

    val activeIconColor = Color(0xFFF9A825) // Vibrant Amber / Gold

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth(0.72f)
                .drawBackdropCustomShape(
                    backdrop = backdrop,
                    layer = layer,
                    luminanceAnimation = luminanceAnimation.value,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(
                    if (isDarkTheme) Color.Black.copy(alpha = 0.25f)
                    else Color.White.copy(alpha = 0.35f)
                )
                .border(
                    width = 1.dp,
                    brush = glassBorderBrush,
                    shape = CircleShape
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            val totalWidth = maxWidth
            val itemCount = items.size.coerceAtLeast(1)
            val itemWidth = totalWidth / itemCount

            // Smooth sliding indicator capsule
            val animatedOffset by animateFloatAsState(
                targetValue = selectedIndex * itemWidth.value,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "indicator_offset"
            )

            // Animated sliding pill indicator
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = animatedOffset.dp.toPx()
                    }
                    .size(width = itemWidth, height = maxHeight)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(indicatorBgColor)
                    .border(
                        width = 1.dp,
                        color = indicatorBorderColor,
                        shape = CircleShape
                    )
            )

            // Navigation icons row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.18f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "icon_scale_$index"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) activeIconColor else unselectedIconColor,
                        animationSpec = tween(durationMillis = 250),
                        label = "icon_color_$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onItemSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = if (isSelected) item.iconActive else item.iconInactive),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )
                    }
                }
            }
        }
    }
}

