package ai.yoofi.app.domain.auth

/**
 * 读取当前登录用户；未登录返回 null。
 */
class GetCurrentUserUseCase(
    private val userSessionStore: UserSessionStore,
) {
    operator fun invoke(): User? = userSessionStore.currentUser()
}
