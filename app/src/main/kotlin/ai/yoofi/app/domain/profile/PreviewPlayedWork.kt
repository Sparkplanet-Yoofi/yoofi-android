package ai.yoofi.app.domain.profile

/**
 * 预览页 Played 卡。封面 / 类型用 key，UI 再映射 drawable 与文案。
 */
data class PreviewPlayedWork(
    val id: String,
    val coverKey: String,
    val genre: PreviewPlayedGenre,
)

enum class PreviewPlayedGenre {
    IndieGames,
    MurderMystery,
}
