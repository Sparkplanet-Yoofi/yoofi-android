package ai.yoofi.app.core.image

import android.graphics.Bitmap
import java.io.File

/**
 * 把裁剪结果写成不超过 [ImageProcessConfig.maxBytes] 的 JPEG。
 * 业务确认裁剪后调这个，不要在 UI 里手写 compress 循环。
 */
object ImageCropExporter {
    fun writeJpeg(bitmap: Bitmap, dest: File, config: ImageProcessConfig): Boolean {
        val bytes = ImageCropCompressor.compressToMaxBytes(bitmap, config) ?: return false
        return ImageCropCompressor.writeBytes(bytes, dest)
    }
}
