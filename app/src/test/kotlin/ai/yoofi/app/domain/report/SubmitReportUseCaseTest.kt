package ai.yoofi.app.domain.report

import ai.yoofi.app.core.common.Outcome
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitReportUseCaseTest {

    @Test
    fun `详情为空视为校验失败`() = runBlocking {
        val result = SubmitReportUseCase()(draft(details = "   "))
        assertTrue(result is Outcome.Err)
    }

    @Test
    fun `详情非空则占位成功`() = runBlocking {
        val result = SubmitReportUseCase()(draft(details = "Reason"))
        assertTrue(result is Outcome.Ok)
    }

    private fun draft(details: String) = ReportDraft(
        gameId = "forbidden-world",
        reason = ReportReason.Violent,
        details = details,
    )
}
