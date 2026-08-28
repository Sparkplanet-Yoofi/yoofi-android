package ai.yoofi.app.core.network

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@Serializable
private data class Payload(val name: String = "")

/**
 * 钉住 [KtorApiCaller] 的异常归一化契约：它是全项目唯一 catch HTTP 的地方，
 * 换客户端时这组用例必须照样绿。
 */
class KtorApiCallerTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        isLenient = true
    }

    private val caller = KtorApiCaller(json)

    private fun clientReturning(
        handler: MockEngine.Companion.() -> MockEngine,
    ): HttpClient = HttpClient(MockEngine.handler()) {
        expectSuccess = true
        install(ContentNegotiation) { json(json) }
    }

    private fun jsonClient(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        clientReturning {
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
            }
        }

    private suspend fun fetchPayload(client: HttpClient): Outcome<Payload> =
        caller.fetch { client.get("any").body<ApiResponse<Payload>>() }

    @Test
    fun `业务码 0 且有 data 时返回 Ok`() = runTest {
        val client = jsonClient("""{"code":0,"message":"ok","data":{"name":"yoofi"}}""")
        val outcome = fetchPayload(client)
        assertEquals(Outcome.Ok(Payload("yoofi")), outcome)
    }

    @Test
    fun `HTTP 200 但业务码非 0 时返回 Api 错误`() = runTest {
        val client = jsonClient("""{"code":$ApiInvalidOrExpiredCode,"message":"expired"}""")
        val outcome = fetchPayload(client)
        assertEquals(
            Outcome.Err(AppError.Api(code = ApiInvalidOrExpiredCode, message = "expired")),
            outcome,
        )
    }

    @Test
    fun `非 2xx 时解析错误信封而不是丢成 Unknown`() = runTest {
        val client = jsonClient(
            body = """{"code":$ApiAccountBlocked,"message":"blocked"}""",
            status = HttpStatusCode.Forbidden,
        )
        val outcome = fetchPayload(client)
        assertEquals(
            Outcome.Err(AppError.Api(code = ApiAccountBlocked, message = "blocked")),
            outcome,
        )
    }

    @Test
    fun `非 2xx 且错误体不是信封时落到 Unknown`() = runTest {
        val client = clientReturning {
            MockEngine { respondError(HttpStatusCode.BadGateway, content = "<html>502</html>") }
        }
        assertEquals(Outcome.Err(AppError.Unknown), fetchPayload(client))
    }

    @Test
    fun `IO 异常归一化为 Network 而不是抛给业务`() = runTest {
        val client = clientReturning {
            MockEngine { throw IOException("connection reset") }
        }
        assertEquals(Outcome.Err(AppError.Network), fetchPayload(client))
    }

    @Test
    fun `协程取消必须原样抛出，不能被吞成失败态`() = runTest {
        val client = clientReturning {
            MockEngine { throw kotlin.coroutines.cancellation.CancellationException("cancelled") }
        }
        val thrown = runCatching { fetchPayload(client) }.exceptionOrNull()
        assertTrue(
            "取消异常被吞掉了：$thrown",
            thrown is kotlin.coroutines.cancellation.CancellationException,
        )
    }
}
