package ai.yoofi.app.data.auth

import ai.yoofi.shared.common.Outcome

/**
 * 认证远程数据源。纯 Kotlin 契约，禁止出现 Retrofit / OkHttp / android.*
 * 日后 Ktor 实现同一接口即可，[ai.yoofi.app.domain.auth.AuthRepository] 不用改
 */
interface AuthRemoteDataSource {
    suspend fun login(email: String, code: String): Outcome<LoginDataDto>
}
