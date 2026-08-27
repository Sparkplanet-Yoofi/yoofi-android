package ai.yoofi.app.domain.avatar

import ai.yoofi.app.core.common.Outcome

/**
 * 把相册/拍照临时 URI 立刻拷到缓存，供裁剪页使用，避免授权过期。
 */
class StageAvatarCropUseCase(
    private val store: AvatarLocalStore,
) {
    suspend operator fun invoke(sourceUri: String): String? {
        val trimmed = sourceUri.trim()
        if (trimmed.isEmpty()) return null
        return when (val outcome = store.stageFromUri(trimmed)) {
            is Outcome.Ok -> outcome.value
            is Outcome.Err -> null
        }
    }

    fun discard() {
        store.discardStagedCrop()
    }
}
