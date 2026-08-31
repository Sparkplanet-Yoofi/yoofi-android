package ai.yoofi.app.domain.profile

import ai.yoofi.app.domain.auth.UserSessionStore

/**
 * 判定「我的」该走主态还是空态。
 * 未登录、未完善资料、昵称为空都视为空态；Tab 壳以后允许未登录进入时不用改 UI。
 */
class ResolveMineProfilePresenceUseCase(
    private val userSessionStore: UserSessionStore,
) {
    operator fun invoke(): MineProfilePresence {
        val session = userSessionStore.currentSession() ?: return MineProfilePresence.Vacant
        if (!session.profileCompleted) return MineProfilePresence.Vacant
        if (session.user.nickname.isBlank()) return MineProfilePresence.Vacant
        return MineProfilePresence.Populated
    }
}
