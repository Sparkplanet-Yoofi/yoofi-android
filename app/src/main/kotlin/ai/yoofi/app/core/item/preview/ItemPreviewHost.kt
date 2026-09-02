package ai.yoofi.app.core.item.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 预览卡面所需的道具信息。
 *
 * 刻意不直接用 domain 的 GameItem：core 能力层不依赖业务模型，
 * 由调用方做一次映射，日后换业务实体不波及渲染契约。
 */
data class ItemPreviewContent(
    val imageKey: String,
    val name: String,
    val description: String,
)

/**
 * 道具大图 / 3D 预览契约。当前默认 2D 卡面，日后换 3D SDK 只改适配实现。
 * UI 只依赖本接口，禁止 import 第三方包。
 *
 * 卡面的全部图层（立绘、装饰边框、信息区）都由实现方绘制，
 * 这样 3D 模式下整张卡才能作为一个整体转动，不会出现边框文字不跟转的穿帮。
 */
interface ItemPreviewHostRenderer {
    @Composable
    fun Render(
        content: ItemPreviewContent,
        modifier: Modifier,
    )
}

@Composable
fun ItemPreviewHost(
    content: ItemPreviewContent,
    renderer: ItemPreviewHostRenderer,
    modifier: Modifier = Modifier,
) {
    renderer.Render(content = content, modifier = modifier)
}
