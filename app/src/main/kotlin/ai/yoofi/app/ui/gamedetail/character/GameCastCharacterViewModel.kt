package ai.yoofi.app.ui.gamedetail.character

import ai.yoofi.app.domain.gamedetail.GameCastCharacter
import ai.yoofi.app.domain.gamedetail.GameCastCharacterTab
import ai.yoofi.app.domain.gamedetail.GetGameCastCharacterUseCase
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal data class GameCastCharacterUiState(
    val character: GameCastCharacter? = null,
    val synopsisExpanded: Boolean = false,
)

internal sealed interface GameCastCharacterIntent {
    data object ToggleFollow : GameCastCharacterIntent
    data object ToggleFavorite : GameCastCharacterIntent
    data object ToggleSynopsis : GameCastCharacterIntent
    data class SelectTab(val tab: GameCastCharacterTab) : GameCastCharacterIntent
}

/**
 * 角色详情页。Cast 金卡点进来，不进 GameCastViewModel。
 */
@HiltViewModel
internal class GameCastCharacterViewModel @Inject constructor(
    private val getCharacter: GetGameCastCharacterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameCastCharacterUiState())
    val uiState: StateFlow<GameCastCharacterUiState> = _uiState.asStateFlow()

    private var characterId: String? = null

    /**
     * 工程尚未引入 Navigation，由 Screen 在 `LaunchedEffect` 里喂 id。
     * 同一个 id 不会重复拉。
     */
    fun load(characterId: String) {
        if (this.characterId == characterId) return
        this.characterId = characterId
        _uiState.value = GameCastCharacterUiState(
            character = getCharacter(characterId),
        )
    }

    fun onIntent(intent: GameCastCharacterIntent) {
        when (intent) {
            GameCastCharacterIntent.ToggleFollow -> updateCharacter {
                it.copy(following = !it.following)
            }
            GameCastCharacterIntent.ToggleFavorite -> updateCharacter {
                it.copy(favorited = !it.favorited)
            }
            GameCastCharacterIntent.ToggleSynopsis -> {
                _uiState.update { it.copy(synopsisExpanded = !it.synopsisExpanded) }
            }
            is GameCastCharacterIntent.SelectTab -> updateCharacter {
                it.copy(tab = intent.tab)
            }
        }
    }

    private fun updateCharacter(transform: (GameCastCharacter) -> GameCastCharacter) {
        _uiState.update { state ->
            val current = state.character ?: return@update state
            state.copy(character = transform(current))
        }
    }
}
