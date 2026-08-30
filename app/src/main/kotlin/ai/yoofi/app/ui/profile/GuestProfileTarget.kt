package ai.yoofi.app.ui.profile

/**
 * 打开客态页所需的最小身份。接口未定时由入口页（详情作者、评论者）带过来，
 * 接真实资料接口后只在 ViewModel 里换成 userId 拉取，这个入口契约不用改。
 */
internal data class GuestProfileTarget(
    val userId: String,
    val displayName: String,
    val avatarKey: String,
    val following: Boolean = false,
)
