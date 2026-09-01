package ai.yoofi.app.ui.settings.blocked

import ai.yoofi.shared.common.Outcome
import ai.yoofi.app.domain.block.BlockedUser
import ai.yoofi.app.domain.block.GetBlockedUsersUseCase
import ai.yoofi.app.domain.block.UnblockUserUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal enum class BlockedUsersSnackbar { Unblocked }

internal data class BlockedUsersUiState(
    val users: List<BlockedUser> = emptyList(),
    val pendingUser: BlockedUser? = null,
    val snackbar: BlockedUsersSnackbar? = null,
)

internal sealed interface BlockedUsersIntent {
    data class RequestUnblock(val userId: String) : BlockedUsersIntent
    data object ConfirmUnblock : BlockedUsersIntent
    data object DismissOverlay : BlockedUsersIntent
    data object ConsumeSnackbar : BlockedUsersIntent
}

/**
 * 黑名单。点 Unblock 出确认弹层；确认后接口未定只发 Toast，并就地删行对照成功稿。
 */
@HiltViewModel
internal class BlockedUsersViewModel @Inject constructor(
    private val getBlockedUsers: GetBlockedUsersUseCase,
    private val unblockUser: UnblockUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockedUsersUiState())
    val uiState: StateFlow<BlockedUsersUiState> = _uiState.asStateFlow()

    fun bind() {
        _uiState.value = BlockedUsersUiState(users = getBlockedUsers())
    }

    fun onIntent(intent: BlockedUsersIntent) {
        when (intent) {
            is BlockedUsersIntent.RequestUnblock -> onRequestUnblock(intent.userId)
            BlockedUsersIntent.ConfirmUnblock -> confirmUnblock()
            BlockedUsersIntent.DismissOverlay -> _uiState.update { it.copy(pendingUser = null) }
            BlockedUsersIntent.ConsumeSnackbar -> _uiState.update { it.copy(snackbar = null) }
        }
    }

    private fun onRequestUnblock(userId: String) {
        val user = _uiState.value.users.find { it.id == userId } ?: return
        _uiState.update { it.copy(pendingUser = user) }
    }

    private fun confirmUnblock() {
        val user = _uiState.value.pendingUser ?: return
        viewModelScope.launch {
            when (unblockUser(user.id)) {
                is Outcome.Ok -> _uiState.update { state ->
                    state.copy(
                        users = state.users.filter { it.id != user.id },
                        pendingUser = null,
                        snackbar = BlockedUsersSnackbar.Unblocked,
                    )
                }
                is Outcome.Err -> _uiState.update { it.copy(pendingUser = null) }
            }
        }
    }
}
