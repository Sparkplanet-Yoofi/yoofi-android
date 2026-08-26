package ai.yoofi.app.core.common

/**
 * Domain 能懂的失败原因，不含 OkHttp / Retrofit / android.
 */
sealed interface AppError {
    data class Api(val code: Int, val message: String) : AppError
    data object Network : AppError
    data object Unknown : AppError
}
