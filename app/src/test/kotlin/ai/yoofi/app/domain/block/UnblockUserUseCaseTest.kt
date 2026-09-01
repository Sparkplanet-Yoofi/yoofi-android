package ai.yoofi.app.domain.block

import ai.yoofi.shared.common.Outcome
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class UnblockUserUseCaseTest {

    @Test
    fun `接口未定时占位成功`() = runBlocking {
        val result = UnblockUserUseCase()("blocked-jenny")
        assertTrue(result is Outcome.Ok)
    }
}
