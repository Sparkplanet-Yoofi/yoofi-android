package ai.yoofi.app.ui.gamedetail.item

import ai.yoofi.app.domain.gamedetail.GameItem
import ai.yoofi.app.domain.gamedetail.GameItemTarget
import ai.yoofi.app.domain.gamedetail.GetGameItemTargetsUseCase
import ai.yoofi.app.domain.gamedetail.GetGameItemsUseCase
import ai.yoofi.app.domain.gamedetail.formatItemUseMessage
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal data class GameItemUiState(
    val items: List<GameItem> = emptyList(),
    val targets: List<GameItemTarget> = emptyList(),
    val selectedItemId: String = "",
    val sheetOpen: Boolean = false,
    val previewOpen: Boolean = false,
    val targetOpen: Boolean = false,
    val selectedTargetIds: Set<String> = emptySet(),
) {
    val selectedItem: GameItem?
        get() = items.firstOrNull { it.id == selectedItemId }

    val allTargetsSelected: Boolean
        get() = targets.isNotEmpty() && selectedTargetIds.containsAll(targets.map { it.id })
}

internal sealed interface GameItemIntent {
    /** Items 芯片每次进入都回列表。Activity 级 Hilt VM 会记住选人页。 */
    data object ShowList : GameItemIntent
    data class OpenItem(val itemId: String) : GameItemIntent
    data object DismissSheet : GameItemIntent
    data object OpenPreview : GameItemIntent
    data object ClosePreview : GameItemIntent
    data object OpenTargets : GameItemIntent
    data object CloseTargets : GameItemIntent
    data class ToggleTarget(val targetId: String) : GameItemIntent
    data object ToggleSelectAll : GameItemIntent
    data object ConfirmUse : GameItemIntent
}

internal sealed interface GameItemSideEffect {
    data class SendToChat(val text: String) : GameItemSideEffect
}

/**
 * 游戏详情道具页。聊天室 Items 芯片跳这里，不进 ChatRoomViewModel。
 */
@HiltViewModel
internal class GameItemViewModel @Inject constructor(
    getItems: GetGameItemsUseCase,
    getTargets: GetGameItemTargetsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GameItemUiState(
            items = getItems(),
            targets = getTargets(),
        ),
    )
    val uiState: StateFlow<GameItemUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<GameItemSideEffect>(capacity = Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onIntent(intent: GameItemIntent) {
        when (intent) {
            GameItemIntent.ShowList,
            GameItemIntent.DismissSheet,
            -> clearOverlays()
            is GameItemIntent.OpenItem -> openItem(intent.itemId)
            GameItemIntent.OpenPreview -> {
                if (_uiState.value.selectedItem == null) return
                _uiState.update { it.copy(previewOpen = true) }
            }
            GameItemIntent.ClosePreview -> {
                _uiState.update { it.copy(previewOpen = false) }
            }
            GameItemIntent.OpenTargets -> {
                if (_uiState.value.selectedItem == null) return
                _uiState.update {
                    it.copy(targetOpen = true, selectedTargetIds = emptySet())
                }
            }
            GameItemIntent.CloseTargets -> {
                _uiState.update {
                    it.copy(targetOpen = false, selectedTargetIds = emptySet())
                }
            }
            is GameItemIntent.ToggleTarget -> toggleTarget(intent.targetId)
            GameItemIntent.ToggleSelectAll -> toggleSelectAll()
            GameItemIntent.ConfirmUse -> confirmUse()
        }
    }

    private fun openItem(itemId: String) {
        if (_uiState.value.items.none { it.id == itemId }) return
        _uiState.update {
            it.copy(
                selectedItemId = itemId,
                sheetOpen = true,
                previewOpen = false,
                targetOpen = false,
                selectedTargetIds = emptySet(),
            )
        }
    }

    private fun toggleTarget(targetId: String) {
        if (_uiState.value.targets.none { it.id == targetId }) return
        _uiState.update { state ->
            val next = state.selectedTargetIds.toMutableSet()
            if (!next.add(targetId)) next.remove(targetId)
            state.copy(selectedTargetIds = next)
        }
    }

    private fun toggleSelectAll() {
        _uiState.update { state ->
            val allIds = state.targets.map { it.id }.toSet()
            val next = if (state.allTargetsSelected) emptySet() else allIds
            state.copy(selectedTargetIds = next)
        }
    }

    private fun confirmUse() {
        val state = _uiState.value
        val names = state.targets
            .filter { it.id in state.selectedTargetIds }
            .map { it.displayName }
        if (names.isEmpty()) return
        useItem(names)
    }

    private fun useItem(targetNames: List<String>) {
        val item = _uiState.value.selectedItem ?: return
        _sideEffect.trySend(
            GameItemSideEffect.SendToChat(formatItemUseMessage(item.name, targetNames)),
        )
        clearOverlays()
    }

    private fun clearOverlays() {
        _uiState.update {
            it.copy(
                sheetOpen = false,
                previewOpen = false,
                targetOpen = false,
                selectedItemId = "",
                selectedTargetIds = emptySet(),
            )
        }
    }
}
