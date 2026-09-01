package ai.yoofi.app.ui.gamedetail.map

import ai.yoofi.app.domain.gamedetail.GameMap
import ai.yoofi.app.domain.gamedetail.GetGameMapsUseCase
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

/** 切换地图假进度时长，接接口后改成等真实下载即可。 */
internal const val MapSwitchDurationMs = 1_200L

private const val SwitchSteps = 24

internal data class GameMapUiState(
    val maps: List<GameMap> = emptyList(),
    val currentMapId: String = "",
    val listOpen: Boolean = false,
    val loading: Boolean = false,
    val loadingProgress: Int = 0,
) {
    val currentMap: GameMap?
        get() = maps.firstOrNull { it.id == currentMapId } ?: maps.firstOrNull()
}

internal sealed interface GameMapIntent {
    data object ToggleList : GameMapIntent
    data object DismissList : GameMapIntent
    data class SelectMap(val mapId: String) : GameMapIntent
    data object CancelLoading : GameMapIntent
}

/**
 * 游戏详情地图页。聊天室 Map 芯片跳这里，不进 ChatRoomViewModel。
 */
@HiltViewModel
internal class GameMapViewModel @Inject constructor(
    getMaps: GetGameMapsUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<GameMapUiState>
    val uiState: StateFlow<GameMapUiState>

    init {
        val maps = getMaps()
        _uiState = MutableStateFlow(
            GameMapUiState(
                maps = maps,
                currentMapId = maps.firstOrNull()?.id.orEmpty(),
            ),
        )
        uiState = _uiState.asStateFlow()
    }

    private var switchJob: Job? = null

    fun onIntent(intent: GameMapIntent) {
        when (intent) {
            GameMapIntent.ToggleList -> {
                if (_uiState.value.loading) return
                _uiState.update { it.copy(listOpen = !it.listOpen) }
            }
            GameMapIntent.DismissList -> {
                _uiState.update { it.copy(listOpen = false) }
            }
            is GameMapIntent.SelectMap -> selectMap(intent.mapId)
            GameMapIntent.CancelLoading -> cancelLoading()
        }
    }

    private fun selectMap(mapId: String) {
        val state = _uiState.value
        if (state.maps.none { it.id == mapId }) return
        if (mapId == state.currentMapId) {
            _uiState.update { it.copy(listOpen = false) }
            return
        }
        startSwitch(mapId)
    }

    private fun startSwitch(targetId: String) {
        switchJob?.cancel()
        _uiState.update {
            it.copy(listOpen = false, loading = true, loadingProgress = 0)
        }
        switchJob = viewModelScope.launch {
            val stepMs = MapSwitchDurationMs / SwitchSteps
            repeat(SwitchSteps) { index ->
                delay(stepMs)
                val percent = ((index + 1) * 100) / SwitchSteps
                _uiState.update { it.copy(loadingProgress = percent) }
            }
            _uiState.update {
                it.copy(
                    currentMapId = targetId,
                    loading = false,
                    loadingProgress = 0,
                )
            }
        }
    }

    private fun cancelLoading() {
        switchJob?.cancel()
        switchJob = null
        _uiState.update { it.copy(loading = false, loadingProgress = 0) }
    }
}
