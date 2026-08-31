package ai.yoofi.app.domain.auth

import ai.yoofi.app.core.common.Outcome

/**
 * 解绑第三方登录。接口未定时占位成功，接上后只改这里和 Repository。
 */
class UnlinkAccountUseCase {
    @Suppress("UNUSED_PARAMETER")
    suspend operator fun invoke(provider: LinkedAccountProvider): Outcome<Unit> {
        return Outcome.Ok(Unit)
    }
}
