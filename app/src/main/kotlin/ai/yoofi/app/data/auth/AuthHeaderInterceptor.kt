package ai.yoofi.app.data.auth

import ai.yoofi.app.domain.auth.UserSessionStore
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 已登录请求自动带 Bearer；登录白名单接口此时尚无 token，不会加头。
 */
@Singleton
class AuthHeaderInterceptor @Inject constructor(
    private val userSessionStore: UserSessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = userSessionStore.currentAccessToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
