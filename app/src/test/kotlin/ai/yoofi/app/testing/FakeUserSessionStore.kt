package ai.yoofi.app.testing

import ai.yoofi.app.domain.auth.AuthSession
import ai.yoofi.app.domain.auth.User
import ai.yoofi.app.domain.auth.UserSessionStore

internal class FakeUserSessionStore : UserSessionStore {
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

internal fun fakeSession(userId: Long = 1L): AuthSession = AuthSession(
    user = User(userId = userId, nickname = "jenny", avatarUrl = ""),
    accessToken = "at",
    refreshToken = "rt",
    accessExpiresIn = 900,
    refreshExpiresIn = 604800,
    isNewUser = false,
    profileCompleted = true,
)
