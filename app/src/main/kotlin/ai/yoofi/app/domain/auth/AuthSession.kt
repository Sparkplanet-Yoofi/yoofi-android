package ai.yoofi.app.domain.auth

/**
 * 登录成功后的内存会话。
 * Token 与用户一起保存，供后续鉴权请求读取；进程被杀后丢失。
 */
data class AuthSession(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresIn: Int,
    val refreshExpiresIn: Int,
    val isNewUser: Boolean,
    val profileCompleted: Boolean,
)
