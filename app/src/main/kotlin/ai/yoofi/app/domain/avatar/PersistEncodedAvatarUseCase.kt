package ai.yoofi.app.domain.avatar

import ai.yoofi.app.core.common.Outcome

/** 把裁剪压缩后的 JPEG 拷到头像目录，不再二次解码。 */
class PersistEncodedAvatarUseCase(
    private val store: AvatarLocalStore,
) {
    suspend operator fun invoke(sourcePath: String): String? {
        val trimmed = sourcePath.trim()
        if (trimmed.isEmpty()) return null
        return when (val outcome = store.persistEncodedFile(trimmed)) {
            is Outcome.Ok -> outcome.value
            is Outcome.Err -> null
        }
    }
}
