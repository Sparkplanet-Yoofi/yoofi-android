package ai.yoofi.app.ui.chat

import ai.yoofi.app.domain.chat.AdvanceChatStoryUseCase
import ai.yoofi.app.domain.chat.ChatCastMember
import ai.yoofi.app.domain.chat.ChatEvent
import ai.yoofi.app.domain.chat.ChatEventKind
import ai.yoofi.app.domain.chat.ChatItem
import ai.yoofi.app.domain.chat.ChatRoomContent
import ai.yoofi.app.domain.chat.ChatRoomRepository
import ai.yoofi.app.domain.chat.ObserveChatRoomUseCase
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRoomViewModelTest {

    @Test
    fun `打开 Cast 再关闭回到无弹层`() {
        val viewModel = viewModel()
        viewModel.onIntent(ChatRoomIntent.OpenCast)
        assertEquals(ChatRoomOverlay.Cast, viewModel.uiState.value.overlay)
        viewModel.onIntent(ChatRoomIntent.DismissOverlay)
        assertEquals(ChatRoomOverlay.None, viewModel.uiState.value.overlay)
    }

    @Test
    fun `选人后草稿带 at 前缀`() {
        val viewModel = viewModel()
        viewModel.onIntent(ChatRoomIntent.PickMention("c1"))
        assertTrue(viewModel.uiState.value.draft.startsWith("@tomy"))
        assertEquals(ChatRoomOverlay.None, viewModel.uiState.value.overlay)
    }

    @Test
    fun `Continue 先追加玩家气泡再追加本轮剧情`() {
        val viewModel = viewModel()
        viewModel.onIntent(ChatRoomIntent.DraftChanged("hello"))
        viewModel.onIntent(ChatRoomIntent.ContinueStory)
        val items = viewModel.uiState.value.items
        val player = items[items.lastIndex - 1]
        assertTrue(player is ChatItem.Player)
        assertEquals("hello", (player as ChatItem.Player).body)
        assertEquals("beat-0", items.last().id)
        assertEquals("", viewModel.uiState.value.draft)
    }

    @Test
    fun `草稿为空时 Continue 仍推进剧情且轮次递增`() {
        val viewModel = viewModel()
        val before = viewModel.uiState.value.items.size
        viewModel.onIntent(ChatRoomIntent.ContinueStory)
        viewModel.onIntent(ChatRoomIntent.ContinueStory)
        val items = viewModel.uiState.value.items
        assertEquals(before + 2, items.size)
        assertEquals("beat-0", items[items.lastIndex - 1].id)
        assertEquals("beat-1", items.last().id)
    }

    @Test
    fun `音量图标可在静音与放音之间切换`() {
        val viewModel = viewModel()
        assertEquals(false, viewModel.uiState.value.volumeMuted)
        viewModel.onIntent(ChatRoomIntent.ToggleVolume)
        assertEquals(true, viewModel.uiState.value.volumeMuted)
        viewModel.onIntent(ChatRoomIntent.ToggleVolume)
        assertEquals(false, viewModel.uiState.value.volumeMuted)
    }
}

class ChatItemSpacingTest {

    private val narrative = ChatItem.Narrative(id = "n", body = "story")
    private val events = ChatItem.Events(
        id = "e",
        events = listOf(
            ChatEvent(id = "e1", kind = ChatEventKind.ItemAcquired, subject = "Untitled Diary"),
        ),
    )

    @Test
    fun `事件提示与相邻内容留 12dp`() {
        assertEquals(12.dp, chatItemSpacing(narrative, events))
        assertEquals(12.dp, chatItemSpacing(events, narrative))
    }

    @Test
    fun `普通内容之间留 16dp`() {
        assertEquals(16.dp, chatItemSpacing(narrative, narrative))
    }
}

private fun viewModel(): ChatRoomViewModel {
    val repo = FakeChatRoomRepository()
    return ChatRoomViewModel(ObserveChatRoomUseCase(repo), AdvanceChatStoryUseCase(repo))
}

private class FakeChatRoomRepository : ChatRoomRepository {
    override fun storyBeat(turn: Int): List<ChatItem> =
        listOf(ChatItem.Narrative(id = "beat-$turn", body = "beat $turn"))

    override fun current(): ChatRoomContent = ChatRoomContent(
        chapterTitle = "Chapter Title",
        chapterObjective = "Chapter Objective",
        items = listOf(ChatItem.Narrative(id = "n1", body = "story")),
        cast = listOf(
            ChatCastMember(
                id = "c1",
                displayName = "tomy",
                identity = "Player identity",
                avatarKey = "tomy",
            ),
        ),
        inspirations = listOf("frozen for a single heartbeat."),
    )
}
