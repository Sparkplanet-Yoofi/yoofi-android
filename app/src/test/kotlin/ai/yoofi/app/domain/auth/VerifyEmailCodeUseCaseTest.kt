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
    fun `合法码且新用户返回 isNewUser true`() = runBlocking {
        val useCase = VerifyEmailCodeUseCase(
            FakeAuthRepository(isNewUser = true, profileCompleted = false),
        )
        val result = useCase(email = "test@gmail.com", code = "654321")
        assertEquals(
            VerifyEmailCodeResult.Success(
                isNewUser = true,
                profileCompleted = false,
            ),
            result,
        )
    }

    @Test
    fun `合法码且老用户返回 isNewUser false`() = runBlocking {
        val useCase = VerifyEmailCodeUseCase(
            FakeAuthRepository(isNewUser = false, profileCompleted = true),
        )
        val result = useCase(email = "test@gmail.com", code = "654321")
        assertEquals(
            VerifyEmailCodeResult.Success(
                isNewUser = false,
                profileCompleted = true,
            ),
            result,
        )
    }
}

private class FakeAuthRepository(
    private val isNewUser: Boolean = false,
    private val profileCompleted: Boolean = false,
) : AuthRepository {
    override suspend fun verifyEmailCode(
        email: String,
        code: String,
    ): VerifyEmailCodeResult {
        if (code == DemoInvalidEmailOtp) {
            return VerifyEmailCodeResult.InvalidCode
        }
        return VerifyEmailCodeResult.Success(
            isNewUser = isNewUser,
            profileCompleted = profileCompleted,
        )
    }
}
