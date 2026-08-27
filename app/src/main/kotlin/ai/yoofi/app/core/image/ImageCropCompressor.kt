package ai.yoofi.app.core.image

import android.graphics.Bitmap
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * JPEG 体积压缩。上限走 [ImageProcessConfig.maxBytes]，与裁剪 SDK 解耦。
 */
object ImageCropCompressor {
    /**
     * 先降 JPEG 质量，仍超限则缩小边长，直到 [ImageProcessConfig.maxBytes] 或无法再缩。
     */
    fun compressToMaxBytes(bitmap: Bitmap, config: ImageProcessConfig): ByteArray? {
        val source = softwareArgb8888(bitmap) ?: return null
        var current = source
        var quality = 92
        var created = source !== bitmap
        try {
            while (true) {
                val bytes = encodeJpeg(current, quality)
                if (bytes.isEmpty()) {
                    return null
                }
                if (bytes.size <= config.maxBytes) {
                    return bytes
                }
                if (quality > config.minJpegQuality) {
                    quality = (quality - 8).coerceAtLeast(config.minJpegQuality)
                    continue
                }
                if (current.width <= MinEdgePx || current.height <= MinEdgePx) {
                    return bytes
                }
                val nextWidth = (current.width * Downscale).toInt().coerceAtLeast(MinEdgePx)
                val nextHeight = (current.height * Downscale).toInt().coerceAtLeast(MinEdgePx)
                if (nextWidth >= current.width && nextHeight >= current.height) {
                    return bytes
                }
                val next = Bitmap.createScaledBitmap(current, nextWidth, nextHeight, true)
                if (created) {
                    current.recycle()
                }
                current = next
                created = true
                quality = 92
            }
        } finally {
            if (created && current != bitmap && !current.isRecycled) {
                current.recycle()
            }
        }
    }

    /** HARDWARE bitmap 无法 JPEG 编码，需先拷到软件位图。 */
    private fun softwareArgb8888(bitmap: Bitmap): Bitmap? {
        if (bitmap.isRecycled) return null
        val hardware = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            bitmap.config == Bitmap.Config.HARDWARE
        if (!hardware) return bitmap
        return bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    fun writeBytes(bytes: ByteArray, dest: File): Boolean {
        if (bytes.isEmpty()) return false
        return runCatching {
            dest.parentFile?.mkdirs()
            FileOutputStream(dest).use { it.write(bytes) }
            dest.length() > 0L
        }.getOrDefault(false)
    }

    private fun encodeJpeg(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    private const val MinEdgePx = 256
    private const val Downscale = 0.85f
}
