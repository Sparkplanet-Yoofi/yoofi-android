package ai.yoofi.app.domain.block

/**
 * 读取黑名单。接口未定时返回 Figma `2252:17322` 的 Demo 五人，接上后只改这里。
 */
class GetBlockedUsersUseCase {
    operator fun invoke(): List<BlockedUser> = DemoBlockedUsers
}

internal val DemoBlockedUsers = listOf(
    BlockedUser(
        id = "blocked-jenny",
        displayName = "Jenny",
        blockedOn = "2026.07.12",
        avatarKey = "jenny",
    ),
    BlockedUser(
        id = "blocked-lopez",
        displayName = "Lopez",
        blockedOn = "2026.06.12",
        avatarKey = "lopez",
    ),
    BlockedUser(
        id = "blocked-lavgine",
        displayName = "Lavgine",
        blockedOn = "2026.06.01",
        avatarKey = "lavgine",
    ),
    BlockedUser(
        id = "blocked-troy",
        displayName = "Troy123",
        blockedOn = "2026.05.22",
        avatarKey = "troy",
    ),
    BlockedUser(
        id = "blocked-sony",
        displayName = "Sony",
        blockedOn = "2025.07.12",
        avatarKey = "sony",
    ),
)
