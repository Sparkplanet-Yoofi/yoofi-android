package ai.yoofi.app.ui.settings.linked

import ai.yoofi.app.domain.auth.GetLinkedAccountsUseCase
import ai.yoofi.app.domain.auth.LinkedAccountProvider
import ai.yoofi.app.domain.auth.UnlinkAccountUseCase
import ai.yoofi.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LinkedAccountsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `双账号点已绑定行出确认弹层`() {
        val viewModel = viewModel()
        viewModel.onIntent(LinkedAccountsIntent.ClickAccount(LinkedAccountProvider.Google))
        assertEquals(LinkedAccountsOverlay.ConfirmUnlink, viewModel.uiState.value.overlay)
        assertEquals(LinkedAccountProvider.Google, viewModel.uiState.value.pendingProvider)
    }

    @Test
    fun `未绑定行点击不弹层`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        unlinkGoogle(viewModel)
        runCurrent()
        viewModel.onIntent(LinkedAccountsIntent.ClickAccount(LinkedAccountProvider.Google))
        assertEquals(LinkedAccountsOverlay.None, viewModel.uiState.value.overlay)
    }

    @Test
    fun `确认解绑后发 Snackbar 并变成单账号`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(LinkedAccountsIntent.ClickAccount(LinkedAccountProvider.Apple))
        viewModel.onIntent(LinkedAccountsIntent.ConfirmUnlink)
        runCurrent()
        val state = viewModel.uiState.value
        assertEquals(LinkedAccountsOverlay.None, state.overlay)
        assertEquals(LinkedAccountsSnackbar.Unlinked, state.snackbar)
        assertEquals(1, state.linkedCount)
        assertFalse(state.accounts.first { it.provider == LinkedAccountProvider.Apple }.linked)
    }

    @Test
    fun `只剩一条时点已绑定行出不可解绑提示`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        unlinkGoogle(viewModel)
        runCurrent()
        viewModel.onIntent(LinkedAccountsIntent.ClickAccount(LinkedAccountProvider.Apple))
        assertEquals(LinkedAccountsOverlay.LastAccount, viewModel.uiState.value.overlay)
    }

    @Test
    fun `只剩一条时确认解绑不会改列表`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        unlinkGoogle(viewModel)
        runCurrent()
        viewModel.onIntent(LinkedAccountsIntent.ConsumeSnackbar)
        viewModel.onIntent(LinkedAccountsIntent.ClickAccount(LinkedAccountProvider.Apple))
        viewModel.onIntent(LinkedAccountsIntent.ConfirmUnlink)
        runCurrent()
        assertEquals(1, viewModel.uiState.value.linkedCount)
        assertNull(viewModel.uiState.value.snackbar)
    }

    @Test
    fun `取消弹层不清绑定`() {
        val viewModel = viewModel()
        viewModel.onIntent(LinkedAccountsIntent.ClickAccount(LinkedAccountProvider.Google))
        viewModel.onIntent(LinkedAccountsIntent.DismissOverlay)
        assertEquals(LinkedAccountsOverlay.None, viewModel.uiState.value.overlay)
        assertEquals(2, viewModel.uiState.value.linkedCount)
        assertTrue(viewModel.uiState.value.accounts.all { it.linked })
    }

    private fun unlinkGoogle(viewModel: LinkedAccountsViewModel) {
        viewModel.onIntent(LinkedAccountsIntent.ClickAccount(LinkedAccountProvider.Google))
        viewModel.onIntent(LinkedAccountsIntent.ConfirmUnlink)
    }

    private fun viewModel(): LinkedAccountsViewModel {
        val viewModel = LinkedAccountsViewModel(
            getLinkedAccounts = GetLinkedAccountsUseCase(),
            unlinkAccount = UnlinkAccountUseCase(),
        )
        viewModel.bind()
        return viewModel
    }
}
