package ai.yoofi.app.core.image.crop

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 裁剪视口渲染器契约。唯一允许换成其它 SDK 的注入点。
 * UI 只依赖本接口，禁止 import 第三方包。
 */
interface ImageCropHostRenderer {
    @Composable
    fun Render(
        sourcePath: String,
        spec: ImageCropSpec,
        onSessionReady: (ImageCropSession) -> Unit,
        onLoadFailed: () -> Unit,
        modifier: Modifier,
    )
}

@Composable
fun ImageCropHost(
    sourcePath: String,
    spec: ImageCropSpec,
    renderer: ImageCropHostRenderer,
    onSessionReady: (ImageCropSession) -> Unit,
    onLoadFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    renderer.Render(
        sourcePath = sourcePath,
        spec = spec,
        onSessionReady = onSessionReady,
        onLoadFailed = onLoadFailed,
        modifier = modifier,
    )
}
