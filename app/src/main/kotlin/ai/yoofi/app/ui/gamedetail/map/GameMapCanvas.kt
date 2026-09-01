package ai.yoofi.app.ui.gamedetail.map

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameMap
import ai.yoofi.app.domain.gamedetail.GameMapLocation
import ai.yoofi.app.domain.gamedetail.GameMapMarkerKind
import ai.yoofi.app.ui.theme.YoofiAccent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val LabelShape = RoundedCornerShape(8.dp)
private val PageBg = Color(0xFF1C1528)

/**
 * 可拖动底图 + 按归一化坐标打点。
 * 视口裁切；钉和标签跟着图一起走。
 */
@Composable
internal fun GameMapCanvas(
    map: GameMap,
    modifier: Modifier = Modifier,
) {
    val painter = painterResource(mapImageRes(map.imageKey))
    val density = LocalDensity.current
    val fallbackPx = with(density) { 916.dp.toPx() }
    val intrinsic = painter.intrinsicSize
    val mapWidthPx = if (intrinsic.isSpecified) intrinsic.width else fallbackPx
    val mapHeightPx = if (intrinsic.isSpecified) intrinsic.height else fallbackPx
    val mapWidthDp = with(density) { mapWidthPx.toDp() }
    val mapHeightDp = with(density) { mapHeightPx.toDp() }

    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var offset by remember(map.id) { mutableStateOf(Offset.Zero) }

    LaunchedEffect(map.id, viewport, mapWidthPx, mapHeightPx) {
        if (viewport.width == 0 || viewport.height == 0) return@LaunchedEffect
        offset = coerceMapOffset(
            offset = Offset(map.startOffsetX * mapWidthPx, map.startOffsetY * mapHeightPx),
            mapWidth = mapWidthPx,
            mapHeight = mapHeightPx,
            viewportWidth = viewport.width.toFloat(),
            viewportHeight = viewport.height.toFloat(),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { viewport = it }
            .pointerInput(map.id, viewport, mapWidthPx, mapHeightPx) {
                detectDragGestures { _, drag ->
                    offset = coerceMapOffset(
                        offset = offset + drag,
                        mapWidth = mapWidthPx,
                        mapHeight = mapHeightPx,
                        viewportWidth = viewport.width.toFloat(),
                        viewportHeight = viewport.height.toFloat(),
                    )
                }
            },
    ) {
        Box(
            modifier = Modifier.offset {
                IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
            },
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(mapWidthDp, mapHeightDp),
            )
            map.locations.forEach { mark ->
                MapMarker(
                    mark = mark,
                    mapWidthPx = mapWidthPx,
                    mapHeightPx = mapHeightPx,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.031f to PageBg,
                        0.353f to Color.Transparent,
                        0.731f to Color.Transparent,
                        0.972f to PageBg,
                    ),
                ),
        )
    }
}

@Composable
private fun MapMarker(
    mark: GameMapLocation,
    mapWidthPx: Float,
    mapHeightPx: Float,
) {
    val x = (mark.x * mapWidthPx).roundToInt()
    val y = (mark.y * mapHeightPx).roundToInt()
    when (mark.kind) {
        GameMapMarkerKind.Label -> {
            Text(
                text = mark.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .offset { IntOffset(x, y) }
                    .background(YoofiAccent, LabelShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        GameMapMarkerKind.Pin -> {
            Image(
                painter = painterResource(R.drawable.ic_map_pin),
                contentDescription = stringResource(R.string.cd_map_pin),
                modifier = Modifier
                    .offset { IntOffset(x, y) }
                    .size(36.dp),
            )
        }
    }
}

@DrawableRes
internal fun mapImageRes(imageKey: String): Int = when (imageKey) {
    "demo-world" -> R.drawable.img_game_map_01
    else -> R.drawable.img_game_map_01
}
