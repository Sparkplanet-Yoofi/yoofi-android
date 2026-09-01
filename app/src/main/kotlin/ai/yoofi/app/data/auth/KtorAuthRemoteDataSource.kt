package ai.yoofi.app.data.auth

import ai.yoofi.shared.common.Outcome
import ai.yoofi.shared.network.ApiCaller
import ai.yoofi.shared.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import javax.inject.Singleton

internal const val AuthPlatformAndroid = "android"

/** 登录(注册登录合一)。相对路径，Base URL 由 `defaultRequest` 注入。 */
private const val LoginPath = "customer/auth/login"

/**
 * 登录接口的 Ktor 实现。HTTP 异常交给 [ApiCaller]，本类只组请求。
 *
 * 承接原 `RetrofitAuthRemoteDataSource` 的请求体组装逻辑：设备号与机型为空时传 null，
 * 由服务端按缺省处理。
 *
 * Demo 分支在 [DemoAuthRemoteDataSource]，由 `DemoFeature.Auth` 选择，本类不感知 mock。
 */
@Singleton
class KtorAuthRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient,
    private val apiCaller: ApiCaller,
    private val deviceIdentity: DeviceIdentity,
) : AuthRemoteDataSource {
    override suspend fun login(
        email: String,
        code: String,
    ): Outcome<LoginDataDto> = apiCaller.fetch {
        httpClient.post(LoginPath) {
            setBody(
                LoginRequestDto(
                    email = email,
                    code = code,
                    platform = AuthPlatformAndroid,
                    deviceId = deviceIdentity.deviceId().ifBlank { null },
                    deviceModel = deviceIdentity.deviceModel().ifBlank { null },
                ),
            )
        }.body<ApiResponse<LoginDataDto>>()
    }
}
