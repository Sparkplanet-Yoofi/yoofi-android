package ai.yoofi.app.data.item.preview

import ai.yoofi.app.R
import ai.yoofi.app.core.item.preview.ItemPreviewHostRenderer
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 默认 2D 预览：白蒙层里铺道具卡面。换 3D SDK 时新增适配并改 Hilt 绑定。
 */
@Singleton
class BitmapItemPreviewHostRenderer @Inject constructor() : ItemPreviewHostRenderer {

    @Composable
    override fun Render(
        imageKey: String,
        modifier: Modifier,
    ) {
        Image(
            painter = painterResource(itemArtRes(imageKey)),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
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
