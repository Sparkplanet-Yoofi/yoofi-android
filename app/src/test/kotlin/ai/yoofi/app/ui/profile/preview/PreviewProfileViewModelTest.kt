package ai.yoofi.app.ui.profile.preview

import ai.yoofi.app.domain.profile.GetPreviewPlayedWorksUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewProfileViewModelTest {

    @Test
    fun `初始化带上 Demo 已玩四张`() {
        val viewModel = PreviewProfileViewModel(GetPreviewPlayedWorksUseCase())
        assertEquals(4, viewModel.uiState.value.works.size)
        assertEquals("played-a", viewModel.uiState.value.works.first().id)
    }
}
