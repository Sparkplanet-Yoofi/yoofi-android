package ai.yoofi.app.domain.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class GetPreviewPlayedWorksUseCaseTest {

    @Test
    fun `Demo 默认四张且类型与稿面一致`() {
        val works = GetPreviewPlayedWorksUseCase()()
        assertEquals(4, works.size)
        assertEquals(
            listOf(
                PreviewPlayedGenre.IndieGames,
                PreviewPlayedGenre.MurderMystery,
                PreviewPlayedGenre.IndieGames,
                PreviewPlayedGenre.MurderMystery,
            ),
            works.map { it.genre },
        )
        assertEquals(
            listOf("cover-e", "cover-d", "cover-a", "cover-c"),
            works.map { it.coverKey },
        )
    }
}
