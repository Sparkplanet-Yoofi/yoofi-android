package ai.yoofi.app.core.image.crop

import ai.yoofi.app.core.image.ImageProcessConfig
import android.graphics.Bitmap

/**
 * 裁剪会话。业务只拿这个做旋转/导出，不碰第三方 View。
 */
interface ImageCropSession {
    fun rotateBy(degrees: Int)

    fun cropBitmap(): Bitmap?
}

/** 裁剪视口参数，由 [ImageProcessConfig] 派生。 */
data class ImageCropSpec(
    val aspectWidth: Int,
    val aspectHeight: Int,
    val allowRotate: Boolean = true,
) {
    companion object {
        fun from(config: ImageProcessConfig): ImageCropSpec = ImageCropSpec(
            aspectWidth = config.aspectWidth,
            aspectHeight = config.aspectHeight,
        )
    }
}
