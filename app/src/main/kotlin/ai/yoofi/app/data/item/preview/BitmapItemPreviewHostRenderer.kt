package ai.yoofi.app.data.item.preview

import ai.yoofi.app.R
import ai.yoofi.app.core.item.preview.ItemPreviewContent
import ai.yoofi.app.core.item.preview.ItemPreviewHostRenderer
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 默认 2D 预览：静态卡面，不带 3D 变换。
 * 当前 Hilt 绑的是 [SceneViewItemPreviewHostRenderer]，这里保留作降级与 Compose Preview 用。
 */
@Singleton
class BitmapItemPreviewHostRenderer @Inject constructor() : ItemPreviewHostRenderer {

    @Composable
    override fun Render(
        content: ItemPreviewContent,
        modifier: Modifier,
    ) {
        ItemPreviewCardFace(
            content = content,
            modifier = modifier.clip(ItemPreviewCardShape),
        )
    }
}

@DrawableRes
internal fun itemArtRes(imageKey: String): Int = when (imageKey) {
    "lollipops" -> R.drawable.img_item_lollipops
    "key" -> R.drawable.img_item_key
    "goblet" -> R.drawable.img_item_goblet
    else -> R.drawable.img_item_knife
}
