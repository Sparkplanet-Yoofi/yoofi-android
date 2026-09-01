package ai.yoofi.app.domain.profile

import ai.yoofi.shared.common.AppError
import ai.yoofi.shared.common.Outcome

/**
 * 编辑已有资料。接口未定时先直接成功，接上后只改这里和 Repository，不改 Screen。
 */
class UpdateProfileUseCase {
    suspend operator fun invoke(draft: ProfileDraft): Outcome<Unit> {
        if (draft.displayName.isBlank()) {
            return Outcome.Err(AppError.Unknown)
        }
        return Outcome.Ok(Unit)
    }
}
