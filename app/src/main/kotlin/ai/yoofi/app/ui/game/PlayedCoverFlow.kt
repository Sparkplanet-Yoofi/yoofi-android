package ai.yoofi.app.ui.game

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** 单张玩过的封面，后续接详情路由时用 [id] 跳转。 */
data class PlayedCover(
    val id: String,
    @param:DrawableRes val coverRes: Int,
    @param:StringRes val titleRes: Int,
)

private val CoverFlowWidth = 350.dp
private val CoverFlowHeight = 182.dp
private val CardWidth = 138.dp
private val CardHeight = 182.dp
private val CardRadius = 20.dp
/** Figma 中心卡左缘 106，视觉中心 175 */
private val FocusedCenterX = 175.dp
/** 邻卡相对中心的位移 / 缩放，对齐 `982:14810` */
private val SideShift = 64.66.dp
private val FarShift = 122.08.dp
private val ExitShift = 179.5.dp
private const val SideScale = 121.319f / 138f
private const val FarScale = 106.154f / 138f
private const val ExitScale = 0.62f
/** 越界后仍可见的最大距离，超出不再绘制 */
private const val MaxVisibleDelta = 2.55f
private const val OverscrollFactor = 0.28f

private val OverlayBrush = Brush.verticalGradient(
    0.57105f to Color.Transparent,
    0.72478f to Color(0x7A110F3A),
    0.96459f to Color(0x8A1F003F),
)

private val DemoPlayedCovers = listOf(
    PlayedCover("played-a1", R.drawable.img_game_cover_a, R.string.card_arranged_marriage),
    PlayedCover("played-b", R.drawable.img_game_cover_b, R.string.card_arranged_marriage),
    PlayedCover("played-c", R.drawable.img_game_cover_c, R.string.card_arranged_marriage),
    PlayedCover("played-e", R.drawable.img_game_cover_e, R.string.card_arranged_marriage),
    PlayedCover("played-d", R.drawable.img_game_cover_d, R.string.card_arranged_marriage),
    PlayedCover("played-a2", R.drawable.img_game_cover_a, R.string.card_arranged_marriage),
    PlayedCover("played-c2", R.drawable.img_game_cover_c, R.string.card_arranged_marriage),
)

/**
 * 跟手封面流：位移与缩放按距焦点的连续距离插值，避免分页跳变。
 * 松手弹簧吸附；点侧卡弹到中心，点中心卡视为打开该 item。
 */
@Composable
fun PlayedCoverFlow(
    modifier: Modifier = Modifier,
    items: List<PlayedCover> = DemoPlayedCovers,
    onItemClick: (PlayedCover) -> Unit = {},
) {
    if (items.isEmpty()) {
        Box(modifier.width(CoverFlowWidth).height(CoverFlowHeight))
        return
    }
    val lastIndex = items.lastIndex
    val initial = (items.size / 2).coerceIn(0, lastIndex)
    var focus by remember(lastIndex) { mutableFloatStateOf(initial.toFloat()) }
    val scope = rememberCoroutineScope()
    val itemsState = rememberUpdatedState(items)
    val onItemClickState = rememberUpdatedState(onItemClick)
    val motionJob = remember(lastIndex) { arrayOfNulls<Job>(1) }
    val snapSpring = remember {
        spring<Float>(dampingRatio = 0.86f, stiffness = 380f)
    }

    fun cancelMotion() {
        motionJob[0]?.cancel()
        motionJob[0] = null
    }

    fun animateFocusTo(target: Float, velocity: Float = 0f) {
        cancelMotion()
        val start = focus
        motionJob[0] = scope.launch {
            animate(
                initialValue = start,
                targetValue = target.coerceIn(0f, lastIndex.toFloat()),
                initialVelocity = velocity,
                animationSpec = snapSpring,
            ) { value, _ ->
                focus = value
            }
        }
    }

    fun openOrFocus(index: Int) {
        val item = itemsState.value.getOrNull(index) ?: return
        if (abs(index - focus) < 0.35f) {
            onItemClickState.value(item)
        } else {
            animateFocusTo(index.toFloat())
        }
    }

    Box(
        modifier = modifier
            .width(CoverFlowWidth)
            .height(CoverFlowHeight)
            .coverFlowGestures(
                lastIndex = lastIndex,
                focused = { focus },
                onDragStart = { cancelMotion() },
                onDragTo = { value -> focus = value },
                onSettle = { velocityPx, stepPx ->
                    val target = snapTarget(
                        current = focus,
                        velocityPx = velocityPx,
                        stepPx = stepPx,
                        lastIndex = lastIndex,
                    )
                    animateFocusTo(
                        target = target.toFloat(),
                        velocity = -velocityPx / stepPx,
                    )
                },
                onTapIndex = { index -> openOrFocus(index) },
            ),
    ) {
        val drawOrder = items.indices
            .filter { abs(it - focus) < MaxVisibleDelta }
            .sortedByDescending { abs(it - focus) }
        drawOrder.forEach { index ->
            val item = items[index]
            val delta = index - focus
            val slot = coverSlot(delta)
            val title = stringResource(item.titleRes)
            PlayedCard(
                coverRes = item.coverRes,
                title = title,
                scale = slot.scale,
                elevation = slot.elevation,
                alpha = slot.alpha,
                modifier = Modifier
                    .zIndex(MaxVisibleDelta - abs(delta))
                    .offset {
                        val x = (
                            slot.centerX.toPx() - CardWidth.toPx() / 2f
                            ).roundToInt()
                        IntOffset(x, 0)
                    }
                    .semantics {
                        contentDescription = title
                        onClick {
                            openOrFocus(index)
                            true
                        }
                    },
            )
        }
    }
}

