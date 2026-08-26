package ai.yoofi.app.domain.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetCurrentUserUseCaseTest {

    @Test
    fun `未登录返回 null`() {
        val store = FakeUserSessionStore()
        assertNull(GetCurrentUserUseCase(store)())
    }

    @Test
    fun `登录后可读到 User`() {
        val store = FakeUserSessionStore()
        val user = User(userId = 42L, nickname = "xiaoming", avatarUrl = "")
        store.save(
            AuthSession(
                user = user,
                accessToken = "at",
                refreshToken = "rt",
                accessExpiresIn = 900,
                refreshExpiresIn = 604800,
                isNewUser = true,
                profileCompleted = false,
            ),
        )
        assertEquals(user, GetCurrentUserUseCase(store)())
    }
}

private class FakeUserSessionStore : UserSessionStore {
    private var session: AuthSession? = null

    override fun save(session: AuthSession) {
        this.session = session
    }

    override fun currentUser(): User? = session?.user

    override fun currentAccessToken(): String? = session?.accessToken

    override fun clear() {
        session = null
    }
}
