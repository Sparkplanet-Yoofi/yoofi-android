package ai.yoofi.app.ui.auth

import ai.yoofi.app.domain.auth.VerifyEmailCodeResult
import ai.yoofi.app.domain.auth.VerifyEmailCodeUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isVerifying: Boolean = false,
    val codeError: Boolean = false,
)

sealed interface AuthIntent {
    data object ClearCodeError : AuthIntent
    data class VerifyCode(val email: String, val code: String) : AuthIntent
}

sealed interface AuthSideEffect {
    data object OpenProfileSetup : AuthSideEffect
    data object OpenHome : AuthSideEffect
}

@HiltViewModel
internal class AuthViewModel @Inject constructor(
    private val verifyEmailCode: VerifyEmailCodeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<AuthSideEffect>(capacity = Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            AuthIntent.ClearCodeError -> {
                _uiState.update { it.copy(codeError = false) }
            }
            is AuthIntent.VerifyCode -> verify(intent.email, intent.code)
        }
    }

    private fun verify(email: String, code: String) {
        if (_uiState.value.isVerifying) return
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, codeError = false) }
            when (val result = verifyEmailCode(email, code)) {
                VerifyEmailCodeResult.InvalidCode -> {
                    _uiState.update { it.copy(isVerifying = false, codeError = true) }
                }
                is VerifyEmailCodeResult.Success -> {
                    _uiState.update { it.copy(isVerifying = false) }
                    val effect = if (result.isNewUser) {
                        AuthSideEffect.OpenProfileSetup
                    } else {
                        AuthSideEffect.OpenHome
                    }
                    _sideEffect.send(effect)
                }
            }
        }
    }
}
