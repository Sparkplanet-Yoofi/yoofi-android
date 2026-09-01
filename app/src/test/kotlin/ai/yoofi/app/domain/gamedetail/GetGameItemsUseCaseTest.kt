package ai.yoofi.app.domain.gamedetail

import org.junit.Assert.assertEquals
import org.junit.Test

class GetGameItemsUseCaseTest {

    @Test
    fun `Demo 四张卡且第一张普通第二张多人`() {
        val items = GetGameItemsUseCase()()
        assertEquals(4, items.size)
        assertEquals(GameItemKind.General, items[0].kind)
        assertEquals(GameItemKind.Multiplayer, items[1].kind)
        assertEquals("knife", items[0].imageKey)
        assertEquals("lollipops", items[1].imageKey)
        assertEquals(99, items[0].quantity)
        assertEquals(29, items[1].quantity)
    }
}

class FormatItemUseMessageTest {

    @Test
    fun `无目标只带道具名`() {
        assertEquals("Used Name.", formatItemUseMessage("Name", emptyList()))
    }

    @Test
    fun `有目标拼上角色名`() {
        assertEquals(
            "Used Name on tomy, Anmi.",
            formatItemUseMessage("Name", listOf("tomy", "Anmi")),
        )
    }
}
