package ai.yoofi.app.domain.auth

/** 校验邮箱验证码，并带回是否已完善资料。 */
class VerifyEmailCodeUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        code: String,
    ): VerifyEmailCodeResult {
        val trimmedEmail = email.trim()
        val trimmedCode = code.trim()
        if (trimmedEmail.isEmpty() || trimmedCode.length != OtpLength) {
            return VerifyEmailCodeResult.InvalidCode
        }
        return authRepository.verifyEmailCode(
            email = trimmedEmail,
            code = trimmedCode,
        )
    }
}

private const val OtpLength = 6

/** 与 Figma 验证码错误态对齐的演示码。 */
const val DemoInvalidEmailOtp = "121111"
