package ai.yoofi.app.domain.auth

/** 可关联的第三方登录方式。 */
enum class LinkedAccountProvider { Google, Apple }

/**
 * 一条关联账号。未绑定时 [maskedIdentity] 可为空，UI 显示 Not linked。
 */
data class LinkedAccount(
    val provider: LinkedAccountProvider,
    val linked: Boolean,
    val maskedIdentity: String,
)
