package ai.yoofi.app.domain.avatar

import ai.yoofi.app.core.common.Outcome

/** 把系统回传的 content URI 落到应用私有目录，避免授权过期后头像空白。 */
class PersistPickedAvatarUseCase(
    private val store: AvatarLocalStore,
) {
    suspend operator fun invoke(sourceUri: String): String? {
        val trimmed = sourceUri.trim()
        if (trimmed.isEmpty()) return null
        return when (val outcome = store.persistFromUri(trimmed)) {
            is Outcome.Ok -> outcome.value
            is Outcome.Err -> null
        }
    }
}
