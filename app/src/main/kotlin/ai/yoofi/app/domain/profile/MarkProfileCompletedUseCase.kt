package ai.yoofi.app.domain.profile

import ai.yoofi.app.domain.auth.UserSessionStore

/**
 * 创建资料成功后把会话标成已完善。没有会话时静默返回，不另起一套本地旗标。
 */
class MarkProfileCompletedUseCase(
    private val userSessionStore: UserSessionStore,
) {
    operator fun invoke() {
        val session = userSessionStore.currentSession() ?: return
        if (session.profileCompleted) return
        userSessionStore.save(session.copy(profileCompleted = true))
    }
}
