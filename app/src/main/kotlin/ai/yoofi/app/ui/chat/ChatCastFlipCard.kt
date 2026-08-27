package ai.yoofi.app.ui.chat

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiChatSummonGlowInner
import ai.yoofi.app.ui.theme.YoofiChatSummonGlowOuter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 人物卡尺寸，来自 Figma 1826:9321（298 x 416，圆角 14.634） */
private val CastCardWidth = 298.dp
private val CastCardHeight = 416.dp
private val CastCardShape = RoundedCornerShape(14.634.dp)

/** 与 iOS ChatRoomCharacterView 的 startsWithCardBack + goldenSummon 入场保持一致的时序 */
private const val CastEnterDurationMillis = 260
private const val CastFlipDelayMillis = 520L
private const val CastFlipDurationMillis = 720
private const val CastFlipHalfAngle = 90f
private const val CastFlipEndAngle = 180f

/**
 * 卡背自转一圈的角度与时长。
 *
 * 卡背正反面是同一张贴图，转过 180° 就已经回到视觉上完全一致的姿态，
 * 所以「一圈」对应 180° 而不是 360°；写成 360° 会让卡片正对观众三次，看起来像转了三圈。
 */
private const val CastSpinEndAngle = 180f
private const val CastSpinDurationMillis = 1440

/** 透视距离系数，值越小透视越夸张；8 倍密度接近 iOS 卡牌翻转观感 */
private const val CastCameraDistanceFactor = 8f

/** 光晕相对卡面的最大放大倍数 */
private const val CastGlowMaxScale = 1.35f

/** 人物卡揭示特效时长，shader 内部已带缓动，这里用线性推进 progress */
private const val CastRevealDurationMillis = 900

/** 扫光循环周期，对齐 CodePen 的 `animation: shark-wrap 2s infinite` */
private const val CastShineDurationMillis = 2000

/** 扫光带偏离竖直方向的倾角（tan 值），0.27 约等于 15°，对齐 iOS 的近竖直光带 */
private const val CastShineTiltRatio = 0.27f

/** 扫光带半宽，占扫光轴长度的比例；越小光带越细 */
private const val CastShineHalfWidth = 0.05f

/** 扫光带峰值透明度 */
private const val CastShineAlpha = 0.35f

/**
 * Cast 卡牌：Yoofi 卡背先自转整整一圈后停住等待用户点击，点击后沿 Y 轴翻转 180°
 * 揭示人物卡（Figma 1826:9321），并按 [effect] 跑与 iOS 一致的揭示特效。
 *
 * 自转期间正反两面都是卡背贴图，因此看到的始终是同一张 Yoofi 卡；翻牌时叠加一层
 * 金色召唤光晕，光晕在翻转结束时回落到 0，保证静止状态与 Figma 设计稿完全一致。
 *
 * @param effect 人物卡的揭示特效，与 iOS shader 的 8 种效果一一对应；
 *   传 `null` 表示每次随机挑一种，以后要固定效果直接传入指定枚举即可。
 */
