package ai.yoofi.app.domain.profile

/**
 * 读取「别人眼里的已玩」。接口未定时返回 Figma `2252:19446` 的四张 Demo 卡，
 * 接上后只改这里。
 */
class GetPreviewPlayedWorksUseCase {
    operator fun invoke(): List<PreviewPlayedWork> = DemoPreviewPlayedWorks
}

internal val DemoPreviewPlayedWorks = listOf(
    PreviewPlayedWork(
        id = "played-a",
        coverKey = "cover-e",
        genre = PreviewPlayedGenre.IndieGames,
    ),
    PreviewPlayedWork(
        id = "played-b",
        coverKey = "cover-d",
        genre = PreviewPlayedGenre.MurderMystery,
    ),
    PreviewPlayedWork(
        id = "played-c",
        coverKey = "cover-a",
        genre = PreviewPlayedGenre.IndieGames,
    ),
    PreviewPlayedWork(
        id = "played-d",
        coverKey = "cover-c",
        genre = PreviewPlayedGenre.MurderMystery,
    ),
)
