package ai.yoofi.app.core.network

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `成功信封转 Ok`() {
        val response = ApiResponse(
            code = ApiSuccessCode,
            message = "ok",
            data = "payload",
        )
        val outcome = response.toOutcome()
        assertEquals(Outcome.Ok("payload"), outcome)
    }

    @Test
    fun `业务错误码转 Api`() {
        val response = ApiResponse<String>(
            code = ApiInvalidOrExpiredCode,
            message = "expired",
            data = null,
        )
        val outcome = response.toOutcome()
        assertEquals(
            Outcome.Err(AppError.Api(ApiInvalidOrExpiredCode, "expired")),
            outcome,
        )
    }

    @Test
    fun `错误 JSON 体解析业务码`() {
        val raw = """{"code":4013,"message":"bad"}"""
        val error = parseApiErrorBody(raw, json)
        assertEquals(AppError.Api(4013, "bad"), error)
    }

    @Test
    fun `空错误体视为 Unknown`() {
        assertTrue(parseApiErrorBody("", json) is AppError.Unknown)
    }
}
