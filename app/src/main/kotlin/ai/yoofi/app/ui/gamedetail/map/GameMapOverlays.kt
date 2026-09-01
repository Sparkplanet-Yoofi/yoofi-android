package ai.yoofi.app.ui.gamedetail.map

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameMap
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAuthFocusStroke
import ai.yoofi.app.ui.theme.YoofiCameraTo
import ai.yoofi.app.ui.theme.YoofiDialogBg
import ai.yoofi.app.ui.theme.YoofiDialogButton
import ai.yoofi.app.ui.theme.YoofiDialogScrim
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val ChipShape = RoundedCornerShape(8.dp)
private val DialogShape = RoundedCornerShape(16.dp)
private val ActionShape = RoundedCornerShape(20.dp)
private val ChipFill = Color(0xCC292D45)
private val ChipBorder = Color(0xFF18214F)
private val ListFill = Color(0x8056566D)
private val SelectedFill = Color(0x80925CFF)
private val TrackColor = Color.White.copy(alpha = 0.10f)
private val ProgressColors = listOf(YoofiAuthFocusStroke, YoofiCameraTo)

/**
 * 右上角 Map 01 芯片 + 下拉列表，对齐 Figma `2453:27236` / `2453:27362`。
 */
@Composable
internal fun GameMapSwitcher(
    maps: List<GameMap>,
    currentMap: GameMap,
    listOpen: Boolean,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickLabel = stringResource(R.string.cd_map_pick)
    Column(
        modifier = modifier.width(86.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier
                .semantics { contentDescription = pickLabel }
                .clip(ChipShape)
                .background(ChipFill, ChipShape)
                .then(
                    if (listOpen) {
                        Modifier
                    } else {
                        Modifier.border(1.dp, ChipBorder, ChipShape)
                    },
                )
                .clickable(
                    role = Role.Button,
                    onClick = onToggle,
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_map_flag),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                alpha = 0.7f,
            )
            Text(
                text = currentMap.title,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        if (listOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ChipShape)
                    .background(ListFill, ChipShape)
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                maps.forEach { item ->
                    val selected = item.id == currentMap.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ChipShape)
                            .background(if (selected) SelectedFill else Color.Transparent)
                            .clickable(role = Role.Button) { onSelect(item.id) }
                            .padding(
                                horizontal = 12.dp,
                                vertical = if (selected) 4.dp else 6.dp,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White.copy(alpha = if (selected) 0.7f else 0.6f),
                            fontSize = 12.sp,
                            fontWeight = if (selected) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** Figma `2304:24255`：切换地图 Loading Dialog。 */
@Composable
internal fun GameMapLoadingDialog(
    progress: Int,
    onCancel: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(YoofiDialogScrim),
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(300.dp)
                .clip(DialogShape)
                .background(YoofiDialogBg)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = stringResource(R.string.map_loading_title),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(contentAlignment = Alignment.Center) {
                    MapLoadingRing(progress = progress.coerceIn(0, 100) / 100f)
                    Text(
                        text = stringResource(R.string.map_loading_percent, progress),
                        style = TextStyle(
                            brush = Brush.horizontalGradient(ProgressColors),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(46.dp)
                    .clip(ActionShape)
                    .background(YoofiDialogButton)
                    .clickableDismissingIme(onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.auth_cancel),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/**
 * 72dp 环：底轨白 10%，弧 #5257FF → #906AEF，端点圆帽。
 * 对齐 Figma `2304:24258`，进度可动画所以不用静态 SVG。
 */
@Composable
private fun MapLoadingRing(progress: Float) {
    val fraction = progress.coerceIn(0f, 1f)
    Canvas(modifier = Modifier.size(72.dp)) {
        val stroke = 6.dp.toPx()
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)
        drawArc(
            color = TrackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke),
        )
        rotate(degrees = -90f) {
            drawArc(
                brush = Brush.sweepGradient(ProgressColors),
                startAngle = 0f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        if (fraction > 0f) {
            val radius = (size.minDimension - stroke) / 2f
            val angle = Math.toRadians((-90f + 360f * fraction).toDouble())
            val cap = Offset(
                center.x + radius * cos(angle).toFloat(),
                center.y + radius * sin(angle).toFloat(),
            )
            drawCircle(
                brush = Brush.horizontalGradient(ProgressColors),
                radius = stroke / 2f,
                center = cap,
            )
        }
    }
}
