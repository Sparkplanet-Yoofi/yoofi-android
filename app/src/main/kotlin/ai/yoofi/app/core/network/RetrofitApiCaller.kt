package ai.yoofi.app.core.network

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * Retrofit / OkHttp 适配器。这是当前唯一允许 `catch HttpException` 的地方。
 * 拆 KMP 时本类进 androidMain，用 Ktor 实现替换 [ApiCaller]，Repository 不动。
 */
@Singleton
class RetrofitApiCaller @Inject constructor(
    private val json: Json,
) : ApiCaller {
    override suspend fun <T> fetch(
        block: suspend () -> ApiResponse<T>,
    ): Outcome<T> = withContext(Dispatchers.IO) {
        try {
            block().toOutcome()
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val raw = e.response()?.errorBody()?.string().orEmpty()
            Outcome.Err(parseApiErrorBody(raw, json))
        } catch (_: IOException) {
            Outcome.Err(AppError.Network)
        } catch (_: Exception) {
            Outcome.Err(AppError.Unknown)
        }
    }
}
