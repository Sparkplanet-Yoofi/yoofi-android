package ai.yoofi.app.core.network

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

/**
 * Ktor 适配器。这是全项目唯一允许 catch HTTP 异常的地方。
 *
 * 异常归一化口径与此前的 Retrofit 实现保持一致：
 * - [ResponseException]（非 2xx，由 `expectSuccess = true` 抛出）→ 解错误信封成 [AppError.Api]
 * - [IOException]（含 Ktor 超时）→ [AppError.Network]
 * - [CancellationException] 必须原样抛出，否则协程取消会被吞成失败态
 *
 * 拆 KMP 时本类可直接进 commonMain：依赖的 [IOException] 取自 kotlinx-io，
 * 在 JVM 上就是 `java.io.IOException` 的 typealias。
 */
@Singleton
class KtorApiCaller @Inject constructor(
    private val json: Json,
) : ApiCaller {
    override suspend fun <T> fetch(
        block: suspend () -> ApiResponse<T>,
    ): Outcome<T> = withContext(Dispatchers.IO) {
        try {
            block().toOutcome()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            Outcome.Err(parseApiErrorBody(e.response.bodyAsText(), json))
        } catch (_: IOException) {
            Outcome.Err(AppError.Network)
        } catch (_: Exception) {
            Outcome.Err(AppError.Unknown)
        }
    }
}
