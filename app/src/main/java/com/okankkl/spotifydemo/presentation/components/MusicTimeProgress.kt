package com.okankkl.spotifydemo.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun MusicTimeProgress(
    modifier: Modifier = Modifier,
    musicTime: Float,
    onTap: (Float) -> Unit = {},
    valueRange: ClosedFloatingPointRange<Float>,
    onDragStart: () -> Unit,
    onDragEnd: (position: Float) -> Unit,
) {
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    var barWidth by remember { mutableIntStateOf(0) }
    var activeBarWidth = animateFloatAsState(
        targetValue = dragPosition ?: (musicTime / valueRange.endInclusive),
        animationSpec = if (dragPosition == null) {
            tween(durationMillis = 800)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    )

    val currentMinute = (musicTime / 60).toInt().toString()
    val currentSecond = (musicTime % 60).toInt().toString().padStart(2, '0')

    val leftMinute = ( (valueRange.endInclusive - musicTime) / 60 ).toInt().toString()
    val leftSecond = ( (valueRange.endInclusive - musicTime) % 60).toInt().toString().padStart(2, '0')

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart
        ){
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .onGloballyPositioned { coordinates ->
                        barWidth = coordinates.size.width
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                dragPosition = offset.x
                                onDragStart()
                            },
                            onDragEnd = {
                                val position = activeBarWidth.value + ((dragPosition ?: 0f) - activeBarWidth.value)
                                if (position > 0) { onDragEnd(position) }
                                dragPosition = null
                            },
                            onDragCancel = {
                                dragPosition = null
                            },
                            onHorizontalDrag = { offset, value ->
                                dragPosition = offset.position.x / 1000
                            }
                        )
                    }
                    .pointerInput(Unit){
                        detectTapGestures(
                            onPress = { offset ->
                                val position = offset.x / barWidth
                                if (offset.x > 0 && abs(offset.x - activeBarWidth.value) > 100) {
                                    onTap(position)
                                }
                            }
                        )
                    }
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            )

            // Active Progress Bar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(activeBarWidth.value)
                        .height(5.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 36.dp, bottomStart = 36.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = CircleShape
                        )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${currentMinute}:$currentSecond",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.W400,
                fontSize = 11.sp
            )
            Text(
                text = "$leftMinute:$leftSecond",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.W400,
                fontSize = 11.sp
            )
        }
    }
}