package ai.yoofi.app.domain.block

/**
 * 黑名单里的一条用户。
 * [avatarKey] 是资源标识，UI 层映射到本地图；接接口后可换成 URL。
 * [blockedOn] 用 `yyyy.MM.dd`，domain 不碰 `java.time`。
 */
data class BlockedUser(
    val id: String,
    val displayName: String,
    val blockedOn: String,
    val avatarKey: String,
)
