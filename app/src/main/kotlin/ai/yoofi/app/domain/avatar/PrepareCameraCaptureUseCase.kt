package ai.yoofi.app.domain.avatar

/** 生成拍照输出 URI；取消时丢掉临时文件。 */
class PrepareCameraCaptureUseCase(
    private val store: AvatarLocalStore,
) {
    operator fun invoke(): String = store.createCaptureUri()

    fun discard(uri: String) {
        if (uri.isNotBlank()) {
            store.discardCapture(uri)
        }
    }
}
