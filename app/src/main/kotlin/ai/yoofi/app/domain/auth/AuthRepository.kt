package ai.yoofi.app.domain.auth

/**
 * 认证仓储契约，Data 层实现；Domain 不感知 Retrofit / RemoteDataSource。
 */
interface AuthRepository {
    suspend fun verifyEmailCode(
        email: String,
        code: String,
    ): VerifyEmailCodeResult
}
