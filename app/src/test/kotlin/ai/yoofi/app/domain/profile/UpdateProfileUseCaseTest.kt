package ai.yoofi.app.domain.profile

import ai.yoofi.app.core.common.Outcome
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateProfileUseCaseTest {

    @Test
    fun `有展示名则成功`() = runBlocking {
        val result = UpdateProfileUseCase()(
            ProfileDraft(displayName = "Jenny", genderKey = "Female"),
        )
        assertTrue(result is Outcome.Ok)
    }

    @Test
    fun `空白展示名失败`() = runBlocking {
        val empty = UpdateProfileUseCase()(
            ProfileDraft(displayName = "", genderKey = null),
        )
        val blank = UpdateProfileUseCase()(
            ProfileDraft(displayName = "   ", genderKey = null),
        )
        assertTrue(empty is Outcome.Err)
        assertTrue(blank is Outcome.Err)
    }
}