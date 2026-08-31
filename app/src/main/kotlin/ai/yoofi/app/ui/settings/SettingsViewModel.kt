package ai.yoofi.app.ui.settings

import ai.yoofi.app.domain.auth.LogoutUseCase
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal data class SettingsUiState(
    val logoutConfirm: Boolean = false,
)

internal sealed interface SettingsIntent {
    data object RequestLogout : SettingsIntent
    data object DismissLogout : SettingsIntent
    data object ConfirmLogout : SettingsIntent
}

/**
 * 设置页管家：只负责列表态与登出弹层。
 * 注销账号是独立 Screen，不要把删号表单塞进来。
 */
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val logout: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var onSignedOut: () -> Unit = {}

    fun bind(onSignedOut: () -> Unit) {
        this.onSignedOut = onSignedOut
        _uiState.value = SettingsUiState()
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.RequestLogout -> _uiState.update { it.copy(logoutConfirm = true) }
            SettingsIntent.DismissLogout -> _uiState.update { it.copy(logoutConfirm = false) }
            SettingsIntent.ConfirmLogout -> {
                logout()
                onSignedOut()
            }
        }
    }
}
