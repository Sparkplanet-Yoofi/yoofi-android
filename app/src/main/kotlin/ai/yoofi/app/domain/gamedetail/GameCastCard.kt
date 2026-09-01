package ai.yoofi.app.domain.gamedetail

/**
 * 游戏详情人物页一张卡。空槽 [portraitKey] / [name] / [role] 都为空。
 */
data class GameCastCard(
    val id: String,
    val name: String?,
    val role: GameCastRole?,
    val portraitKey: String?,
)

enum class GameCastRole {
    Me,
    PlayerRole,
}
