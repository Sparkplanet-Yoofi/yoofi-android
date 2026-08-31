package ai.yoofi.app.domain.auth

import ai.yoofi.app.core.common.Outcome
import ai.yoofi.app.testing.FakeUserSessionStore
import ai.yoofi.app.testing.fakeSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteAccountUseCaseTest {

    @Test
    fun `密码或短语不对则失败且保留会话`() = runBlocking {
        val store = FakeUserSessionStore().also { it.save(fakeSession()) }
        val result = DeleteAccountUseCase(store)(
            DeleteAccountProof.Password(password = "", phrase = "DELETE"),
        )
        assertTrue(result is Outcome.Err)
        assertNotNull(store.currentSession())
    }

    @Test
    fun `密码与短语正确则清会话`() = runBlocking {
        val store = FakeUserSessionStore().also { it.save(fakeSession()) }
        val result = DeleteAccountUseCase(store)(
            DeleteAccountProof.Password(password = "secret", phrase = DeleteConfirmPhrase),
        )
        assertTrue(result is Outcome.Ok)
        assertNull(store.currentSession())
    }

    @Test
    fun `验证码与短语正确则清会话`() = runBlocking {
        val store = FakeUserSessionStore().also { it.save(fakeSession()) }
        val result = DeleteAccountUseCase(store)(
            DeleteAccountProof.EmailCode(code = "123456", phrase = DeleteConfirmPhrase),
        )
        assertTrue(result is Outcome.Ok)
        assertNull(store.currentSession())
    }
}
