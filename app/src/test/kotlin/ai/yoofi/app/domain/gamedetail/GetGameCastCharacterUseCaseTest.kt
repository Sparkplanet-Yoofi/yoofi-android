package ai.yoofi.app.domain.gamedetail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GetGameCastCharacterUseCaseTest {

    @Test
    fun `金卡返回稿面 Demo 且空槽为 null`() {
        val useCase = GetGameCastCharacterUseCase()
        val detail = useCase("cast-sunnme-me")
        assertNotNull(detail)
        assertEquals("Forbidden Game", detail?.title)
        assertEquals(GameCastCharacterTab.MyCreations, detail?.tab)
        assertEquals("cast-sunnme-me", detail?.id)
        assertNull(useCase("cast-empty-1"))
        assertNull(useCase("unknown"))
    }
}
