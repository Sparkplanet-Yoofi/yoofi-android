package ai.yoofi.app.ui.world

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.PaytoneOne
import ai.yoofi.app.ui.theme.YoofiGameSurface
import ai.yoofi.app.ui.theme.YoofiTitleGradientEnd
import ai.yoofi.app.ui.theme.YoofiTitleGradientStart
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Figma 节点 982:14768 的画板高度 */
private val HeaderHeight = 60.dp

/** 左右边距 20；图标组 right = 20 */
private val HeaderHorizontalPadding = 20.dp

/** 标题组 top = 53.5 − 47 */
private val TitleGroupTop = 6.5.dp

/** Figma 文本框 94×45，Paytone One 32 */
private val TitleWidth = 94.dp
private val TitleHeight = 45.dp
private val TitleFontSize = 32.dp
private val TitleGroupWidth = 103.5.dp

/** 信号装饰：框 7.5×8.4 在 (96, 12.8)，SVG 向外扩 1.5 */
private val SparkleOffsetX = 94.5.dp
private val SparkleOffsetY = 11.3.dp
private val SparkleWidth = 10.5.dp
private val SparkleHeight = 11.4.dp

/** 图标 24，组 top = 65 − 47 = 18，间距 12 */
private val ActionIconSize = 24.dp
private val ActionRowTop = 18.dp
private val ActionIconGap = 12.dp

/**
 * Game 首页顶栏，像素对齐 Figma `982:14768`（390×60）。
 *
 * 隐藏层 Settings / megaphone 按设计稿不渲染。
 */
@Composable
fun GameTopBar(
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .background(YoofiGameSurface),
    ) {
        GameTitleMark(
            modifier = Modifier.padding(
                start = HeaderHorizontalPadding,
                top = TitleGroupTop,
            ),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = ActionRowTop, end = HeaderHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(ActionIconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderIconButton(
                drawableRes = R.drawable.ic_game_search,
                contentDescription = stringResource(R.string.cd_game_search),
                onClick = onSearchClick,
            )
            HeaderIconButton(
                drawableRes = R.drawable.ic_game_bell,
                contentDescription = stringResource(R.string.cd_game_notifications),
                onClick = onNotificationClick,
            )
        }
    }
}

@Composable
private fun GameTitleMark(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val titleBrush = remember(density) {
        with(density) {
            cssAngleLinearGradient(
                angleDeg = 162.45498874090407,
                colorStops = arrayOf(
                    0f to YoofiTitleGradientStart,
                    0.1235f to YoofiTitleGradientStart,
                    1f to YoofiTitleGradientEnd,
                ),
                widthPx = TitleWidth.toPx(),
                heightPx = TitleHeight.toPx(),
            )
        }
    }
    Box(modifier = modifier.size(width = TitleGroupWidth, height = TitleHeight)) {
        Text(
            text = stringResource(R.string.game_home_title),
            modifier = Modifier
                .width(TitleWidth)
                .height(TitleHeight),
            style = TextStyle(
                fontFamily = PaytoneOne,
                fontWeight = FontWeight.Normal,
                // dp→sp 抵消系统字体缩放，保持与 Figma 32/45px 一致
                fontSize = with(density) { TitleFontSize.toSp() },
                lineHeight = with(density) { TitleHeight.toSp() },
                brush = titleBrush,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false,
        )
        Image(
            painter = painterResource(R.drawable.ic_game_title_sparkle),
            contentDescription = null,
            modifier = Modifier
                .offset(x = SparkleOffsetX, y = SparkleOffsetY)
                .size(width = SparkleWidth, height = SparkleHeight),
            contentScale = ContentScale.FillBounds,
        )
    }
}

@Composable
private fun HeaderIconButton(
    drawableRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    // 视觉尺寸锁死 24dp，不用 IconButton，避免 Material 48dp 最小点击区撑开布局
    Image(
        painter = painterResource(drawableRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(ActionIconSize)
            .clickable(role = Role.Button, onClick = onClick),
        contentScale = ContentScale.Fit,
    )
}

/**
 * 按 CSS / Figma `linear-gradient(angle, …)` 构造 Brush。
 * 0° 朝上、顺时针；色标沿穿过矩形中心、覆盖四角的渐变线分布。
 */
private fun cssAngleLinearGradient(
    angleDeg: Double,
    colorStops: Array<Pair<Float, Color>>,
    widthPx: Float,
    heightPx: Float,
): Brush {
    val theta = Math.toRadians(angleDeg)
    val dx = sin(theta).toFloat()
    val dy = (-cos(theta)).toFloat()
    val length = abs(widthPx * dx) + abs(heightPx * dy)
    val half = length / 2f
    val center = Offset(widthPx / 2f, heightPx / 2f)
    return Brush.linearGradient(
        colorStops = colorStops,
        start = Offset(center.x - dx * half, center.y - dy * half),
        end = Offset(center.x + dx * half, center.y + dy * half),
    )
}

@Preview(widthDp = 390, heightDp = 60, showBackground = true, backgroundColor = 0xFF2B2B2B)
@Composable
private fun GameTopBarPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        GameTopBar(onSearchClick = {}, onNotificationClick = {})
    }
}
