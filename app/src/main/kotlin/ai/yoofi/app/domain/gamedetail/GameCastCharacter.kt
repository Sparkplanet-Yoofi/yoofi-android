package ai.yoofi.app.domain.gamedetail

/**
 * 角色详情。文案与头图标识由接口下发；[tab] 是当前选中的作品分类。
 */
data class GameCastCharacter(
    val id: String,
    val title: String,
    val following: Boolean,
    val tab: GameCastCharacterTab,
    val synopsisTitle: String,
    val synopsis: String,
    val heroKey: String,
    val favorited: Boolean,
)

enum class GameCastCharacterTab {
    All,
    MyCreations,
    Collections,
}
