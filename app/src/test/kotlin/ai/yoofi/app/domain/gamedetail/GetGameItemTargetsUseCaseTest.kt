package ai.yoofi.app.domain.gamedetail

import ai.yoofi.app.domain.chat.ChatCastMember
import ai.yoofi.app.domain.chat.ChatItem
import ai.yoofi.app.domain.chat.ChatRoomContent
import ai.yoofi.app.domain.chat.ChatRoomRepository
import ai.yoofi.app.domain.chat.ObserveChatRoomUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetGameItemTargetsUseCaseTest {

    @Test
    fun `把聊天室 Cast 映射成道具目标`() {
        val targets = GetGameItemTargetsUseCase(ObserveChatRoomUseCase(FakeRepo(cast))).invoke()
        assertEquals(2, targets.size)
        assertEquals("c1", targets[0].id)
        assertEquals("tomy", targets[0].displayName)
        assertEquals("anmi", targets[1].avatarKey)
    }

    @Test
    fun `Cast 为空时目标列表为空`() {
        val targets = GetGameItemTargetsUseCase(
            ObserveChatRoomUseCase(FakeRepo(emptyList())),
        ).invoke()
        assertTrue(targets.isEmpty())
    }
}

private val cast = listOf(
    ChatCastMember("c1", "tomy", "Player identity", "tomy"),
    ChatCastMember("c2", "Anmi", "Identity Tag", "anmi"),
)

private class FakeRepo(
    private val members: List<ChatCastMember>,
) : ChatRoomRepository {
    override fun storyBeat(turn: Int): List<ChatItem> = emptyList()

    override fun current(): ChatRoomContent = ChatRoomContent(
        chapterTitle = "Chapter",
        chapterObjective = "Objective",
        items = emptyList(),
        cast = members,
        inspirations = emptyList(),
    )
}
