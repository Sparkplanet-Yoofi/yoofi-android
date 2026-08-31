package ai.yoofi.app.data.auth

import ai.yoofi.app.core.common.AppError
import ai.yoofi.app.core.common.Outcome
import ai.yoofi.app.domain.auth.AuthSession
import ai.yoofi.app.domain.auth.User
import ai.yoofi.app.domain.auth.UserSessionStore
import ai.yoofi.app.domain.auth.VerifyEmailCodeResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteAuthRepositoryTest {

    @Test
    fun `登录成功写入会话并返回 isNewUser`() = runBlocking {
        val store = FakeUserSessionStore()
        val remote = FakeAuthRemoteDataSource(
            Outcome.Ok(
                LoginDataDto(
                    accessToken = "at",
                    accessExpiresIn = 900,
                    refreshToken = "rt",
                    refreshExpiresIn = 604800,
                    user = UserSummaryDto(userId = 42L, nickname = "xiaoming"),
                    isNewUser = true,
                    profileCompleted = false,
                ),
            ),
        )
        val repo = RemoteAuthRepository(remote, store)
        val result = repo.verifyEmailCode("a@b.com", "123456")
        assertEquals(
            VerifyEmailCodeResult.Success(isNewUser = true, profileCompleted = false),
            result,
        )
        assertEquals(42L, store.currentUser()?.userId)
    }

    @Test
    fun `远程失败不写会话`() = runBlocking {
        val store = FakeUserSessionStore()
        val remote = FakeAuthRemoteDataSource(
            Outcome.Err(AppError.Api(code = 4013, message = "bad")),
        )
        val repo = RemoteAuthRepository(remote, store)
        val result = repo.verifyEmailCode("a@b.com", "123456")
        assertEquals(VerifyEmailCodeResult.InvalidCode, result)
        assertNull(store.currentUser())
    }
}

private class FakeAuthRemoteDataSource(
    private val outcome: Outcome<LoginDataDto>,
) : AuthRemoteDataSource {
    override suspend fun login(email: String, code: String): Outcome<LoginDataDto> = outcome
}

private class FakeUserSessionStore : UserSessionStore {
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
