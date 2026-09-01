package ai.yoofi.app.ui.gamedetail.character

import ai.yoofi.app.domain.gamedetail.GameCastCharacterTab
import ai.yoofi.app.domain.gamedetail.GetGameCastCharacterUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCastCharacterViewModelTest {

    @Test
    fun `载入金卡后可关注收藏切 Tab 展开简介`() {
        val viewModel = GameCastCharacterViewModel(GetGameCastCharacterUseCase())
        viewModel.load("cast-tomy-player")
        assertEquals("cast-tomy-player", viewModel.uiState.value.character?.id)
        assertEquals("Forbidden Game", viewModel.uiState.value.character?.title)

        viewModel.onIntent(GameCastCharacterIntent.ToggleFollow)
        assertTrue(viewModel.uiState.value.character?.following == true)

        viewModel.onIntent(GameCastCharacterIntent.ToggleFavorite)
        assertTrue(viewModel.uiState.value.character?.favorited == true)

        viewModel.onIntent(GameCastCharacterIntent.SelectTab(GameCastCharacterTab.All))
        assertEquals(GameCastCharacterTab.All, viewModel.uiState.value.character?.tab)

        viewModel.onIntent(GameCastCharacterIntent.ToggleSynopsis)
        assertTrue(viewModel.uiState.value.synopsisExpanded)
        viewModel.onIntent(GameCastCharacterIntent.ToggleSynopsis)
        assertFalse(viewModel.uiState.value.synopsisExpanded)
    }

    @Test
    fun `同一 id 不重复覆盖本地状态`() {
        val viewModel = GameCastCharacterViewModel(GetGameCastCharacterUseCase())
        viewModel.load("cast-sunnme-me")
        viewModel.onIntent(GameCastCharacterIntent.ToggleFollow)
        viewModel.load("cast-sunnme-me")
        assertTrue(viewModel.uiState.value.character?.following == true)
    }
}
