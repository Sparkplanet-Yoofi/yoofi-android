package ai.yoofi.app.domain.report

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome

/**
 * 提交内容举报。接口未定时先本地校验后成功，接上后只改这里和 Repository。
 */
class SubmitReportUseCase {
    suspend operator fun invoke(draft: ReportDraft): Outcome<Unit> {
        if (draft.details.isBlank()) return Outcome.Err(AppError.Unknown)
        return Outcome.Ok(Unit)
    }
}
