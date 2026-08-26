package ai.yoofi.app.data.auth

import ai.yoofi.app.domain.auth.AuthSession
import ai.yoofi.app.domain.auth.User
import ai.yoofi.app.domain.auth.UserSessionStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 进程内会话；杀进程后需重新登录。后续可接到 DataStore 持久化 token。
 */
@Singleton
class InMemoryUserSession @Inject constructor() : UserSessionStore {
    @Volatile
    private var session: AuthSession? = null

    @Synchronized
    override fun save(session: AuthSession) {
        this.session = session
    }

    override fun currentUser(): User? = session?.user

    override fun currentAccessToken(): String? = session?.accessToken

    @Synchronized
    override fun clear() {
        session = null
    }
}
