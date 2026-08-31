package ai.yoofi.app.ui.profile.preview

import ai.yoofi.app.domain.profile.GetPreviewPlayedWorksUseCase
import ai.yoofi.app.domain.profile.PreviewPlayedWork
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class PreviewProfileUiState(
    val works: List<PreviewPlayedWork> = emptyList(),
)

/**
 * 我的个人页预览。别人眼里的 Played / Lorebook / Props，不进 MeViewModel，
 * 也不复用客态拉黑。
 */
@HiltViewModel
internal class PreviewProfileViewModel @Inject constructor(
    getPlayed: GetPreviewPlayedWorksUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PreviewProfileUiState(works = getPlayed()),
    )
    val uiState: StateFlow<PreviewProfileUiState> = _uiState.asStateFlow()
}
