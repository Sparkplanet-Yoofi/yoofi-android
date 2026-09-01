package ai.yoofi.app.domain.gamedetail

/**
 * 读取角色详情。接口未定时四张金卡都回 Figma `2409:27067` 的同一份 Demo，
 * 空槽返回 null；接上后只改这里。
 */
class GetGameCastCharacterUseCase {
    operator fun invoke(characterId: String): GameCastCharacter? {
        val card = DemoGameCastCards.firstOrNull { it.id == characterId } ?: return null
        if (card.portraitKey == null) return null
        return DemoCastCharacter.copy(id = characterId)
    }
}

internal val DemoCastCharacter = GameCastCharacter(
    id = "cast-sunnme-me",
    title = "Forbidden Game",
    following = false,
    tab = GameCastCharacterTab.MyCreations,
    synopsisTitle = "Forbidden Game：",
    synopsis = "The metal floor of the capsule rises beneath you, and then the " +
        "world detonates into sound and heat. Sand, sun, and the roar of a " +
        "bloodthirsty,, and the …",
    heroKey = "forbidden-hero",
    favorited = false,
)
