package com.subtracks.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

private const val MAX_STRETCH_FRACTION = 0.14f

private class StretchOverscrollEffect : OverscrollEffect {
    val overscroll = mutableFloatStateOf(0f)
    var limitPx: Float = 1f

    private val emptyNode = object : Modifier.Node() {}

    override val node: DelegatableNode get() = emptyNode

    override val isInProgress: Boolean get() = overscroll.floatValue != 0f

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        if (delta.y == 0f) return performScroll(delta)
        var remainingY = delta.y
        if (overscroll.floatValue != 0f) {
            val current = overscroll.floatValue
            val next = current + remainingY
            val clamped = if (current > 0f) next.coerceAtLeast(0f) else next.coerceAtMost(0f)
            remainingY -= clamped - current
            overscroll.floatValue = clamped
        }
        val consumed = performScroll(Offset(delta.x, remainingY))
        val leftover = remainingY - consumed.y
        if (leftover != 0f) {
            val current = overscroll.floatValue
            val next = (current + leftover).coerceIn(-limitPx, limitPx)
            overscroll.floatValue =
                when {
                    current > 0f -> next.coerceAtLeast(0f)
                    current < 0f -> next.coerceAtMost(0f)
                    else -> next
                }
        }
        return Offset(consumed.x, delta.y)
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        if (overscroll.floatValue != 0f) {
            Animatable(overscroll.floatValue)
                .animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) {
                    overscroll.floatValue = value
                }
        }
        performFling(velocity)
    }
}

@Composable
fun StretchOverscroll(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val effect = remember { StretchOverscrollEffect() }
    val factory =
        remember {
            object : OverscrollFactory {
                override fun createOverscrollEffect(): OverscrollEffect = effect

                override fun hashCode(): Int = 0x5EED_1234

                override fun equals(other: Any?): Boolean = other === this
            }
        }

    CompositionLocalProvider(LocalOverscrollFactory provides factory) {
        Box(
            modifier =
                modifier
                    .onSizeChanged { effect.limitPx = (it.height * MAX_STRETCH_FRACTION).coerceAtLeast(1f) }
                    .graphicsLayer {
                        val value = effect.overscroll.floatValue
                        if (value == 0f) return@graphicsLayer
                        val height = size.height
                        val resisted = value / (1f + abs(value) / (height * 0.35f))
                        scaleY = 1f + abs(resisted) / height
                        translationY = resisted
                        transformOrigin = TransformOrigin(0.5f, if (resisted > 0f) 0f else 1f)
                    },
        ) {
            content()
        }
    }
}
