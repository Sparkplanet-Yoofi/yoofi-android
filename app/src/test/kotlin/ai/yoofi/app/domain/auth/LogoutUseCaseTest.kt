package ai.yoofi.app.domain.auth

import ai.yoofi.app.testing.FakeUserSessionStore
import ai.yoofi.app.testing.fakeSession
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LogoutUseCaseTest {

    @Test
    fun `退出登录会清会话`() {
        val store = FakeUserSessionStore()
        store.save(fakeSession())
        assertNotNull(store.currentSession())
        LogoutUseCase(store)()
        assertNull(store.currentSession())
    }
}
