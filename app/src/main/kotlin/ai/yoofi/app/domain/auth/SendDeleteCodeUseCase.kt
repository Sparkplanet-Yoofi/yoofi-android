package ai.yoofi.app.domain.auth

import ai.yoofi.app.core.common.Outcome

/**
 * 无密账号注销发验证码。接口未定时占位成功，接上后只改这里。
 */
class SendDeleteCodeUseCase {
    suspend operator fun invoke(): Outcome<Unit> = Outcome.Ok(Unit)
}
