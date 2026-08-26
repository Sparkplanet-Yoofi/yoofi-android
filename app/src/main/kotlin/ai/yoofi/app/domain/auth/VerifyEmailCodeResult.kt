package ai.yoofi.app.domain.auth

/**
 * 邮箱验证码校验结果。
 * [hasUserProfile] 对应服务端「是否已填写用户资料」。
 */
sealed interface VerifyEmailCodeResult {
    data object InvalidCode : VerifyEmailCodeResult

    data class Success(
        val hasUserProfile: Boolean,
    ) : VerifyEmailCodeResult
}
