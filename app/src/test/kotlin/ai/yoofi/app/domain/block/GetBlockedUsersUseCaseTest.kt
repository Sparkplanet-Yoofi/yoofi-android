package ai.yoofi.app.domain.block

import org.junit.Assert.assertEquals
import org.junit.Test

class GetBlockedUsersUseCaseTest {

    @Test
    fun `Demo 默认五人且顺序与稿面一致`() {
        val users = GetBlockedUsersUseCase()()
        assertEquals(5, users.size)
        assertEquals(
            listOf("Jenny", "Lopez", "Lavgine", "Troy123", "Sony"),
            users.map { it.displayName },
        )
    }
}
