package ai.yoofi.app.core.network

import ai.yoofi.app.core.common.Outcome

/**
 * 统一网络调用入口。接口本身无 Retrofit，便于日后 Ktor 另写实现。
 *
 * [block] 里才允许碰具体 HTTP 客户端；异常必须在实现类里吞掉并变成 [Outcome]。
 */
interface ApiCaller {
    suspend fun <T> fetch(block: suspend () -> ApiResponse<T>): Outcome<T>
}
