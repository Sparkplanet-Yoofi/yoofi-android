package ai.yoofi.app.data.auth

import ai.yoofi.app.domain.auth.AuthSession
import ai.yoofi.app.domain.auth.User
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val code: String,
    val platform: String,
    val deviceId: String? = null,
    val deviceModel: String? = null,
)

@Serializable
data class LoginDataDto(
    val accessToken: String,
    val accessExpiresIn: Int,
    val refreshToken: String,
    val refreshExpiresIn: Int,
    val user: UserSummaryDto,
    val isNewUser: Boolean = false,
    val profileCompleted: Boolean = false,
)

@Serializable
data class UserSummaryDto(
    val userId: Long,
    val nickname: String = "",
    val avatarUrl: String = "",
)

internal fun LoginDataDto.toSession(): AuthSession = AuthSession(
    user = user.toDomain(),
    accessToken = accessToken,
    refreshToken = refreshToken,
    accessExpiresIn = accessExpiresIn,
    refreshExpiresIn = refreshExpiresIn,
    isNewUser = isNewUser,
    profileCompleted = profileCompleted,
)

internal fun UserSummaryDto.toDomain(): User = User(
    userId = userId,
    nickname = nickname,
    avatarUrl = avatarUrl,
)
