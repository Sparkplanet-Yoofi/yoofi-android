package ai.yoofi.app.ui.settings.blocked

import ai.yoofi.app.domain.block.GetBlockedUsersUseCase
import ai.yoofi.app.domain.block.UnblockUserUseCase
import ai.yoofi.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BlockedUsersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `点 Unblock 出确认弹层`() {
        val viewModel = viewModel()
        viewModel.onIntent(BlockedUsersIntent.RequestUnblock("blocked-jenny"))
        assertEquals("Jenny", viewModel.uiState.value.pendingUser?.displayName)
    }

    @Test
    fun `未知 id 不弹层`() {
        val viewModel = viewModel()
        viewModel.onIntent(BlockedUsersIntent.RequestUnblock("missing"))
        assertNull(viewModel.uiState.value.pendingUser)
    }

    @Test
    fun `确认解除后删行并发 Toast`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(BlockedUsersIntent.RequestUnblock("blocked-jenny"))
        viewModel.onIntent(BlockedUsersIntent.ConfirmUnblock)
        runCurrent()
        val state = viewModel.uiState.value
        assertNull(state.pendingUser)
        assertEquals(BlockedUsersSnackbar.Unblocked, state.snackbar)
        assertEquals(4, state.users.size)
        assertEquals("Lopez", state.users.first().displayName)
    }

    @Test
    fun `取消弹层不改列表`() {
        val viewModel = viewModel()
        viewModel.onIntent(BlockedUsersIntent.RequestUnblock("blocked-jenny"))
        viewModel.onIntent(BlockedUsersIntent.DismissOverlay)
        assertNull(viewModel.uiState.value.pendingUser)
        assertEquals(5, viewModel.uiState.value.users.size)
    }

    private fun viewModel(): BlockedUsersViewModel {
        val viewModel = BlockedUsersViewModel(
            getBlockedUsers = GetBlockedUsersUseCase(),
            unblockUser = UnblockUserUseCase(),
        )
        viewModel.bind()
        return viewModel
    }
}
