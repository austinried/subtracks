package com.subtracks.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.flow.first
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal suspend fun loadArtistArtCovers(
    library: LibraryRepository,
    source: SourceRepository,
    artistId: String,
): List<CoverArtRef> {
    val sourceId = library.activeSourceId.first() ?: return emptyList()
    val own = library.artistOwnCoverArt(sourceId, artistId)
    val ids =
        if (own != null) {
            listOf(own)
        } else {
            library.artistFallbackCoverArts(sourceId, artistId)
        }
    return ids.take(4).mapNotNull { source.coverArt(it, thumbnail = true) }
}

@Composable
fun ArtistIcon(
    artist: Artist,
    fallback: CoverArtRef?,
    loadCovers: suspend (String) -> List<CoverArtRef>,
    modifier: Modifier = Modifier,
) {
    val covers by produceState(initialValue = emptyList<CoverArtRef>(), artist.id) {
        value = loadCovers(artist.id)
    }
    if (covers.size >= 2) {
        ArtistArtGrid(covers = covers, name = artist.name, modifier = modifier)
    } else {
        CoverArt(
            ref = covers.firstOrNull() ?: fallback,
            name = artist.name,
            showPlaceholder = covers.isEmpty() && fallback == null,
            modifier = modifier,
        )
    }
}

@Composable
fun ArtistArtGrid(
    covers: List<CoverArtRef>,
    name: String,
    modifier: Modifier = Modifier,
    singleThumbnail: CoverArtRef? = null,
) {
    when {
        covers.size >= 4 -> {
            Column(modifier) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    covers.take(2).forEach { ref ->
                        CoverArt(ref = ref, name = name, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    covers.drop(2).take(2).forEach { ref ->
                        CoverArt(ref = ref, name = name, modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }

        covers.size == 3 -> {
            Box(modifier) {
                covers.forEachIndexed { index, ref ->
                    CoverArt(
                        ref = ref,
                        name = name,
                        modifier = Modifier.matchParentSize().clip(YRegionShape(index)),
                    )
                }
            }
        }

        covers.size == 2 -> {
            Row(modifier) {
                covers.forEach { ref ->
                    CoverArt(ref = ref, name = name, modifier = Modifier.weight(1f).fillMaxHeight())
                }
            }
        }

        covers.size == 1 -> {
            CoverArt(
                ref = covers.first(),
                name = name,
                thumbnailRef = singleThumbnail,
                showPlaceholder = false,
                modifier = modifier,
            )
        }
    }
}

private class YRegionShape(
    private val region: Int,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val center = Offset(size.width / 2f, size.height / 2f)
        val left = rayPoint(size, 210.0)
        val right = rayPoint(size, 330.0)
        val bottom = Offset(size.width / 2f, size.height)
        val path = Path()
        when (region) {
            0 -> {
                path.moveTo(center.x, center.y)
                path.lineTo(left.x, left.y)
                addClockwise(path, size, left, right)
            }

            1 -> {
                path.moveTo(center.x, center.y)
                path.lineTo(bottom.x, bottom.y)
                addClockwise(path, size, bottom, left)
            }

            else -> {
                path.moveTo(center.x, center.y)
                path.lineTo(right.x, right.y)
                addClockwise(path, size, right, bottom)
            }
        }
        path.lineTo(center.x, center.y)
        path.close()
        return Outline.Generic(path)
    }
}

private fun rayPoint(
    size: Size,
    angleDegrees: Double,
): Offset {
    val cx = size.width / 2.0
    val cy = size.height / 2.0
    val radians = angleDegrees * PI / 180.0
    val dx = cos(radians)
    val dy = sin(radians)
    val tx =
        when {
            dx > 0 -> (size.width - cx) / dx
            dx < 0 -> -cx / dx
            else -> Double.POSITIVE_INFINITY
        }
    val ty =
        when {
            dy > 0 -> (size.height - cy) / dy
            dy < 0 -> -cy / dy
            else -> Double.POSITIVE_INFINITY
        }
    val t = min(tx, ty)
    return Offset((cx + dx * t).toFloat(), (cy + dy * t).toFloat())
}

private fun addClockwise(
    path: Path,
    size: Size,
    from: Offset,
    to: Offset,
) {
    val w = size.width
    val h = size.height
    val corners =
        listOf(
            Offset(0f, 0f),
            Offset(w, 0f),
            Offset(w, h),
            Offset(0f, h),
        )
    val fromParam = perimeterParam(from, size)
    val toParam = perimeterParam(to, size)
    if (fromParam <= toParam) {
        corners
            .filter { perimeterParam(it, size) > fromParam && perimeterParam(it, size) < toParam }
            .forEach { path.lineTo(it.x, it.y) }
    } else {
        corners
            .filter { perimeterParam(it, size) > fromParam }
            .forEach { path.lineTo(it.x, it.y) }
        corners
            .filter { perimeterParam(it, size) < toParam }
            .forEach { path.lineTo(it.x, it.y) }
    }
    path.lineTo(to.x, to.y)
}

private fun perimeterParam(
    point: Offset,
    size: Size,
): Float {
    val w = size.width
    val h = size.height
    return when {
        point.y <= 0f -> point.x
        point.x >= w -> w + point.y
        point.y >= h -> w + h + (w - point.x)
        else -> 2f * w + h + (h - point.y)
    }
}
