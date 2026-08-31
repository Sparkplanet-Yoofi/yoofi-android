package ai.yoofi.app.domain.feedback

import ai.yoofi.app.core.common.Outcome
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitFeedbackUseCaseTest {

    @Test
    fun `有描述时占位成功`() = runBlocking {
        val result = SubmitFeedbackUseCase()(
            FeedbackDraft(
                type = FeedbackType.Bug,
                details = "12345679",
                contact = "123456@gmail.com",
            ),
        )
        assertTrue(result is Outcome.Ok)
    }

    @Test
    fun `空描述失败`() = runBlocking {
        val result = SubmitFeedbackUseCase()(
            FeedbackDraft(
                type = FeedbackType.Bug,
                details = "   ",
                contact = "",
            ),
        )
        assertTrue(result is Outcome.Err)
    }
}