@Composable
internal fun ChatCastFlipCard(
    modifier: Modifier = Modifier,
    effect: ChatCastRevealEffect? = null,
) {
    val revealEffect = remember(effect) { effect ?: ChatCastRevealEffect.random() }
    // 入场：卡片轻微放大淡入
    val enter = remember { Animatable(0f) }
    // 卡背自转：0f -> 360f，转完一圈回到正面
    val spin = remember { Animatable(0f) }
    // 揭示翻牌：0f 卡背，180f 人物卡，由用户点击触发
    val flip = remember { Animatable(0f) }
    // 人物卡揭示进度，喂给 shader 的 progress uniform
    val reveal = remember { Animatable(0f) }
    var spinFinished by remember { mutableStateOf(false) }
    var revealRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        enter.animateTo(
            targetValue = 1f,
            animationSpec = tween(CastEnterDurationMillis, easing = LinearOutSlowInEasing),
        )
    }
    LaunchedEffect(Unit) {
        delay(CastFlipDelayMillis)
        spin.animateTo(
            targetValue = CastSpinEndAngle,
            animationSpec = tween(CastSpinDurationMillis, easing = FastOutSlowInEasing),
        )
        spinFinished = true
    }
    LaunchedEffect(revealRequested) {
        if (!revealRequested) return@LaunchedEffect
        launch {
            flip.animateTo(
                targetValue = CastFlipEndAngle,
                animationSpec = tween(CastFlipDurationMillis, easing = FastOutSlowInEasing),
            )
        }
        // 翻过 90° 人物卡才正对观众，此时才开始跑揭示特效
        delay(CastFlipDurationMillis / 2L)
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(CastRevealDurationMillis, easing = LinearEasing),
        )
    }

    // 自转与翻牌共用同一根 Y 轴，累加成总角度
    val angle = spin.value + flip.value
    // 每转过 180° 朝向观众的就是另一面，把角度折回 [-90°, 90°] 才不会出现镜像贴图
    val faceRotation = angle - CastFlipEndAngle * (angle / CastFlipEndAngle).roundToInt()
    val showsCardBack = flip.value < CastFlipHalfAngle
    // 0 -> 1 -> 0 的正弦包络，翻到侧面（90°）时光晕最亮；自转阶段不出光晕
    val glowAlpha = sin(PI * flip.value / CastFlipEndAngle).toFloat()
    val enterProgress = enter.value
    val awaitingTap = spinFinished && !revealRequested

    Box(
        modifier = modifier
            .width(CastCardWidth)
            .height(CastCardHeight)
            .graphicsLayer {
                val scale = 0.88f + 0.12f * enterProgress
                scaleX = scale
                scaleY = scale
                alpha = enterProgress
            }
            .clickable(
                enabled = awaitingTap,
                // 卡面上不适合出现水波纹，去掉点击反馈
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                revealRequested = true
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = 1f + (CastGlowMaxScale - 1f) * glowAlpha
                    scaleX = scale
                    scaleY = scale
                    alpha = glowAlpha * 0.75f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(YoofiChatSummonGlowInner, YoofiChatSummonGlowOuter),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = faceRotation
                    cameraDistance = CastCameraDistanceFactor * density
                },
        ) {
            if (showsCardBack) {
                CastCardBack(showsHint = awaitingTap)
            } else {
                CastCardFront(effect = revealEffect, revealProgress = reveal.value)
            }
        }
    }
}

/**
 * 卡背：Yoofi 卡面贴图叠加循环扫光。卡背 PNG 自带圆角与透明边，无需再裁圆角。
 *
 * @param showsHint 自转结束
 */
@Composable
private fun BoxScope.CastCardBack(showsHint: Boolean) {
    Image(
        painter = painterResource(R.drawable.img_chat_cast_card),
        contentDescription = stringResource(R.string.chat_cast),
        modifier = Modifier.matchParentSize(),
        contentScale = ContentScale.FillBounds,
    )
    CastCardShine()
}

/** 卡面：人物卡贴图，按 [effect] 施加与 iOS 一致的揭示特效 */
@Composable
private fun BoxScope.CastCardFront(
    effect: ChatCastRevealEffect,
    revealProgress: Float,
) {
    Image(
        painter = painterResource(R.drawable.img_chat_cast_character),
        contentDescription = stringResource(R.string.cd_chat_cast_character),
        modifier = Modifier
            .matchParentSize()
            .clip(CastCardShape)
            .chatCastReveal(
                effect = effect,
                progress = revealProgress,
                accentColor = YoofiChatSummonGlowInner,
            ),
        contentScale = ContentScale.Crop,
    )
}

/**
 * 卡背高光扫过效果，等价于 CodePen "CSS shark animation"（xboxyan/KKLLZOE）的 `.shark-wrap::after`：
 * 一条 45° 的白色半透明光带从卡片左侧平移到右侧，被卡片圆角裁剪，2 秒无限循环。
 */
@Composable
private fun BoxScope.CastCardShine() {
    val transition = rememberInfiniteTransition(label = "castCardShine")
    // 对应 CSS 的 translateX(-100%) -> translateX(100%)，此处换算成光带沿扫光轴的行程
    val offsetRatio by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(CastShineDurationMillis, easing = LinearEasing),
        ),
        label = "castCardShineOffset",
    )
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(CastCardShape)
            .drawBehind {
                // 扫光轴水平向右并按 tan 值下倾，光带垂直于该轴，因而呈上端偏右的近竖直姿态
                val axis = Offset(size.width, size.width * CastShineTiltRatio)
                // 光带中心沿轴向平移，两端各完全移出卡面一次
                val bandCenter = Offset(size.width / 2f, size.height / 2f) + axis * offsetRatio
                // 渐变超出首尾色标后按 Clamp 取透明，卡面上不会出现硬边
                val brush = Brush.linearGradient(
                    0.5f - CastShineHalfWidth to Color.Transparent,
                    0.5f to Color.White.copy(alpha = CastShineAlpha),
                    0.5f + CastShineHalfWidth to Color.Transparent,
                    start = bandCenter - axis / 2f,
                    end = bandCenter + axis / 2f,
                )
                drawRect(brush)
            },
    )
}
