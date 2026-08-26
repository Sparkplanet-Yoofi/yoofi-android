package ai.yoofi.app.core.common

/**
 * 跨层统一结果。放 common，业务不要再各写一份。
 * Data / Domain 用它；UI 再映射成 UiState，不要直接暴露 Retrofit 异常。
 */
sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Err(val error: AppError) : Outcome<Nothing>
}
