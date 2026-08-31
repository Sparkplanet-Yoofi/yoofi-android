package ai.yoofi.app.ui.me

import ai.yoofi.app.domain.profile.MineProfilePresence
import ai.yoofi.app.domain.profile.ResolveMineProfilePresenceUseCase
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 只解析「我的」主态 / 空态。客态不进这个 VM。
 */
@HiltViewModel
internal class MeViewModel @Inject constructor(
    private val resolvePresence: ResolveMineProfilePresenceUseCase,
) : ViewModel() {

    private val _presence = MutableStateFlow(resolvePresence())
    val presence: StateFlow<MineProfilePresence> = _presence.asStateFlow()

    /** 从完善资料返回或会话变化后重算，避免 Tab 复用旧 VM 一直停在空态。 */
    fun refresh() {
        _presence.value = resolvePresence()
    }
}
