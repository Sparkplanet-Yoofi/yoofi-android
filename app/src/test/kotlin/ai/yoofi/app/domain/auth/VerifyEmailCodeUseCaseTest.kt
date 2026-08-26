package ai.yoofi.app.domain.auth

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class VerifyEmailCodeUseCaseTest {

    @Test
    fun `空邮箱视为验证码无效`() = runBlocking {
        val useCase = VerifyEmailCodeUseCase(FakeAuthRepository())
        val result = useCase(email = "  ", code = "123456")
        assertEquals(VerifyEmailCodeResult.InvalidCode, result)
    }

    @Test
    fun `演示错误码原样返回 InvalidCode`() = runBlocking {
        val useCase = VerifyEmailCodeUseCase(FakeAuthRepository())
        val result = useCase(email = "test@gmail.com", code = DemoInvalidEmailOtp)
        assertEquals(VerifyEmailCodeResult.InvalidCode, result)
    }

    @Test
    fun `合法码且未填资料返回 Success false`() = runBlocking {
        val useCase = VerifyEmailCodeUseCase(
            FakeAuthRepository(hasUserProfile = false),
        )
        val result = useCase(email = "test@gmail.com", code = "654321")
        assertEquals(
            VerifyEmailCodeResult.Success(hasUserProfile = false),
            result,
        )
    }

    @Test
    fun `合法码且已填资料返回 Success true`() = runBlocking {
        val useCase = VerifyEmailCodeUseCase(
            FakeAuthRepository(hasUserProfile = true),
        )
        val result = useCase(email = "test@gmail.com", code = "654321")
        assertEquals(
            VerifyEmailCodeResult.Success(hasUserProfile = true),
            result,
        )
    }
}

private class FakeAuthRepository(
    private val hasUserProfile: Boolean = false,
) : AuthRepository {
    override suspend fun verifyEmailCode(
        email: String,
        code: String,
    ): VerifyEmailCodeResult {
        if (code == DemoInvalidEmailOtp) {
            return VerifyEmailCodeResult.InvalidCode
        }
        return VerifyEmailCodeResult.Success(hasUserProfile = hasUserProfile)
    }
}
