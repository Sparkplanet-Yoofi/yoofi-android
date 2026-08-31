package ai.yoofi.app.ui.profile

import ai.yoofi.app.domain.profile.MineProfilePresence

/**
 * 资料页观众。加新态只加分支 + 对应 Screen，禁止往卡上塞 `isSelf` / `isEmpty`。
 *
 * - [Mine]：Tab「我的」，再按 [MineProfilePresence] 分主态 / 空态
 * - [Guest]：栈上客态 overlay
 */
internal sealed interface ProfileAudience {
    /** 当前用户看自己的个人中心。 */
    sealed interface Mine : ProfileAudience {
        data object Populated : Mine
        data object Vacant : Mine
    }

    /** 看其他用户。 */
    data class Guest(val target: GuestProfileTarget) : ProfileAudience
}

internal fun MineProfilePresence.toAudience(): ProfileAudience.Mine = when (this) {
    MineProfilePresence.Populated -> ProfileAudience.Mine.Populated
    MineProfilePresence.Vacant -> ProfileAudience.Mine.Vacant
}
