package ai.yoofi.app.domain.feedback

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome

/**
 * 提交设置反馈。接口未定时先本地校验后成功，接上后只改这里和 Repository。
 */
class SubmitFeedbackUseCase {
    suspend operator fun invoke(draft: FeedbackDraft): Outcome<Unit> {
        if (draft.details.isBlank()) return Outcome.Err(AppError.Unknown)
        return Outcome.Ok(Unit)
    }
}
