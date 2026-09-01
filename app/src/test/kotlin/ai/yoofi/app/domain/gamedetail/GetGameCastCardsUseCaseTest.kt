package ai.yoofi.app.domain.gamedetail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetGameCastCardsUseCaseTest {

    @Test
    fun `Demo 四张金卡加两个空槽且角色与稿面一致`() {
        val cards = GetGameCastCardsUseCase()()
        assertEquals(6, cards.size)
        assertEquals(
            listOf("sunnme", "TOMY", "sunnme", "TOMY"),
            cards.take(4).map { it.name },
        )
        assertEquals(GameCastRole.Me, cards.first().role)
        assertEquals(GameCastRole.PlayerRole, cards[1].role)
        assertNull(cards[4].portraitKey)
        assertNull(cards[5].name)
    }
}
