package ai.yoofi.app.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChatMentionDraftTest {

    @Test
    fun `空草稿或只有触发符写成 at 加名字`() {
        assertEquals("@tomy ", applyPickedMention("", "tomy"))
        assertEquals("@tomy ", applyPickedMention("@", "tomy"))
    }

    @Test
    fun `句尾触发符被替换而不是名字接到 at 前面`() {
        assertEquals("hello @tomy ", applyPickedMention("hello @", "tomy"))
        assertEquals("@tomy ", applyPickedMention("@to", "tomy"))
    }

    @Test
    fun `点 at 图标时草稿没有触发符则追加`() {
        assertEquals("hello @tomy ", applyPickedMention("hello", "tomy"))
        assertEquals("hello @tomy ", applyPickedMention("hello ", "tomy"))
    }

    @Test
    fun `已经完成的 mention 不会被二次选人吃掉`() {
        assertEquals("@anna @tomy ", applyPickedMention("@anna ", "tomy"))
    }

    @Test
    fun `邮箱中间的 at 不当成 mention`() {
        assertNull(incompleteMentionStart("a@b"))
        assertEquals("a@b @tomy ", applyPickedMention("a@b", "tomy"))
    }
}
