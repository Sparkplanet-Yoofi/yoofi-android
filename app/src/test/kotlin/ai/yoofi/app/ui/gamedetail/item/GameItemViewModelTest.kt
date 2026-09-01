package ai.yoofi.app.ui.gamedetail.item

import ai.yoofi.app.domain.chat.ChatCastMember
import ai.yoofi.app.domain.chat.ChatItem
import ai.yoofi.app.domain.chat.ChatRoomContent
import ai.yoofi.app.domain.chat.ChatRoomRepository
import ai.yoofi.app.domain.chat.ObserveChatRoomUseCase
import ai.yoofi.app.domain.gamedetail.GameItemKind
import ai.yoofi.app.domain.gamedetail.GetGameItemTargetsUseCase
import ai.yoofi.app.domain.gamedetail.GetGameItemsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameItemViewModelTest {

    @Test
    fun `点第一张卡打开普通道具底栏`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameItemIntent.OpenItem("item-knife"))
        val state = viewModel.uiState.value
        assertTrue(state.sheetOpen)
        assertEquals("item-knife", state.selectedItemId)
        assertEquals(GameItemKind.General, state.selectedItem?.kind)
        assertFalse(state.targetOpen)
    }

    @Test
    fun `点第二张卡打开多人道具底栏`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameItemIntent.OpenItem("item-lollipops"))
        val state = viewModel.uiState.value
        assertTrue(state.sheetOpen)
        assertEquals(GameItemKind.Multiplayer, state.selectedItem?.kind)
    }

    @Test
    fun `普通道具也走选人后再发聊天文案`() = runTest {
        val viewModel = viewModel()
        val effect = async { viewModel.sideEffect.first() }
        viewModel.onIntent(GameItemIntent.OpenItem("item-knife"))
        viewModel.onIntent(GameItemIntent.OpenTargets)
        assertTrue(viewModel.uiState.value.targetOpen)
        viewModel.onIntent(GameItemIntent.ToggleTarget("c1"))
        viewModel.onIntent(GameItemIntent.ConfirmUse)
        assertEquals(
            GameItemSideEffect.SendToChat("Used Name on tomy."),
            effect.await(),
        )
        assertFalse(viewModel.uiState.value.targetOpen)
    }

    @Test
    fun `未选人时确认使用不发消息`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(GameItemIntent.OpenItem("item-lollipops"))
        viewModel.onIntent(GameItemIntent.OpenTargets)
        viewModel.onIntent(GameItemIntent.ConfirmUse)
        assertTrue(viewModel.uiState.value.targetOpen)
        assertTrue(viewModel.uiState.value.selectedTargetIds.isEmpty())
    }

    @Test
    fun `全选后再用发出带角色名的文案`() = runTest {
        val viewModel = viewModel()
        val effect = async { viewModel.sideEffect.first() }
        viewModel.onIntent(GameItemIntent.OpenItem("item-lollipops"))
        viewModel.onIntent(GameItemIntent.OpenTargets)
        viewModel.onIntent(GameItemIntent.ToggleSelectAll)
        assertTrue(viewModel.uiState.value.allTargetsSelected)
        viewModel.onIntent(GameItemIntent.ConfirmUse)
        assertEquals(
            GameItemSideEffect.SendToChat("Used Name on tomy, Anmi."),
            effect.await(),
        )
    }

    @Test
    fun `切另一张卡直接换选不先清空`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameItemIntent.OpenItem("item-knife"))
        viewModel.onIntent(GameItemIntent.OpenItem("item-lollipops"))
        val state = viewModel.uiState.value
        assertEquals("item-lollipops", state.selectedItemId)
        assertTrue(state.sheetOpen)
        assertFalse(state.targetOpen)
    }

    @Test
    fun `ShowList 从选人页回到道具列表`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameItemIntent.OpenItem("item-lollipops"))
        viewModel.onIntent(GameItemIntent.OpenTargets)
        assertTrue(viewModel.uiState.value.targetOpen)
        viewModel.onIntent(GameItemIntent.ShowList)
        val state = viewModel.uiState.value
        assertFalse(state.targetOpen)
        assertFalse(state.sheetOpen)
        assertEquals("", state.selectedItemId)
    }

    @Test
    fun `预览开关不影响已选道具`() {
        val viewModel = viewModel()
        viewModel.onIntent(GameItemIntent.OpenItem("item-knife"))
        viewModel.onIntent(GameItemIntent.OpenPreview)
        assertTrue(viewModel.uiState.value.previewOpen)
        viewModel.onIntent(GameItemIntent.ClosePreview)
        assertFalse(viewModel.uiState.value.previewOpen)
        assertTrue(viewModel.uiState.value.sheetOpen)
    }
}

private fun viewModel(): GameItemViewModel {
    val observe = ObserveChatRoomUseCase(object : ChatRoomRepository {
        override fun storyBeat(turn: Int): List<ChatItem> = emptyList()

        override fun current(): ChatRoomContent = ChatRoomContent(
            chapterTitle = "Chapter",
            chapterObjective = "Objective",
            items = emptyList(),
            cast = listOf(
                ChatCastMember("c1", "tomy", "Player identity", "tomy"),
                ChatCastMember("c2", "Anmi", "Identity Tag", "anmi"),
            ),
            inspirations = emptyList(),
        )
    })
    return GameItemViewModel(GetGameItemsUseCase(), GetGameItemTargetsUseCase(observe))
}
