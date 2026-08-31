package ai.yoofi.app.domain.auth

/**
 * 退出登录：清内存会话。Token 落盘后只改这里，不要把 clear 写进 Screen。
 */
class LogoutUseCase(
    private val userSessionStore: UserSessionStore,
) {
    operator fun invoke() {
        userSessionStore.clear()
    }
}
