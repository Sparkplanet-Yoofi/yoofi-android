package ai.yoofi.app.domain.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetLinkedAccountsUseCaseTest {

    @Test
    fun `Demo 默认双账号都已绑定`() {
        val accounts = GetLinkedAccountsUseCase()()
        assertEquals(2, accounts.size)
        assertTrue(accounts.all { it.linked })
        assertEquals(
            listOf(LinkedAccountProvider.Google, LinkedAccountProvider.Apple),
            accounts.map { it.provider },
        )
    }
}
