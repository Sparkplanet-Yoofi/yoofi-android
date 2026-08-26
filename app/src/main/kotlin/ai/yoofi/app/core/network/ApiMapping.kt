package ai.yoofi.app.core.network

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome
import kotlinx.serialization.json.Json

/**
 * 把信封转成 [Outcome]。纯 Kotlin，KMP common 可原样搬走。
 */
fun <T> ApiResponse<T>.toOutcome(): Outcome<T> {
    val payload = data
    return if (code == ApiSuccessCode && payload != null) {
        Outcome.Ok(payload)
    } else {
        Outcome.Err(AppError.Api(code = code, message = message))
    }
}

/**
 * HTTP 错误体（可能无 data）解析成 [AppError]
 */
fun parseApiErrorBody(raw: String, json: Json): AppError {
    if (raw.isBlank()) return AppError.Unknown
    return try {
        val envelope = json.decodeFromString(ApiErrorEnvelope.serializer(), raw)
        AppError.Api(code = envelope.code, message = envelope.message)
    } catch (_: Exception) {
        AppError.Unknown
    }
}
