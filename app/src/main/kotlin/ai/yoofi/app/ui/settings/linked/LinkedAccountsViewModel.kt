package ai.yoofi.app.ui.settings.linked

import ai.yoofi.app.core.common.Outcome
import ai.yoofi.app.domain.auth.GetLinkedAccountsUseCase
import ai.yoofi.app.domain.auth.LinkedAccount
import ai.yoofi.app.domain.auth.LinkedAccountProvider
import ai.yoofi.app.domain.auth.UnlinkAccountUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal enum class LinkedAccountsOverlay { None, ConfirmUnlink, LastAccount }

internal enum class LinkedAccountsSnackbar { Unlinked }

internal data class LinkedAccountsUiState(
    val accounts: List<LinkedAccount> = emptyList(),
    val overlay: LinkedAccountsOverlay = LinkedAccountsOverlay.None,
    val pendingProvider: LinkedAccountProvider? = null,
    val snackbar: LinkedAccountsSnackbar? = null,
) {
    val linkedCount: Int get() = accounts.count { it.linked }
}

internal sealed interface LinkedAccountsIntent {
    data class ClickAccount(val provider: LinkedAccountProvider) : LinkedAccountsIntent
    data object ConfirmUnlink : LinkedAccountsIntent
    data object DismissOverlay : LinkedAccountsIntent
    data object ConsumeSnackbar : LinkedAccountsIntent
}

/**
 * 关联账号。双账号点已绑定行出确认弹层；只剩一条时出不可解绑提示。
 * 解绑接口未定，确认后只发 Snackbar，并就地改列表方便对照单账号稿。
 */
@HiltViewModel
internal class LinkedAccountsViewModel @Inject constructor(
    private val getLinkedAccounts: GetLinkedAccountsUseCase,
    private val unlinkAccount: UnlinkAccountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkedAccountsUiState())
    val uiState: StateFlow<LinkedAccountsUiState> = _uiState.asStateFlow()

    fun bind() {
        _uiState.value = LinkedAccountsUiState(accounts = getLinkedAccounts())
    }

    fun onIntent(intent: LinkedAccountsIntent) {
        when (intent) {
            is LinkedAccountsIntent.ClickAccount -> onClickAccount(intent.provider)
            LinkedAccountsIntent.ConfirmUnlink -> confirmUnlink()
            LinkedAccountsIntent.DismissOverlay -> _uiState.update {
                it.copy(overlay = LinkedAccountsOverlay.None, pendingProvider = null)
            }
            LinkedAccountsIntent.ConsumeSnackbar -> _uiState.update { it.copy(snackbar = null) }
        }
    }

    private fun onClickAccount(provider: LinkedAccountProvider) {
        val account = _uiState.value.accounts.find { it.provider == provider } ?: return
        // Link 接口未定，未绑定行先空实现，接 OAuth 时再走绑定。
        if (!account.linked) return
        if (_uiState.value.linkedCount <= 1) {
            _uiState.update {
                it.copy(
                    overlay = LinkedAccountsOverlay.LastAccount,
                    pendingProvider = provider,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    overlay = LinkedAccountsOverlay.ConfirmUnlink,
                    pendingProvider = provider,
                )
            }
        }
    }

    private fun confirmUnlink() {
        val provider = _uiState.value.pendingProvider ?: return
        if (_uiState.value.linkedCount <= 1) return
        viewModelScope.launch {
            when (unlinkAccount(provider)) {
                is Outcome.Ok -> _uiState.update { state ->
                    state.copy(
                        accounts = state.accounts.map { account ->
                            if (account.provider == provider) {
                                account.copy(linked = false)
                            } else {
                                account
                            }
                        },
                        overlay = LinkedAccountsOverlay.None,
                        pendingProvider = null,
                        snackbar = LinkedAccountsSnackbar.Unlinked,
                    )
                }
                is Outcome.Err -> _uiState.update {
                    it.copy(
                        overlay = LinkedAccountsOverlay.None,
                        pendingProvider = null,
                    )
                }
            }
        }
    }
}
