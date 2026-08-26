package ai.yoofi.app.data.auth

import ai.yoofi.app.core.common.Outcome
import ai.yoofi.app.core.network.ApiCaller
import javax.inject.Inject
import javax.inject.Singleton

internal const val AuthPlatformAndroid = "android"

/**
 * 登录接口的 Retrofit 实现。HTTP 异常交给 [ApiCaller]，本类只组请求。
 */
@Singleton
class RetrofitAuthRemoteDataSource @Inject constructor(
    private val authApi: AuthApi,
    private val apiCaller: ApiCaller,
    private val deviceIdentity: DeviceIdentity,
) : AuthRemoteDataSource {
    override suspend fun login(
        email: String,
        code: String,
    ): Outcome<LoginDataDto> = apiCaller.fetch {
        authApi.login(
            LoginRequestDto(
                email = email,
                code = code,
                platform = AuthPlatformAndroid,
                deviceId = deviceIdentity.deviceId().ifBlank { null },
                deviceModel = deviceIdentity.deviceModel().ifBlank { null },
            ),
        )
    }
}
