package ai.yoofi.app.ui.gamedetail.map

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameMap
import ai.yoofi.app.domain.gamedetail.GameMapDesignSizeDp
import ai.yoofi.app.domain.gamedetail.GameMapLocation
import ai.yoofi.app.domain.gamedetail.GameMapMarkerKind
import ai.yoofi.app.ui.gamedetail.DetailActionBrush
import ai.yoofi.app.ui.theme.YoofiAccent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
    selectedLocationId: String,
    onSelectLocation: (String) -> Unit,
    onDismissGo: () -> Unit,
    onConfirmGo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val painter = painterResource(mapImageRes(map.imageKey))
    val density = LocalDensity.current
    val designPx = with(density) { GameMapDesignSizeDp.dp.toPx() }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val mapPx = mapCoverDisplayPx(
        designPx = designPx,
        viewportWidth = viewport.width.toFloat(),
        viewportHeight = viewport.height.toFloat(),
    )
    val mapWidthPx = mapPx
    val mapHeightPx = mapPx
    val mapWidthDp = with(density) { mapWidthPx.toDp() }
    val mapHeightDp = with(density) { mapHeightPx.toDp() }

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
            }
            .pointerInput(selectedLocationId) {
                detectTapGestures {
                    if (selectedLocationId.isNotEmpty()) onDismissGo()
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
                    selected = mark.id == selectedLocationId,
                    mapWidthPx = mapWidthPx,
                    mapHeightPx = mapHeightPx,
                    onSelect = { onSelectLocation(mark.id) },
                    onConfirmGo = onConfirmGo,
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
    selected: Boolean,
    mapWidthPx: Float,
    mapHeightPx: Float,
    onSelect: () -> Unit,
    onConfirmGo: () -> Unit,
) {
    val density = LocalDensity.current
    val calloutWidthPx = with(density) { MapGoCalloutWidth.roundToPx() }
    val calloutShiftY = with(density) { (MapGoCalloutHeight + MapGoCalloutGap).roundToPx() }
    val x = (mark.x * mapWidthPx).roundToInt()
    val y = (mark.y * mapHeightPx).roundToInt()
    var markerSize by remember(mark.id) { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .offset { IntOffset(x, y) }
            .onSizeChanged { markerSize = it }
            .zIndex(if (selected) 1f else 0f),
    ) {
        when (mark.kind) {
            GameMapMarkerKind.Label -> {
                Text(
                    text = mark.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .then(
                            if (selected) {
                                Modifier.background(DetailActionBrush, LabelShape)
                            } else {
                                Modifier.background(YoofiAccent, LabelShape)
                            },
                        )
                        .clickable(role = Role.Button, onClick = onSelect)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
            GameMapMarkerKind.Pin -> {
                Image(
                    painter = painterResource(R.drawable.ic_map_pin),
                    contentDescription = stringResource(R.string.cd_map_pin),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(role = Role.Button, onClick = onSelect),
                )
            }
        }
    }
    if (selected && markerSize.width > 0) {
        GameMapGoCallout(
            previewRes = mapLocationPreviewRes(mark.previewKey),
            onGo = onConfirmGo,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x + markerSize.width / 2 - calloutWidthPx / 2,
                        y - calloutShiftY,
                    )
                }
                .zIndex(2f),
        )
    }
}

@DrawableRes
internal fun mapImageRes(imageKey: String): Int = when (imageKey) {
    "demo-world" -> R.drawable.img_game_map_01
    else -> R.drawable.img_game_map_01
}

/** 地点预览，不能用默认聊天底，否则点 Go 后看不出底图换了。 */
@DrawableRes
internal fun mapLocationPreviewRes(imageKey: String): Int = when (imageKey) {
    "demo-go" -> R.drawable.img_home_hero
    else -> R.drawable.img_home_hero
}
