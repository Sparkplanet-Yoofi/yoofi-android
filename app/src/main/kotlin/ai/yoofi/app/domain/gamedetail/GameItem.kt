package ai.yoofi.app.domain.gamedetail

/** 道具类型。普通卡直接使用，多人道具要先选人。 */
enum class GameItemKind {
    General,
    Multiplayer,
}

/**
 * 一张游戏道具。
 *
 * [imageKey] 现阶段对应本地卡面；接 CDN 后只改 UseCase 与预览适配。
 * [quantity] 为 0 时列表不画数量角标。
 */
data class GameItem(
    val id: String,
    val name: String,
    val cardDescription: String,
    val description: String,
    val imageKey: String,
    val quantity: Int,
    val remainingCards: Int,
    val remainingUses: Int,
    val usageScope: String,
    val usageRules: String,
    val kind: GameItemKind,
)

/** 多人道具的可选目标，字段对齐聊天室 [ai.yoofi.app.domain.chat.ChatCastMember]。 */
data class GameItemTarget(
    val id: String,
    val displayName: String,
    val identity: String,
    val avatarKey: String,
)

/**
 * 使用道具后发到聊天室的玩家气泡。
 * 普通卡没有目标；多人道具带上选中角色名。
 */
fun formatItemUseMessage(
    itemName: String,
    targetNames: List<String>,
): String {
    val names = targetNames.filter { it.isNotBlank() }
    return if (names.isEmpty()) {
        "Used $itemName."
    } else {
        "Used $itemName on ${names.joinToString(", ")}."
    }
}
