package ai.yoofi.app.data.auth

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome
import ai.yoofi.app.core.network.ApiInvalidOrExpiredCode
import ai.yoofi.app.domain.auth.DemoInvalidEmailOtp
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

private const val MockLoginDelayMs = 200L

/**
 * 登录接口的 Demo 实现，不打网。
 *
 * 逻辑原样承接自 `RetrofitAuthRemoteDataSource.mockLoginSuccess`：
 * [DemoInvalidEmailOtp] 返回验证码错误，其余六位码一律成功且按新用户走资料填写。
 *
 * 是否启用由 `DemoFeature.Auth` 决定，不再靠源码里的手改开关。
 */
@Singleton
class DemoAuthRemoteDataSource @Inject constructor() : AuthRemoteDataSource {
    override suspend fun login(
        email: String,
        code: String,
    ): Outcome<LoginDataDto> {
        delay(MockLoginDelayMs)
        if (code == DemoInvalidEmailOtp) {
            return Outcome.Err(
                AppError.Api(code = ApiInvalidOrExpiredCode, message = "mock invalid"),
            )
        }
        return Outcome.Ok(
            LoginDataDto(
                accessToken = "mock-access-token",
                accessExpiresIn = 900,
                refreshToken = "mock-refresh-token",
                refreshExpiresIn = 604800,
                user = UserSummaryDto(userId = 1L, nickname = "mock"),
                isNewUser = true,
                profileCompleted = false,
            ),
        )
    }
}
