package com.subtracks.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.paging.LoadState

private const val FILL_EPSILON_PX = 1

/**
 * Scrolls back to the top once a new filter/sort has actually loaded.
 *
 * Scrolling immediately after [resetKey] changes does not stick: the lazy layout keeps the
 * previously visible item in place by key while the paged list reloads, which snaps the scroll
 * back. Waiting for the refresh triggered by the change and resetting afterwards avoids that.
 */
@Composable
fun ResetScrollOnChange(
    resetKey: Any?,
    refreshState: () -> LoadState,
    onReset: suspend () -> Unit,
) {
    var pending by remember { mutableStateOf(false) }
    var primed by remember { mutableStateOf(false) }
    LaunchedEffect(resetKey) {
        if (primed) pending = true else primed = true
    }
    LaunchedEffect(Unit) {
        var refreshes = 0
        var handled = 0
        snapshotFlow(refreshState).collect { state ->
            if (state is LoadState.Loading) {
                refreshes++
            } else if (pending && refreshes > handled) {
                onReset()
                handled = refreshes
                pending = false
            }
        }
    }
}

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
