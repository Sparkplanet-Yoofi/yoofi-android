package ai.yoofi.app.domain.auth

/**
 * 当前登录用户摘要，对应接口 UserSummary。
 */
data class User(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String,
)
