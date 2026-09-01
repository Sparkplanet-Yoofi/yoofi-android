package ai.yoofi.app.domain.gamedetail

/** Figma `2453:27240` 底图边长，单位 dp。布局按这个画，不按 PNG 像素。 */
const val GameMapDesignSizeDp = 916f

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
 * [previewKey] 是点标记后 Go 气泡里的地点预览；[sceneKey] 是点 Go 后聊天室底图。
 * 接接口时两把钥匙都由服务端下发，UI 只做解析。
 */
data class GameMapLocation(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val kind: GameMapMarkerKind,
    val previewKey: String = "",
    val sceneKey: String = "",
)

/**
 * 点 Go 后发到聊天室的玩家气泡。
 * 地点名为空时用 location，与稿面标签占位一致。
 */
fun formatMapGoMessage(locationName: String): String {
    val name = locationName.trim().ifBlank { "location" }
    return "Go to $name."
}

enum class GameMapMarkerKind {
    Label,
    Pin,
}
