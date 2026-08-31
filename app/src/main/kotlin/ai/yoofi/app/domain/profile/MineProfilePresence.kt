package ai.yoofi.app.domain.profile

/**
 * 「我的」页资料是否就绪。客态不走这里，避免和自己的空态缠在一起。
 */
enum class MineProfilePresence {
    /**
     * 已登录且资料已完善：主态
     */
    Populated,

    /**
     * 未登录，或已登录但资料未完善 / 昵称为空：空态
     */
    Vacant,
}
