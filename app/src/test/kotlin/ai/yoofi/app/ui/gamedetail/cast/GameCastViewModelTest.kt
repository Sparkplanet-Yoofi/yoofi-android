package ai.yoofi.app.ui.gamedetail.cast

import ai.yoofi.app.domain.gamedetail.GetGameCastCardsUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class GameCastViewModelTest {

    @Test
    fun `初始化带上 Demo 六张人物卡`() {
        val viewModel = GameCastViewModel(GetGameCastCardsUseCase())
        assertEquals(6, viewModel.uiState.value.cards.size)
        assertEquals("cast-sunnme-me", viewModel.uiState.value.cards.first().id)
    }
}
