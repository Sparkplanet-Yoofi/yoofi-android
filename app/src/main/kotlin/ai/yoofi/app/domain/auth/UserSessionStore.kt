package ai.yoofi.app.domain.auth

/**
 * 当前登录会话（内存）。读取用户请走 [GetCurrentUserUseCase]，不要直接碰 Data 层。
 */
interface UserSessionStore {
    fun save(session: AuthSession)
    fun currentUser(): User?
    fun currentAccessToken(): String?
    fun clear()
}
