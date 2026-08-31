package ai.yoofi.app.ui.settings

import ai.yoofi.app.domain.auth.LogoutUseCase
import ai.yoofi.app.testing.FakeUserSessionStore
import ai.yoofi.app.testing.fakeSession
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {

    @Test
    fun `点 Log Out 先出确认弹层`() {
        val viewModel = viewModel()
        viewModel.onIntent(SettingsIntent.RequestLogout)
        assertTrue(viewModel.uiState.value.logoutConfirm)
    }

    @Test
    fun `取消弹层不退登录`() {
        val store = FakeUserSessionStore().also { it.save(fakeSession()) }
        var signedOut = false
        val viewModel = viewModel(store) { signedOut = true }
        viewModel.onIntent(SettingsIntent.RequestLogout)
        viewModel.onIntent(SettingsIntent.DismissLogout)
        assertFalse(viewModel.uiState.value.logoutConfirm)
        assertFalse(signedOut)
        assertTrue(store.currentSession() != null)
    }

    @Test
    fun `确认退出会清会话并回登录`() {
        val store = FakeUserSessionStore().also { it.save(fakeSession()) }
        var signedOut = false
        val viewModel = viewModel(store) { signedOut = true }
        viewModel.onIntent(SettingsIntent.ConfirmLogout)
        assertTrue(signedOut)
        assertNull(store.currentSession())
    }

    private fun viewModel(
        store: FakeUserSessionStore = FakeUserSessionStore(),
        onSignedOut: () -> Unit = {},
    ): SettingsViewModel {
        val viewModel = SettingsViewModel(LogoutUseCase(store))
        viewModel.bind(onSignedOut)
        return viewModel
    }
}
