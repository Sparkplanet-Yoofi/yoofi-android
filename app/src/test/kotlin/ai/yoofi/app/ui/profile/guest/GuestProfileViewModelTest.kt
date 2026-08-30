package ai.yoofi.app.ui.profile.guest

import ai.yoofi.app.ui.profile.GuestProfileTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuestProfileViewModelTest {

    @Test
    fun `三点菜单再取消回到无弹层`() {
        val viewModel = viewModel()
        viewModel.onIntent(GuestProfileIntent.OpenMenu)
        assertEquals(GuestProfileOverlay.Menu, viewModel.uiState.value.overlay)
        viewModel.onIntent(GuestProfileIntent.DismissOverlay)
        assertEquals(GuestProfileOverlay.None, viewModel.uiState.value.overlay)
    }

    @Test
    fun `菜单点拉黑进入确认弹窗`() {
        val viewModel = viewModel()
        viewModel.onIntent(GuestProfileIntent.OpenMenu)
        viewModel.onIntent(GuestProfileIntent.RequestBlock)
        assertEquals(GuestProfileOverlay.ConfirmBlock, viewModel.uiState.value.overlay)
    }

    @Test
    fun `确认拉黑关闭弹层并留下 Snackbar 占位`() {
        val viewModel = viewModel()
        viewModel.onIntent(GuestProfileIntent.RequestBlock)
        viewModel.onIntent(GuestProfileIntent.ConfirmBlock)
        assertEquals(GuestProfileOverlay.None, viewModel.uiState.value.overlay)
        assertEquals(GuestSnackbar.BlockUser, viewModel.uiState.value.snackbar)
        viewModel.onIntent(GuestProfileIntent.ConsumeSnackbar)
        assertNull(viewModel.uiState.value.snackbar)
    }

    @Test
    fun `关注钮只翻转本地 following`() {
        val viewModel = viewModel()
        assertTrue(!viewModel.uiState.value.following)
        viewModel.onIntent(GuestProfileIntent.ToggleFollow)
        assertTrue(viewModel.uiState.value.following)
    }

    private fun viewModel(): GuestProfileViewModel {
        val viewModel = GuestProfileViewModel()
        viewModel.bind(
            GuestProfileTarget(
                userId = "author-anmi",
                displayName = "Jenny",
                avatarKey = "avatar-author",
            ),
        )
        return viewModel
    }
}
