package ai.yoofi.app.data.auth

import ai.yoofi.shared.common.AppError
import ai.yoofi.shared.common.Outcome
import ai.yoofi.app.domain.auth.AuthRepository
import ai.yoofi.app.domain.auth.UserSessionStore
import ai.yoofi.app.domain.auth.VerifyEmailCodeResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证仓储：只编排远程结果与会话，不 catch HTTP 异常。
 */
@Singleton
class RemoteAuthRepository @Inject constructor(
    private val remote: AuthRemoteDataSource,
    private val userSessionStore: UserSessionStore,
) : AuthRepository {
    override suspend fun verifyEmailCode(
        email: String,
        code: String,
    ): VerifyEmailCodeResult {
        return when (val outcome = remote.login(email, code)) {
            is Outcome.Ok -> {
                val session = outcome.value.toSession()
                userSessionStore.save(session)
                VerifyEmailCodeResult.Success(
                    isNewUser = session.isNewUser,
                    profileCompleted = session.profileCompleted,
                )
            }
            is Outcome.Err -> outcome.error.toVerifyResult()
        }
    }
}

/** 4010/4013/4014 与网络失败目前都走验证码错误态，避免改登录 UI。 */
private fun AppError.toVerifyResult(): VerifyEmailCodeResult =
    VerifyEmailCodeResult.InvalidCode
