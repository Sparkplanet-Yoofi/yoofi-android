package ai.yoofi.app.ui.surface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize

/**
 * 当前页离屏层。必须包住「记录页」和「读层的底栏」；只包记录页的话，
 * 底栏 [LocalContentBackdrop] 恒为 null，毛玻璃整段被跳过。
 */
val LocalContentBackdrop = staticCompositionLocalOf<GraphicsLayer?> { null }

@Composable
fun rememberContentBackdropLayer(): GraphicsLayer = rememberGraphicsLayer()

@Composable
fun ContentBackdropProvider(
    layer: GraphicsLayer,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalContentBackdrop provides layer, content = content)
}

/** 只记录 Tab 页，不要把底栏画进去，否则糊的是自己。 */
@Composable
fun ContentBackdropRecorder(
    layer: GraphicsLayer,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                layer.record(
                    size = IntSize(
                        size.width.toInt().coerceAtLeast(1),
                        size.height.toInt().coerceAtLeast(1),
                    ),
                ) {
                    this@drawWithContent.drawContent()
                }
                drawContent()
            },
    ) {
        content()
    }
}

/**
 * CSS `backdrop-filter: blur` + 半透明 fill。
 *
 * 不能改 [GraphicsLayer.renderEffect]：那是共享页层，设完立刻清空，GPU 合成时
 * 效果已经没了。做法和详情卡一样——先画出裁切后的页，再 [Modifier.blur] 这一层。
 */
@Composable
fun BackdropFrostBox(
    modifier: Modifier = Modifier,
    tint: Color,
    blurRadius: Dp,
    shape: Shape,
    content: @Composable BoxScope.() -> Unit,
) {
    val layer = LocalContentBackdrop.current
    var originInRoot by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier = modifier
            .onGloballyPositioned { originInRoot = it.positionInRoot() }
            .clip(shape),
    ) {
        if (layer != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clipToBounds()
                    .blur(blurRadius)
                    .drawBehind {
                        translate(-originInRoot.x, -originInRoot.y) {
                            drawLayer(layer)
                        }
                    },
            )
        }
        Box(Modifier.matchParentSize().background(tint))
        content()
    }
}
