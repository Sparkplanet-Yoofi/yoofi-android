package ai.yoofi.app.data.auth

import ai.yoofi.app.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 认证 API
 */
interface AuthApi {
    /**
     * 登录(注册登录合一)
     */
    @POST("customer/auth/login")
    suspend fun login(
        @Body body: LoginRequestDto,
    ): ApiResponse<LoginDataDto>
}
