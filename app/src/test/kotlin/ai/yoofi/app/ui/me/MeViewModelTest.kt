package ai.yoofi.app.ui.me

import ai.yoofi.app.domain.auth.AuthSession
import ai.yoofi.app.domain.auth.User
import ai.yoofi.app.domain.auth.UserSessionStore
import ai.yoofi.app.domain.profile.MineProfilePresence
import ai.yoofi.app.domain.profile.ResolveMineProfilePresenceUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class MeViewModelTest {

    @Test
    fun `初始化按会话解析空态`() {
        val store = FakeSessionStore()
        val viewModel = MeViewModel(ResolveMineProfilePresenceUseCase(store))
        assertEquals(MineProfilePresence.Vacant, viewModel.presence.value)
    }

    @Test
    fun `完善资料后 refresh 切到主态`() {
        val store = FakeSessionStore()
        val viewModel = MeViewModel(ResolveMineProfilePresenceUseCase(store))
        store.save(
            AuthSession(
                user = User(userId = 1L, nickname = "Jenny", avatarUrl = ""),
                accessToken = "at",
                refreshToken = "rt",
                accessExpiresIn = 1,
                refreshExpiresIn = 1,
                isNewUser = false,
                profileCompleted = true,
            ),
        )
        viewModel.refresh()
        assertEquals(MineProfilePresence.Populated, viewModel.presence.value)
    }
}

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
