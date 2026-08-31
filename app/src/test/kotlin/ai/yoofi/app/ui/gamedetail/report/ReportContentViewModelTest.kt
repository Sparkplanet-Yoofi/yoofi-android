package ai.yoofi.app.ui.gamedetail.report

import ai.yoofi.app.domain.report.ReportReason
import ai.yoofi.app.domain.report.SubmitReportUseCase
import ai.yoofi.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportContentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `未选原因不能进入下一步`() {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.canGoNext)
        viewModel.onIntent(ReportContentIntent.Next)
        assertEquals(ReportStep.Reason, viewModel.uiState.value.step)
    }

    @Test
    fun `选原因后 Next 进入详情页`() {
        val viewModel = viewModel()
        viewModel.onIntent(ReportContentIntent.SelectReason(ReportReason.Violent))
        assertTrue(viewModel.uiState.value.canGoNext)
        viewModel.onIntent(ReportContentIntent.Next)
        assertEquals(ReportStep.Details, viewModel.uiState.value.step)
    }

    @Test
    fun `空详情不能提交`() {
        val viewModel = viewModel()
        viewModel.onIntent(ReportContentIntent.SelectReason(ReportReason.Violent))
        viewModel.onIntent(ReportContentIntent.Next)
        assertFalse(viewModel.uiState.value.canSubmit)
        viewModel.onIntent(ReportContentIntent.Submit)
        assertEquals(ReportStep.Details, viewModel.uiState.value.step)
    }

    @Test
    fun `填完详情提交进入成功页`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(ReportContentIntent.SelectReason(ReportReason.Violent))
        viewModel.onIntent(ReportContentIntent.Next)
        viewModel.onIntent(ReportContentIntent.DetailsChanged("Reason"))
        assertTrue(viewModel.uiState.value.canSubmit)
        viewModel.onIntent(ReportContentIntent.Submit)
        advanceUntilIdle()
        assertEquals(ReportStep.Done, viewModel.uiState.value.step)
    }

    @Test
    fun `截图最多三张且去重`() {
        val viewModel = viewModel()
        repeat(3) { index ->
            viewModel.onIntent(ReportContentIntent.AddScreenshot("uri-$index"))
        }
        viewModel.onIntent(ReportContentIntent.AddScreenshot("uri-0"))
        viewModel.onIntent(ReportContentIntent.AddScreenshot("uri-3"))
        assertEquals(3, viewModel.uiState.value.screenshotUris.size)
        assertFalse(viewModel.uiState.value.canAddScreenshot)
    }

    @Test
    fun `详情超过五百字会被截断`() {
        val viewModel = viewModel()
        viewModel.onIntent(ReportContentIntent.DetailsChanged("a".repeat(501)))
        assertEquals(ReportDetailsMaxLength, viewModel.uiState.value.details.length)
    }

    @Test
    fun `详情页返回回到原因页`() {
        val viewModel = viewModel()
        viewModel.onIntent(ReportContentIntent.SelectReason(ReportReason.Other))
        viewModel.onIntent(ReportContentIntent.Next)
        viewModel.onIntent(ReportContentIntent.Back)
        assertEquals(ReportStep.Reason, viewModel.uiState.value.step)
    }

    @Test
    fun `取消会关掉整页`() {
        var closed = false
        val viewModel = viewModel(onClose = { closed = true })
        viewModel.onIntent(ReportContentIntent.Cancel)
        assertTrue(closed)
    }

    private fun viewModel(
        onClose: () -> Unit = {},
    ): ReportContentViewModel {
        val viewModel = ReportContentViewModel(SubmitReportUseCase())
        viewModel.bind(
            target = ReportTarget(
                gameId = "forbidden-world",
                title = "Arranged Marriage Simulator",
                authorName = "Author Name",
                coverKey = "cover-forbidden-world",
            ),
            onClose = onClose,
        )
        return viewModel
    }
}
