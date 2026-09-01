package ai.yoofi.app.ui.settings.delete

import ai.yoofi.shared.common.Outcome
import ai.yoofi.app.domain.auth.DeleteAccountProof
import ai.yoofi.app.domain.auth.DeleteAccountUseCase
import ai.yoofi.app.domain.auth.DeleteConfirmPhrase
import ai.yoofi.app.domain.auth.SendDeleteCodeUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val DeleteRedirectSeconds = 3
internal const val DeleteCodeCooldownSeconds = 60

internal enum class DeleteAccountStep { Warning, Password, Email, Done }

internal data class DeleteAccountUiState(
    val step: DeleteAccountStep = DeleteAccountStep.Warning,
    val acknowledged: Boolean = false,
    val password: String = "",
    val passwordVisible: Boolean = false,
    val passwordPhrase: String = "",
    val emailCode: String = "",
    val emailPhrase: String = "",
    val codeCooldownSec: Int = 0,
    val sendingCode: Boolean = false,
    val submitting: Boolean = false,
    val redirectSeconds: Int = DeleteRedirectSeconds,
) {
    val canGoNext: Boolean get() = acknowledged
    val canConfirmPassword: Boolean
        get() = password.isNotBlank() &&
            passwordPhrase == DeleteConfirmPhrase &&
            !submitting
    val canConfirmEmail: Boolean
        get() = emailCode.isNotBlank() &&
            emailPhrase == DeleteConfirmPhrase &&
            !submitting
    val canSendCode: Boolean get() = codeCooldownSec == 0 && !sendingCode
}

internal sealed interface DeleteAccountIntent {
    data object ToggleAck : DeleteAccountIntent
    data object Next : DeleteAccountIntent
    data class PasswordChanged(val value: String) : DeleteAccountIntent
    data object TogglePasswordVisible : DeleteAccountIntent
    data class PasswordPhraseChanged(val value: String) : DeleteAccountIntent
    data object ConfirmPassword : DeleteAccountIntent
    data class EmailCodeChanged(val value: String) : DeleteAccountIntent
    data class EmailPhraseChanged(val value: String) : DeleteAccountIntent
    data object SendCode : DeleteAccountIntent
    data object ConfirmEmail : DeleteAccountIntent
    data object Back : DeleteAccountIntent
    data object Cancel : DeleteAccountIntent
}

/**
 * 注销四步：警告 → 有密确认 → 无密确认 → 成功倒计时。
 * Figma 把有密 / 无密画成并列稿，产品点选路径要求两页都能走到。
 */
@HiltViewModel
internal class DeleteAccountViewModel @Inject constructor(
    private val deleteAccount: DeleteAccountUseCase,
    private val sendDeleteCode: SendDeleteCodeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeleteAccountUiState())
    val uiState: StateFlow<DeleteAccountUiState> = _uiState.asStateFlow()

    private var onClose: () -> Unit = {}
    private var onDeleted: () -> Unit = {}
    private var redirectJob: Job? = null
    private var cooldownJob: Job? = null

    fun bind(onClose: () -> Unit, onDeleted: () -> Unit) {
        this.onClose = onClose
        this.onDeleted = onDeleted
        redirectJob?.cancel()
        cooldownJob?.cancel()
        _uiState.value = DeleteAccountUiState()
    }

    fun onIntent(intent: DeleteAccountIntent) {
        when (intent) {
            DeleteAccountIntent.ToggleAck -> _uiState.update {
                it.copy(acknowledged = !it.acknowledged)
            }
            DeleteAccountIntent.Next -> {
                if (!_uiState.value.canGoNext) return
                _uiState.update { it.copy(step = DeleteAccountStep.Password) }
            }
            is DeleteAccountIntent.PasswordChanged -> _uiState.update {
                it.copy(password = intent.value)
            }
            DeleteAccountIntent.TogglePasswordVisible -> _uiState.update {
                it.copy(passwordVisible = !it.passwordVisible)
            }
            is DeleteAccountIntent.PasswordPhraseChanged -> _uiState.update {
                it.copy(passwordPhrase = intent.value)
            }
            DeleteAccountIntent.ConfirmPassword -> {
                if (!_uiState.value.canConfirmPassword) return
                _uiState.update { it.copy(step = DeleteAccountStep.Email) }
            }
            is DeleteAccountIntent.EmailCodeChanged -> _uiState.update {
                it.copy(emailCode = intent.value)
            }
            is DeleteAccountIntent.EmailPhraseChanged -> _uiState.update {
                it.copy(emailPhrase = intent.value)
            }
            DeleteAccountIntent.SendCode -> sendCode()
            DeleteAccountIntent.ConfirmEmail -> confirmDelete()
            DeleteAccountIntent.Back -> onBack()
            DeleteAccountIntent.Cancel -> onClose()
        }
    }

    private fun onBack() {
        when (_uiState.value.step) {
            DeleteAccountStep.Warning -> onClose()
            DeleteAccountStep.Password -> _uiState.update {
                it.copy(step = DeleteAccountStep.Warning)
            }
            DeleteAccountStep.Email -> _uiState.update {
                it.copy(step = DeleteAccountStep.Password)
            }
            DeleteAccountStep.Done -> finishDeleted()
        }
    }

    private fun sendCode() {
        val state = _uiState.value
        if (!state.canSendCode) return
        viewModelScope.launch {
            _uiState.update { it.copy(sendingCode = true) }
            when (sendDeleteCode()) {
                is Outcome.Ok -> {
                    _uiState.update { it.copy(sendingCode = false) }
                    startCooldown()
                }
                is Outcome.Err -> _uiState.update { it.copy(sendingCode = false) }
            }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _uiState.update { it.copy(codeCooldownSec = DeleteCodeCooldownSeconds) }
            while (_uiState.value.codeCooldownSec > 0) {
                delay(1_000)
                _uiState.update { it.copy(codeCooldownSec = it.codeCooldownSec - 1) }
            }
        }
    }

    private fun confirmDelete() {
        val state = _uiState.value
        if (!state.canConfirmEmail) return
        viewModelScope.launch {
            _uiState.update { it.copy(submitting = true) }
            when (
                deleteAccount(
                    DeleteAccountProof.EmailCode(
                        code = state.emailCode,
                        phrase = state.emailPhrase,
                    ),
                )
            ) {
                is Outcome.Ok -> {
                    _uiState.update {
                        it.copy(
                            submitting = false,
                            step = DeleteAccountStep.Done,
                            redirectSeconds = DeleteRedirectSeconds,
                        )
                    }
                    startRedirect()
                }
                is Outcome.Err -> _uiState.update { it.copy(submitting = false) }
            }
        }
    }

    private fun startRedirect() {
        redirectJob?.cancel()
        redirectJob = viewModelScope.launch {
            while (_uiState.value.redirectSeconds > 0) {
                delay(1_000)
                val next = _uiState.value.redirectSeconds - 1
                _uiState.update { it.copy(redirectSeconds = next) }
                if (next == 0) {
                    finishDeleted()
                    return@launch
                }
            }
        }
    }

    private fun finishDeleted() {
        redirectJob?.cancel()
        onDeleted()
    }
}
