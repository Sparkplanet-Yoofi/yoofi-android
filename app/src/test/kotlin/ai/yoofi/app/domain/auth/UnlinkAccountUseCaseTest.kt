package ai.yoofi.app.domain.auth

import ai.yoofi.shared.common.Outcome
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class UnlinkAccountUseCaseTest {

    @Test
    fun `接口未定时占位成功`() = runBlocking {
        val result = UnlinkAccountUseCase()(LinkedAccountProvider.Google)
        assertTrue(result is Outcome.Ok)
    }
}
