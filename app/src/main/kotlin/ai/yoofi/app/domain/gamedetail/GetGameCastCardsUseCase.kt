package ai.yoofi.app.domain.gamedetail

/**
 * 读取游戏详情人物页。接口未定时返回 Figma `2304:23753` 的四张金卡 + 两个空槽，
 * 接上后只改这里。
 */
class GetGameCastCardsUseCase {
    operator fun invoke(): List<GameCastCard> = DemoGameCastCards
}

internal val DemoGameCastCards = listOf(
    GameCastCard(
        id = "cast-sunnme-me",
        name = "sunnme",
        role = GameCastRole.Me,
        portraitKey = "sunnme-a",
    ),
    GameCastCard(
        id = "cast-tomy-player",
        name = "TOMY",
        role = GameCastRole.PlayerRole,
        portraitKey = "tomy-a",
    ),
    GameCastCard(
        id = "cast-sunnme-player",
        name = "sunnme",
        role = GameCastRole.PlayerRole,
        portraitKey = "sunnme-b",
    ),
    GameCastCard(
        id = "cast-tomy-b",
        name = "TOMY",
        role = GameCastRole.PlayerRole,
        portraitKey = "tomy-b",
    ),
    GameCastCard(
        id = "cast-empty-1",
        name = null,
        role = null,
        portraitKey = null,
    ),
    GameCastCard(
        id = "cast-empty-2",
        name = null,
        role = null,
        portraitKey = null,
    ),
)
