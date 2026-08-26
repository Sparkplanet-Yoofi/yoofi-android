package ai.yoofi.app.core.network

import kotlinx.serialization.Serializable

/** 业务成功码。 */
const val ApiSuccessCode = 0

/** 邮箱格式错误。 */
const val ApiInvalidEmail = 4010

/** 验证码错误或过期。 */
const val ApiInvalidOrExpiredCode = 4013

/** 账号已封禁。 */
const val ApiAccountBlocked = 4014

@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String = "",
    val data: T? = null,
    val timestamp: Long = 0L,
)

/** 错误体可能没有 data，单独解以免泛型失败。 */
@Serializable
data class ApiErrorEnvelope(
    val code: Int = -1,
    val message: String = "",
)
