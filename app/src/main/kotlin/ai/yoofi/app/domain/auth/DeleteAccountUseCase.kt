package ai.yoofi.app.domain.auth

import ai.yoofi.shared.common.AppError
import ai.yoofi.shared.common.Outcome

/**
 * 注销账号。接口未定时先本地校验再清会话，接上后只改这里和 Repository。
 */
class DeleteAccountUseCase(
    private val userSessionStore: UserSessionStore,
) {
    suspend operator fun invoke(proof: DeleteAccountProof): Outcome<Unit> {
        if (!proof.isValid()) return Outcome.Err(AppError.Unknown)
        userSessionStore.clear()
        return Outcome.Ok(Unit)
    }
}
