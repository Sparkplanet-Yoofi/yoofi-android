package ai.yoofi.app.domain.chat

import ai.yoofi.app.data.chat.DemoChatRoomRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveChatRoomUseCaseTest {

    @Test
    fun `演示房间含旁白和对白`() {
        val content = ObserveChatRoomUseCase(DemoChatRoomRepository())()
        assertTrue(content.items.any { it is ChatItem.Narrative })
        assertTrue(content.items.any { it is ChatItem.Speech })
        assertEquals("Chapter Title", content.chapterTitle)
    }

    @Test
    fun `选人列表按五人一页对齐 Figma 1 slash 5`() {
        val content = ObserveChatRoomUseCase(DemoChatRoomRepository())()
        assertEquals(25, content.cast.size)
        assertEquals(3, content.inspirations.size)
    }
}
