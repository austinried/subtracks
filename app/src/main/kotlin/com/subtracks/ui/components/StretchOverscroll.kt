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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

private const val MAX_STRETCH_FRACTION = 0.30f

private object StretchOverscrollFactory : OverscrollFactory {
    override fun createOverscrollEffect(): OverscrollEffect = StretchOverscrollEffect()

    override fun hashCode(): Int = 0x5EED_5EED

    override fun equals(other: Any?): Boolean = other is StretchOverscrollFactory
}

private class StretchOverscrollEffect : OverscrollEffect {
    private val overscroll = mutableFloatStateOf(0f)

    private val drawNode =
        object : Modifier.Node(), DrawModifierNode {
            override fun ContentDrawScope.draw() {
                val raw = overscroll.floatValue
                if (raw == 0f) {
                    drawContent()
                    return
                }
                val limit = size.height * MAX_STRETCH_FRACTION
                val capped = raw.coerceIn(-limit, limit)
                val resisted = capped / (1f + abs(capped) / (size.height * 0.5f))
                val pivot = Offset(size.width / 2f, if (resisted > 0f) 0f else size.height)
                scale(scaleX = 1f, scaleY = 1f + abs(resisted) / size.height, pivot = pivot) {
                    translate(left = 0f, top = resisted) {
                        this@draw.drawContent()
                    }
                }
            }
        }

    override val node: DelegatableNode get() = drawNode

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
            val next = current + leftover
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
            Animatable(overscroll.floatValue).animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) {
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
    CompositionLocalProvider(LocalOverscrollFactory provides StretchOverscrollFactory) {
        Box(modifier) { content() }
    }
}
