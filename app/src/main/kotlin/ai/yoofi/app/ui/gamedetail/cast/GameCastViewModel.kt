package ai.yoofi.app.ui.gamedetail.cast

import ai.yoofi.app.domain.gamedetail.GameCastCard
import ai.yoofi.app.domain.gamedetail.GetGameCastCardsUseCase
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class GameCastUiState(
    val cards: List<GameCastCard> = emptyList(),
)

/**
 * 游戏详情人物页。聊天室 Cast 芯片跳这里，不进 ChatRoomViewModel。
 */
@HiltViewModel
internal class GameCastViewModel @Inject constructor(
    getCards: GetGameCastCardsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameCastUiState(cards = getCards()))
    val uiState: StateFlow<GameCastUiState> = _uiState.asStateFlow()
}
