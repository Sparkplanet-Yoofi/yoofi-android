package ai.yoofi.app.domain.gamedetail

/**
 * 一张可拖动的游戏地图。
 *
 * [locations] 的 [GameMapLocation.x] / [y] 是相对整图的 0..1，不是屏幕像素。
 * 接接口时把服务端 Location 除以图宽/图高即可，UI 不用改。
 * [imageKey] 现阶段对应本地底图；日后换成 CDN 地址也只改 UseCase 与图片解析。
 */
data class GameMap(
    val id: String,
    val title: String,
    val imageKey: String,
    val startOffsetX: Float,
    val startOffsetY: Float,
    val locations: List<GameMapLocation>,
)

/**
 * 地图打点。[x]/[y] 是图幅归一化坐标，左上为 (0,0)。
 * [kind] 决定画标签还是钉；[name] 给标签文案，钉可空。
 */
data class GameMapLocation(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val kind: GameMapMarkerKind,
)

enum class GameMapMarkerKind {
    Label,
    Pin,
}
