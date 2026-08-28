package ai.yoofi.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// 超时分三档，对应三种不同的失败模式。

/**
 *  连接：TCP + TLS 握手耗时。握手最多几个 RTT，海外链路 RTT 按 1s 估也就 6s 上下，
 *  超过 10s 基本是链路不通而非慢——早失败早重试，比让用户对着转圈干等强。
 *  取 10s 与 OkHttp 默认一致。
 */
private const val ConnectTimeoutMillis = 10_000L

/**
 * Socket：**两个数据包之间**的最大间隔，不是整体耗时。服务端在算响应期间不发包也计入，
 * 普通 JSON 接口 15s 绰绰有余；再长只会拖慢弱网下的失败反馈。
  */
private const val SocketTimeoutMillis = 15_000L

/**
 * 请求：单次请求总时长上限
 *  注意这一档会掐断长连接：AI 流式 / SSE 与大文件分片上传都会在 30s 被杀，
 *  而 architecure.md §6.1 明确要求这类接口单独放大超时。
 *  正确做法是在 per-request 的 `timeout { }` 里覆盖
 *  （长连接用 HttpTimeoutConfig.INFINITE_TIMEOUT_MS），**不要改这里的全局值**。
  */
private const val RequestTimeoutMillis = 30_000L

/**
 * 构造全局 [HttpClient]。engine 由调用方传入，本函数不含任何平台专属代码，
 * 拆 KMP 时可整体进 commonMain，各平台只换 engine。
 *
 * @param baseUrl 必须以 `/` 结尾，业务侧传相对路径（如 `customer/auth/login`）
 * @param accessTokenProvider 每次请求实时读取，返回 null 或空串则不加 Authorization 头
 * @param enableLogging 只在 debug 打开；BODY 级日志会打印明文请求体
 */
fun createYoofiHttpClient(
    engine: HttpClientEngine,
    baseUrl: String,
    json: Json,
    accessTokenProvider: () -> String?,
    enableLogging: Boolean,
): HttpClient = HttpClient(engine) {
    // 非 2xx 抛 ResponseException，交给 KtorApiCaller 归一化，业务侧看不到 HTTP 异常
    expectSuccess = true

    install(ContentNegotiation) {
        json(json)
    }

    install(HttpTimeout) {
        connectTimeoutMillis = ConnectTimeoutMillis
        socketTimeoutMillis = SocketTimeoutMillis
        requestTimeoutMillis = RequestTimeoutMillis
    }

    if (enableLogging) {
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    // 取代原 AuthHeaderInterceptor：登录白名单接口此时尚无 token，不会加头
    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
        accessTokenProvider()
            ?.takeIf { it.isNotBlank() }
            ?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }
}
