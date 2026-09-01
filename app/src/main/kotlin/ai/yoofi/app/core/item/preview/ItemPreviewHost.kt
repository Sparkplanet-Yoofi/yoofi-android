package ai.yoofi.app.core.item.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 道具大图 / 3D 预览契约。当前默认 2D 卡面，日后换 3D SDK 只改适配实现。
 * UI 只依赖本接口，禁止 import 第三方包。
 */
interface ItemPreviewHostRenderer {
    @Composable
    fun Render(
        imageKey: String,
        modifier: Modifier,
    )
}

@Composable
fun ItemPreviewHost(
    imageKey: String,
    renderer: ItemPreviewHostRenderer,
    modifier: Modifier = Modifier,
) {
    renderer.Render(imageKey = imageKey, modifier = modifier)
}
