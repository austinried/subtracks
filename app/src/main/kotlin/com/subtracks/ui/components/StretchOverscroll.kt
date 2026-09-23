package com.subtracks.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val MAX_STRETCH_FRACTION = 0.35f

@Composable
fun Modifier.stretchOverscroll(): Modifier {
    val overscroll = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val connection =
        remember(overscroll) {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    val current = overscroll.value
                    if (current == 0f || available.y == 0f) return Offset.Zero
                    val next = current + available.y
                    scope.launch {
                        overscroll.snapTo(if (current > 0f) next.coerceAtLeast(0f) else next.coerceAtMost(0f))
                    }
                    return available
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    val current = overscroll.value
                    if (available.y == 0f) return Offset.Zero
                    val next = current + available.y
                    scope.launch {
                        overscroll.snapTo(if (current > 0f) next.coerceAtLeast(0f) else next.coerceAtMost(0f))
                    }
                    return available
                }

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity,
                ): Velocity {
                    overscroll.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                    return available
                }
            }
        }

    return this
        .nestedScroll(connection)
        .graphicsLayer {
            val raw = overscroll.value
            if (raw == 0f) {
                scaleY = 1f
                translationY = 0f
            } else {
                val limit = size.height * MAX_STRETCH_FRACTION
                val capped = raw.coerceIn(-limit, limit)
                val resisted = capped / (1f + abs(capped) / (size.height * 0.5f))
                scaleY = 1f + abs(resisted) / size.height
                translationY = resisted
                transformOrigin = TransformOrigin(0.5f, if (resisted > 0f) 0f else 1f)
            }
        }
}