private fun Modifier.coverFlowGestures(
    lastIndex: Int,
    focused: () -> Float,
    onDragStart: () -> Unit,
    onDragTo: (Float) -> Unit,
    onSettle: (velocityPx: Float, stepPx: Float) -> Unit,
    onTapIndex: (Int) -> Unit,
): Modifier = this.then(
    Modifier.pointerInput(lastIndex) {
        val stepPx = SideShift.toPx().coerceAtLeast(1f)
        val slop = viewConfiguration.touchSlop
        val minFling = viewConfiguration.minimumFlingVelocity
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            onDragStart()
            val tracker = VelocityTracker()
            tracker.addPosition(down.uptimeMillis, down.position)
            var dragging = false
            var totalDx = 0f
            var totalDy = 0f
            val pointerId = down.id
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                if (change.changedToUpIgnoreConsumed()) {
                    if (dragging) {
                        val velocityX = tracker.calculateVelocity().x
                        val fling = if (abs(velocityX) >= minFling) velocityX else 0f
                        onSettle(fling, stepPx)
                    } else if (abs(totalDx) < slop && abs(totalDy) < slop) {
                        hitCoverIndex(
                            position = down.position,
                            focused = focused(),
                            itemCount = lastIndex + 1,
                        )?.let(onTapIndex)
                    }
                    break
                }
                val delta = change.positionChange()
                totalDx += delta.x
                totalDy += delta.y
                tracker.addPosition(change.uptimeMillis, change.position)
                if (!dragging && abs(totalDx) > slop && abs(totalDx) > abs(totalDy)) {
                    dragging = true
                }
                if (dragging) {
                    change.consume()
                    onDragTo(
                        rubberBand(
                            value = focused() - delta.x / stepPx,
                            min = 0f,
                            max = lastIndex.toFloat(),
                        ),
                    )
                }
            }
        }
    },
)

private data class CoverSlot(
    val centerX: Dp,
    val scale: Float,
    val elevation: Dp,
    val alpha: Float,
)

/**
 * 以 Figma 三档（中 / 邻 / 远）为锚点，余弦缓动插值。
 * 缩放绕卡片中心，邻卡/远卡的下沉由缩放自然产生，对应 y=11 / y=21。
 */
private fun coverSlot(delta: Float): CoverSlot {
    val sign = if (delta >= 0f) 1f else -1f
    val ad = abs(delta)
    val scale: Float
    val shift: Dp
    when {
        ad <= 1f -> {
            val t = easeInOut(ad)
            scale = lerp(1f, SideScale, t)
            shift = lerpDp(0.dp, SideShift, t)
        }
        ad <= 2f -> {
            val t = easeInOut(ad - 1f)
            scale = lerp(SideScale, FarScale, t)
            shift = lerpDp(SideShift, FarShift, t)
        }
        else -> {
            val t = easeInOut((ad - 2f).coerceIn(0f, 1f))
            scale = lerp(FarScale, ExitScale, t)
            shift = lerpDp(FarShift, ExitShift, t)
        }
    }
    val elevationFactor = (1f - ad.coerceAtMost(1f))
    val alpha = if (ad <= 2f) {
        1f
    } else {
        lerp(1f, 0f, ((ad - 2f) / 0.55f).coerceIn(0f, 1f))
    }
    return CoverSlot(
        centerX = FocusedCenterX + shift * sign,
        scale = scale,
        elevation = 4.dp * elevationFactor,
        alpha = alpha,
    )
}

private fun easeInOut(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return (1f - cos(x * PI.toFloat())) / 2f
}

private fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp =
    start + (stop - start) * fraction

private fun rubberBand(value: Float, min: Float, max: Float): Float = when {
    value < min -> min + (value - min) * OverscrollFactor
    value > max -> max + (value - max) * OverscrollFactor
    else -> value
}

private fun snapTarget(
    current: Float,
    velocityPx: Float,
    stepPx: Float,
    lastIndex: Int,
): Int {
    val velocityItems = -velocityPx / stepPx
    val page = when {
        velocityItems > 0.45f -> ceil(current - 0.001f).toInt()
        velocityItems < -0.45f -> floor(current + 0.001f).toInt()
        else -> current.roundToInt()
    }
    return page.coerceIn(0, lastIndex)
}

private fun androidx.compose.ui.unit.Density.hitCoverIndex(
    position: Offset,
    focused: Float,
    itemCount: Int,
): Int? {
    return (0 until itemCount)
        .filter { abs(it - focused) < MaxVisibleDelta }
        .sortedBy { abs(it - focused) }
        .firstOrNull { index ->
            val slot = coverSlot(index - focused)
            val width = CardWidth.toPx() * slot.scale
            val height = CardHeight.toPx() * slot.scale
            val cx = slot.centerX.toPx()
            val cy = CardHeight.toPx() / 2f
            position.x in (cx - width / 2f)..(cx + width / 2f) &&
                position.y in (cy - height / 2f)..(cy + height / 2f)
        }
}

@Composable
private fun PlayedCard(
    coverRes: Int,
    title: String,
    scale: Float,
    elevation: Dp,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(CardRadius)
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                shadowElevation = elevation.toPx()
                this.shape = shape
                clip = true
            }
            .size(CardWidth, CardHeight),
    ) {
        Image(
            painter = painterResource(coverRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OverlayBrush),
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, end = 10.dp, bottom = 12.dp),
        )
    }
}

@Preview(widthDp = 351, heightDp = 182, showBackground = true, backgroundColor = 0xFF131126)
@Composable
private fun PlayedCoverFlowPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        PlayedCoverFlow()
    }
}
