package ai.yoofi.app.core.image

/**
 * 裁剪比例与体积上限，供头像及其他选图复用。
 * [aspectWidth]:[aspectHeight] 为输出画幅；[maxBytes] 为编码后上限。
 */
data class ImageProcessConfig(
    val aspectWidth: Int = 1,
    val aspectHeight: Int = 1,
    val maxBytes: Long = 5L * 1024 * 1024,
    val maxDecodeEdgePx: Int = 2048,
    val minJpegQuality: Int = 45,
) {
    init {
        require(aspectWidth > 0 && aspectHeight > 0) { "宽高比必须为正" }
        require(maxBytes > 0L) { "体积上限必须为正" }
        require(maxDecodeEdgePx > 0) { "解码边长必须为正" }
        require(minJpegQuality in 1..100) { "JPEG 质量 1–100" }
    }

    val aspectRatio: Float
        get() = aspectWidth.toFloat() / aspectHeight.toFloat()

    companion object {
        /** 头像：1:1，确认后不超过 5MB。 */
        val Avatar = ImageProcessConfig(
            aspectWidth = 1,
            aspectHeight = 1,
            maxBytes = 5L * 1024 * 1024,
        )
    }
}
