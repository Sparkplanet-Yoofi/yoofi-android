package ai.yoofi.app.data.auth

import ai.yoofi.app.domain.auth.AuthRepository
import ai.yoofi.app.domain.auth.DemoInvalidEmailOtp
import ai.yoofi.app.domain.auth.VerifyEmailCodeResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

/**
 * 验证码接口本地 mock。
 * 当前固定 [VerifyEmailCodeResult.Success.hasUserProfile] = false，
 * 以便走资料填写；接真接口时替换为本实现。
 */
@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {
    override suspend fun verifyEmailCode(
        email: String,
        code: String,
    ): VerifyEmailCodeResult {
        delay(MockNetworkDelayMs)
        if (code == DemoInvalidEmailOtp) {
            return VerifyEmailCodeResult.InvalidCode
        }
        return VerifyEmailCodeResult.Success(hasUserProfile = MockHasUserProfile)
    }
}

private const val MockNetworkDelayMs = 300L

/** TODO mock：用户尚未填写资料。改为 true 可验证直达 Home。 */
private const val MockHasUserProfile = false
