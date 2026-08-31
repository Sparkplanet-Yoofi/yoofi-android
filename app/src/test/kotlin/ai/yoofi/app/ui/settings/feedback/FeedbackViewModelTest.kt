package ai.yoofi.app.ui.settings.feedback

import ai.yoofi.app.domain.feedback.FeedbackType
import ai.yoofi.app.domain.feedback.SubmitFeedbackUseCase
import ai.yoofi.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `未选类型或空描述不能提交`() {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.canSubmit)
        viewModel.onIntent(FeedbackIntent.SelectType(FeedbackType.Bug))
        assertFalse(viewModel.uiState.value.canSubmit)
        viewModel.onIntent(FeedbackIntent.DetailsChanged("12345679"))
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `描述超过 500 截断`() {
        val viewModel = viewModel()
        viewModel.onIntent(FeedbackIntent.DetailsChanged("a".repeat(501)))
        assertEquals(500, viewModel.uiState.value.details.length)
    }

    @Test
    fun `联系方式可选仍可提交`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(FeedbackIntent.SelectType(FeedbackType.Suggestion))
        viewModel.onIntent(FeedbackIntent.DetailsChanged("hello"))
        viewModel.onIntent(FeedbackIntent.Submit)
        runCurrent()
        assertEquals(FeedbackStep.Done, viewModel.uiState.value.step)
    }

    @Test
    fun `未填描述点提交不停在表单`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(FeedbackIntent.SelectType(FeedbackType.Bug))
        viewModel.onIntent(FeedbackIntent.Submit)
        runCurrent()
        assertEquals(FeedbackStep.Form, viewModel.uiState.value.step)
    }

    @Test
    fun `成功页返回关闭 overlay`() {
        var closed = false
        val viewModel = viewModel { closed = true }
        viewModel.onIntent(FeedbackIntent.Back)
        assertTrue(closed)
    }

    private fun viewModel(onClose: () -> Unit = {}): FeedbackViewModel {
        val viewModel = FeedbackViewModel(SubmitFeedbackUseCase())
        viewModel.bind(onClose)
        return viewModel
    }
}
