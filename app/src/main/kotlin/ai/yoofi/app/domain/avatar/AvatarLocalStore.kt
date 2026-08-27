package ai.yoofi.app.domain.avatar

import ai.yoofi.app.core.common.Outcome

/**
 * 头像落本地私有目录。URI 用字符串，避免 domain 依赖 android.net.Uri。
 */
interface AvatarLocalStore {
    /**
     * 创建 FileProvider 拍照输出 URI。
     */
    fun createCaptureUri(): String

    /**
     * 把相册/拍照 URI 拷贝压缩到 filesDir，返回本地绝对路径。
     */
    suspend fun persistFromUri(sourceUri: String): Outcome<String>

    /**
     * 相册/拍照 URI 原样拷到缓存，供裁剪页解码。
     */
    suspend fun stageFromUri(sourceUri: String): Outcome<String>

    /**
     * 把已编码的 JPEG 拷到 filesDir 头像文件，不再二次压缩。
     */
    suspend fun persistEncodedFile(sourcePath: String): Outcome<String>

    /**
     * 用户取消拍照时删掉临时文件。
     */
    fun discardCapture(uri: String)

    /**
     * 丢掉裁剪暂存与裁剪结果，取消或确认后都要调。
     */
    fun discardStagedCrop()
}
