package ai.yoofi.app.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 钉住从 Retrofit 迁到 Ktor 时最容易出错的两点：
 * Base URL 与相对路径的拼接语义，以及 Bearer 头的注入时机。
 */
class YoofiHttpClientTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun captureRequest(
        baseUrl: String = "http://test-cn.your-api-server.com/",
        token: String? = null,
        path: String = "customer/auth/login",
    ): HttpRequestData {
        lateinit var captured: HttpRequestData
        val engine = MockEngine { request ->
            captured = request
            respond(
                content = """{"code":0,"message":"ok"}""",
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString(),
                ),
            )
        }
        val client = createYoofiHttpClient(
            engine = engine,
            baseUrl = baseUrl,
            json = json,
            accessTokenProvider = { token },
            enableLogging = false,
        )
        runTest { client.get(path) }
        return captured
    }

    @Test
    fun `相对路径拼在 Base URL 之后，不吞掉路径段`() {
        val request = captureRequest()
        assertEquals(
            "http://test-cn.your-api-server.com/customer/auth/login",
            request.url.toString(),
        )
    }

    @Test
    fun `有 token 时带 Bearer 头`() {
        val request = captureRequest(token = "abc123")
        assertEquals("Bearer abc123", request.headers[HttpHeaders.Authorization])
    }

    @Test
    fun `无 token 时不加 Authorization 头`() {
        assertNull(captureRequest(token = null).headers[HttpHeaders.Authorization])
    }

    @Test
    fun `空白 token 视同未登录`() {
        assertNull(captureRequest(token = "   ").headers[HttpHeaders.Authorization])
    }

    @Test
    fun `默认带 JSON Content-Type`() {
        val request = captureRequest()
        val contentType = request.body.contentType ?: request.headers[HttpHeaders.ContentType]
            ?.let(ContentType::parse)
        assertEquals(ContentType.Application.Json, contentType)
    }
}
