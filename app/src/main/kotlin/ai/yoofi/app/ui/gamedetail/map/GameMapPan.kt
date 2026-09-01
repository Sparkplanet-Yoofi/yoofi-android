package ai.yoofi.app.ui.gamedetail.map

import androidx.compose.ui.geometry.Offset
import kotlin.math.max

/**
 * 正方形底图的绘制边长：至少稿面 [designPx]，且盖住视口（cover）。
 * 不能用 PNG 像素当 dp，否则 xxhdpi 上图比屏小，四周漏底色。
 */
internal fun mapCoverDisplayPx(
    designPx: Float,
    viewportWidth: Float,
    viewportHeight: Float,
): Float {
    if (designPx <= 0f) return 0f
    if (viewportWidth <= 0f || viewportHeight <= 0f) return designPx
    val cover = max(viewportWidth / designPx, viewportHeight / designPx)
    return designPx * max(1f, cover)
}

/**
 * 把拖动偏移夹在「图始终铺满视口」的范围内。
 * 图比屏小则居中；图比屏大则不允许拖出空白边。
 */
internal fun coerceMapOffset(
    offset: Offset,
    mapWidth: Float,
    mapHeight: Float,
    viewportWidth: Float,
    viewportHeight: Float,
): Offset {
    if (viewportWidth <= 0f || viewportHeight <= 0f) return offset
    val x = coerceAxis(offset.x, mapWidth, viewportWidth)
    val y = coerceAxis(offset.y, mapHeight, viewportHeight)
    return Offset(x, y)
}

private fun coerceAxis(value: Float, map: Float, viewport: Float): Float {
    if (map <= viewport) {
        val centered = (viewport - map) / 2f
        return centered
    }
    val min = viewport - map
    return value.coerceIn(min, 0f)
}
