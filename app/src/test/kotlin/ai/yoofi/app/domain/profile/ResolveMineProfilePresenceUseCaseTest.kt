package ai.yoofi.app.domain.profile

import ai.yoofi.app.domain.auth.AuthSession
import ai.yoofi.app.domain.auth.User
import ai.yoofi.app.domain.auth.UserSessionStore
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveMineProfilePresenceUseCaseTest {

    @Test
    fun `未登录是空态`() {
        val store = FakeSessionStore()
        assertEquals(MineProfilePresence.Vacant, ResolveMineProfilePresenceUseCase(store)())
    }

    @Test
    fun `已登录但未完善资料是空态`() {
        val store = FakeSessionStore()
        store.save(session(completed = false, nickname = "mock"))
        assertEquals(MineProfilePresence.Vacant, ResolveMineProfilePresenceUseCase(store)())
    }

    @Test
    fun `已完善但昵称为空是空态`() {
        val store = FakeSessionStore()
        store.save(session(completed = true, nickname = "   "))
        assertEquals(MineProfilePresence.Vacant, ResolveMineProfilePresenceUseCase(store)())
    }

    @Test
    fun `已完善且有昵称是主态`() {
        val store = FakeSessionStore()
        store.save(session(completed = true, nickname = "Jenny"))
        assertEquals(MineProfilePresence.Populated, ResolveMineProfilePresenceUseCase(store)())
    }
}

class MarkProfileCompletedUseCaseTest {

    @Test
    fun `无会话时不新建会话`() {
        val store = FakeSessionStore()
        MarkProfileCompletedUseCase(store)()
        assertEquals(null, store.currentSession())
    }

    @Test
    fun `有会话则只改 profileCompleted`() {
        val store = FakeSessionStore()
        store.save(session(completed = false, nickname = "mock"))
        MarkProfileCompletedUseCase(store)()
        val updated = store.currentSession()
        assertEquals(true, updated?.profileCompleted)
        assertEquals("mock", updated?.user?.nickname)
    }
}

private fun session(completed: Boolean, nickname: String): AuthSession = AuthSession(
    user = User(userId = 1L, nickname = nickname, avatarUrl = ""),
    accessToken = "at",
    refreshToken = "rt",
    accessExpiresIn = 1,
    refreshExpiresIn = 1,
    isNewUser = true,
    profileCompleted = completed,
)

private class FakeSessionStore : UserSessionStore {
    private var session: AuthSession? = null

    override fun save(session: AuthSession) {
        this.session = session
    }

    override fun currentUser(): User? = session?.user

    override fun currentAccessToken(): String? = session?.accessToken

    override fun currentSession(): AuthSession? = session

    override fun clear() {
        session = null
    }
}
