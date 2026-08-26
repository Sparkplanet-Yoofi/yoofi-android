package ai.yoofi.app.domain.auth

/**
 * 邮箱验证码登录结果。
 *
 * 导航按 [Success.isNewUser]：true 进资料填写，false 进首页。
 * [Success.profileCompleted] 写入会话，供后续与 isNewUser 区分使用。
 */
sealed interface VerifyEmailCodeResult {
    data object InvalidCode : VerifyEmailCodeResult

    data class Success(
        val isNewUser: Boolean,
        val profileCompleted: Boolean,
    ) : VerifyEmailCodeResult
}
