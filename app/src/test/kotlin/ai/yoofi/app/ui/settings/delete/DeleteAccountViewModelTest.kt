package ai.yoofi.app.ui.settings.delete

import ai.yoofi.app.domain.auth.DeleteAccountUseCase
import ai.yoofi.app.domain.auth.DeleteConfirmPhrase
import ai.yoofi.app.domain.auth.SendDeleteCodeUseCase
import ai.yoofi.app.testing.FakeUserSessionStore
import ai.yoofi.app.testing.MainDispatcherRule
import ai.yoofi.app.testing.fakeSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `未勾选不能进入下一步`() {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.canGoNext)
        viewModel.onIntent(DeleteAccountIntent.Next)
        assertEquals(DeleteAccountStep.Warning, viewModel.uiState.value.step)
    }

    @Test
    fun `勾选后 Next 进入有密确认页`() {
        val viewModel = viewModel()
        viewModel.onIntent(DeleteAccountIntent.ToggleAck)
        assertTrue(viewModel.uiState.value.canGoNext)
        viewModel.onIntent(DeleteAccountIntent.Next)
        assertEquals(DeleteAccountStep.Password, viewModel.uiState.value.step)
    }

    @Test
    fun `密码或短语不全不能确认`() {
        val viewModel = viewModel()
        goToPassword(viewModel)
        viewModel.onIntent(DeleteAccountIntent.PasswordChanged("secret"))
        assertFalse(viewModel.uiState.value.canConfirmPassword)
        viewModel.onIntent(DeleteAccountIntent.ConfirmPassword)
        assertEquals(DeleteAccountStep.Password, viewModel.uiState.value.step)
    }

    @Test
    fun `有密确认后进入无密确认页`() {
        val viewModel = viewModel()
        goToPassword(viewModel)
        viewModel.onIntent(DeleteAccountIntent.PasswordChanged("secret"))
        viewModel.onIntent(DeleteAccountIntent.PasswordPhraseChanged(DeleteConfirmPhrase))
        assertTrue(viewModel.uiState.value.canConfirmPassword)
        viewModel.onIntent(DeleteAccountIntent.ConfirmPassword)
        assertEquals(DeleteAccountStep.Email, viewModel.uiState.value.step)
    }

    @Test
    fun `无密确认成功进入倒计时并清会话`() = runTest(mainDispatcherRule.dispatcher) {
        val store = FakeUserSessionStore().also { it.save(fakeSession()) }
        var deleted = false
        val viewModel = viewModel(store = store, onDeleted = { deleted = true })
        goToEmail(viewModel)
        viewModel.onIntent(DeleteAccountIntent.EmailCodeChanged("123456"))
        viewModel.onIntent(DeleteAccountIntent.EmailPhraseChanged(DeleteConfirmPhrase))
        viewModel.onIntent(DeleteAccountIntent.ConfirmEmail)
        // 只跑当前时刻的任务，不要 advanceUntilIdle，否则 3 秒倒计时会被一次性推完
        runCurrent()
        assertEquals(DeleteAccountStep.Done, viewModel.uiState.value.step)
        assertNull(store.currentSession())
        assertFalse(deleted)
        advanceTimeBy(3_000)
        runCurrent()
        assertTrue(deleted)
    }

    @Test
    fun `取消只关页不删号`() {
        val store = FakeUserSessionStore().also { it.save(fakeSession()) }
        var closed = false
        var deleted = false
        val viewModel = viewModel(
            store = store,
            onClose = { closed = true },
            onDeleted = { deleted = true },
        )
        viewModel.onIntent(DeleteAccountIntent.Cancel)
        assertTrue(closed)
        assertFalse(deleted)
        assertTrue(store.currentSession() != null)
    }

    @Test
    fun `有密页返回回到警告页`() {
        val viewModel = viewModel()
        goToPassword(viewModel)
        viewModel.onIntent(DeleteAccountIntent.Back)
        assertEquals(DeleteAccountStep.Warning, viewModel.uiState.value.step)
    }

    private fun goToPassword(viewModel: DeleteAccountViewModel) {
        viewModel.onIntent(DeleteAccountIntent.ToggleAck)
        viewModel.onIntent(DeleteAccountIntent.Next)
    }

    private fun goToEmail(viewModel: DeleteAccountViewModel) {
        goToPassword(viewModel)
        viewModel.onIntent(DeleteAccountIntent.PasswordChanged("secret"))
        viewModel.onIntent(DeleteAccountIntent.PasswordPhraseChanged(DeleteConfirmPhrase))
        viewModel.onIntent(DeleteAccountIntent.ConfirmPassword)
    }

    private fun viewModel(
        store: FakeUserSessionStore = FakeUserSessionStore(),
        onClose: () -> Unit = {},
        onDeleted: () -> Unit = {},
    ): DeleteAccountViewModel {
        val viewModel = DeleteAccountViewModel(
            deleteAccount = DeleteAccountUseCase(store),
            sendDeleteCode = SendDeleteCodeUseCase(),
        )
        viewModel.bind(onClose = onClose, onDeleted = onDeleted)
        return viewModel
    }
}
