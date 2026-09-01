package ai.yoofi.app.domain.gamedetail

/**
 * 读取游戏详情道具列表。接口未定时返回 Figma `2304:24267` 的四张卡：
 * 第一张普通（General / Use Item），第二张多人（Select Target）。
 * 接上后只改这里。
 */
class GetGameItemsUseCase {
    operator fun invoke(): List<GameItem> = DemoGameItems
}

private const val SheetDescription =
    "Use in dark environments. The flame turns blue when you are near a spirit."

private const val UsageScope =
    "Player “Sanity” +1 or selected character “Affection” +5"

private const val UsageRules =
    "This is a universal card that can be used on any character. " +
        "Each card can be used up to 5 times."

private const val CardDescription =
    "Add a description Enter a descriptionAdd a description Enter a description"

internal val DemoGameItems = listOf(
    GameItem(
        id = "item-knife",
        name = "Name",
        cardDescription = CardDescription,
        description = SheetDescription,
        imageKey = "knife",
        quantity = 99,
        remainingCards = 8,
        remainingUses = 39,
        usageScope = UsageScope,
        usageRules = UsageRules,
        kind = GameItemKind.General,
    ),
    GameItem(
        id = "item-lollipops",
        name = "Name",
        cardDescription = CardDescription,
        description = SheetDescription,
        imageKey = "lollipops",
        quantity = 29,
        remainingCards = 8,
        remainingUses = 39,
        usageScope = UsageScope,
        usageRules = UsageRules,
        kind = GameItemKind.Multiplayer,
    ),
    GameItem(
        id = "item-key",
        name = "Name",
        cardDescription = CardDescription,
        description = SheetDescription,
        imageKey = "key",
        quantity = 0,
        remainingCards = 8,
        remainingUses = 39,
        usageScope = UsageScope,
        usageRules = UsageRules,
        kind = GameItemKind.General,
    ),
    GameItem(
        id = "item-goblet",
        name = "Name",
        cardDescription = CardDescription,
        description = SheetDescription,
        imageKey = "goblet",
        quantity = 0,
        remainingCards = 8,
        remainingUses = 39,
        usageScope = UsageScope,
        usageRules = UsageRules,
        kind = GameItemKind.General,
    ),
)
