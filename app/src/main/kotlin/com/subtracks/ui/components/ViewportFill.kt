package com.subtracks.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

private const val FILL_EPSILON_PX = 1

@Composable
fun rememberViewportFill(state: LazyListState): Dp {
    val density = LocalDensity.current
    val fillPx by remember {
        derivedStateOf {
            val info = state.layoutInfo
            val realCount = info.totalItemsCount - 1
            val visibleReal = info.visibleItemsInfo.filter { it.index < realCount }
            if (realCount <= 0 || visibleReal.size < realCount) {
                FILL_EPSILON_PX
            } else {
                val contentHeight = visibleReal.maxOf { it.offset + it.size } - visibleReal.minOf { it.offset }
                val padding = info.beforeContentPadding + info.afterContentPadding
                (info.viewportSize.height - padding - contentHeight).coerceAtLeast(0) + FILL_EPSILON_PX
            }
        }
    }
    return with(density) { fillPx.toDp() }
}

@Composable
fun rememberViewportFill(state: LazyGridState): Dp {
    val density = LocalDensity.current
    val fillPx by remember {
        derivedStateOf {
            val info = state.layoutInfo
            val realCount = info.totalItemsCount - 1
            val visibleReal = info.visibleItemsInfo.filter { it.index < realCount }
            if (realCount <= 0 || visibleReal.size < realCount) {
                FILL_EPSILON_PX
            } else {
                val contentHeight = visibleReal.maxOf { it.offset.y + it.size.height } - visibleReal.minOf { it.offset.y }
                val padding = info.beforeContentPadding + info.afterContentPadding
                (info.viewportSize.height - padding - contentHeight).coerceAtLeast(0) + FILL_EPSILON_PX
            }
        }
    }
    return with(density) { fillPx.toDp() }
}
