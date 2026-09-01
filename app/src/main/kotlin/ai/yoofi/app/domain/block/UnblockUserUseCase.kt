package ai.yoofi.app.domain.block

import ai.yoofi.shared.common.Outcome

/**
 * 解除拉黑。接口未定时占位成功，接上后只改这里和 Repository。
 */
class UnblockUserUseCase {
    @Suppress("UNUSED_PARAMETER")
    suspend operator fun invoke(userId: String): Outcome<Unit> {
        return Outcome.Ok(Unit)
    }
}
