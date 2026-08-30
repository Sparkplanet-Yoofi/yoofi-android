package ai.yoofi.app.ui.profile.guest

import ai.yoofi.app.ui.profile.GuestProfileTarget
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal enum class GuestProfileOverlay { None, Menu, ConfirmBlock }

internal enum class GuestSnackbar { BlockUser }

internal data class GuestProfileUiState(
    val target: GuestProfileTarget? = null,
    val following: Boolean = false,
    val overlay: GuestProfileOverlay = GuestProfileOverlay.None,
    val snackbar: GuestSnackbar? = null,
)

internal sealed interface GuestProfileIntent {
    data object ToggleFollow : GuestProfileIntent
    data object OpenMenu : GuestProfileIntent
    data object DismissOverlay : GuestProfileIntent
    data object RequestBlock : GuestProfileIntent
    data object ConfirmBlock : GuestProfileIntent
    data object ConsumeSnackbar : GuestProfileIntent
}

/**
 * 客态资料页。拉黑接口未定时 [ConfirmBlock] 只发出 Snackbar 占位，
 * 接接口后在这里加 UseCase，不要把 HTTP 写进 Screen。
 */
@HiltViewModel
internal class GuestProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(GuestProfileUiState())
    val uiState: StateFlow<GuestProfileUiState> = _uiState.asStateFlow()

    /**
     * 由 Screen 在 `LaunchedEffect(target.userId)` 里调用。
     * 同一用户重复进入只保留当前关注态，不重置弹层。
     */
    fun bind(target: GuestProfileTarget) {
        val current = _uiState.value.target
        if (current?.userId == target.userId) return
        _uiState.value = GuestProfileUiState(
            target = target,
            following = target.following,
        )
    }

    fun onIntent(intent: GuestProfileIntent) {
        when (intent) {
            GuestProfileIntent.ToggleFollow -> _uiState.update {
                it.copy(following = !it.following)
            }
            GuestProfileIntent.OpenMenu -> _uiState.update {
                it.copy(overlay = GuestProfileOverlay.Menu)
            }
            GuestProfileIntent.DismissOverlay -> _uiState.update {
                it.copy(overlay = GuestProfileOverlay.None)
            }
            GuestProfileIntent.RequestBlock -> _uiState.update {
                it.copy(overlay = GuestProfileOverlay.ConfirmBlock)
            }
            GuestProfileIntent.ConfirmBlock -> _uiState.update {
                it.copy(
                    overlay = GuestProfileOverlay.None,
                    snackbar = GuestSnackbar.BlockUser,
                )
            }
            GuestProfileIntent.ConsumeSnackbar -> _uiState.update {
                it.copy(snackbar = null)
            }
        }
    }
}
