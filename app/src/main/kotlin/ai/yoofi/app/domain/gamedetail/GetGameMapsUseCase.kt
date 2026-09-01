package ai.yoofi.app.domain.gamedetail

/**
 * 读取当前游戏可用地图。接口未定时返回 Figma `2453:27236` 的四张 Demo 图，
 * 接上后只改这里，UI 继续吃 [GameMap] 列表。
 */
class GetGameMapsUseCase {
    operator fun invoke(): List<GameMap> = DemoGameMaps
}

/** Figma `2453:27236` 底图 916×916，摆在屏幕 (-242, 12)。 */
private const val FigMapSize = 916f
private const val FigImageLeft = -242f
private const val FigImageTop = 12f

private val DemoStartOffsetX = FigImageLeft / FigMapSize
private val DemoStartOffsetY = FigImageTop / FigMapSize

internal val DemoGameMaps = listOf(
    GameMap(
        id = "map-01",
        title = "Map 01",
        imageKey = "demo-world",
        startOffsetX = DemoStartOffsetX,
        startOffsetY = DemoStartOffsetY,
        locations = map01Locations(),
    ),
    GameMap(
        id = "map-02",
        title = "Map 02",
        imageKey = "demo-world",
        startOffsetX = DemoStartOffsetX,
        startOffsetY = DemoStartOffsetY,
        locations = shiftLocations(map01Locations(), dx = 0.06f, dy = -0.08f),
    ),
    GameMap(
        id = "map-03",
        title = "Map 03",
        imageKey = "demo-world",
        startOffsetX = DemoStartOffsetX,
        startOffsetY = DemoStartOffsetY,
        locations = shiftLocations(map01Locations(), dx = -0.08f, dy = 0.05f),
    ),
    GameMap(
        id = "map-04",
        title = "Map 04",
        imageKey = "demo-world",
        startOffsetX = DemoStartOffsetX,
        startOffsetY = DemoStartOffsetY,
        locations = shiftLocations(map01Locations(), dx = 0.10f, dy = 0.07f),
    ),
)

/**
 * 把 Figma 屏幕坐标换成图幅 0..1。
 * 屏幕点 (sx, sy) 对应图上 (sx - left, sy - top)。
 */
private fun figNormX(screenX: Float): Float = (screenX - FigImageLeft) / FigMapSize

private fun figNormY(screenY: Float): Float = (screenY - FigImageTop) / FigMapSize

private fun map01Locations(): List<GameMapLocation> = listOf(
    GameMapLocation("loc-1", "location", figNormX(137f), figNormY(313f), GameMapMarkerKind.Label),
    GameMapLocation("loc-2", "location", figNormX(297f), figNormY(195f), GameMapMarkerKind.Label),
    GameMapLocation("loc-3", "location", figNormX(259f), figNormY(518f), GameMapMarkerKind.Label),
    GameMapLocation("loc-4", "location", figNormX(26f), figNormY(488f), GameMapMarkerKind.Label),
    GameMapLocation("pin-1", "", figNormX(156f), figNormY(347f), GameMapMarkerKind.Pin),
)

private fun shiftLocations(
    source: List<GameMapLocation>,
    dx: Float,
    dy: Float,
): List<GameMapLocation> = source.map { mark ->
    mark.copy(
        id = "${mark.id}-shift",
        x = (mark.x + dx).coerceIn(0f, 1f),
        y = (mark.y + dy).coerceIn(0f, 1f),
    )
}
