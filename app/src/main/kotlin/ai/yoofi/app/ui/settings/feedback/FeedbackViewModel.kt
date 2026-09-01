package ai.yoofi.app.ui.settings.feedback

import ai.yoofi.shared.common.Outcome
import ai.yoofi.app.domain.feedback.FeedbackDraft
import ai.yoofi.app.domain.feedback.FeedbackType
import ai.yoofi.app.domain.feedback.SubmitFeedbackUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val FeedbackDetailsMaxLength = 500

internal enum class FeedbackStep { Form, Done }

internal data class FeedbackUiState(
    val step: FeedbackStep = FeedbackStep.Form,
    val type: FeedbackType? = null,
    val details: String = "",
    val contact: String = "",
    val submitting: Boolean = false,
) {
    val canSubmit: Boolean get() = type != null && details.isNotBlank() && !submitting
}

internal sealed interface FeedbackIntent {
    data class SelectType(val type: FeedbackType) : FeedbackIntent
    data class DetailsChanged(val value: String) : FeedbackIntent
    data class ContactChanged(val value: String) : FeedbackIntent
    data object Submit : FeedbackIntent
    data object Back : FeedbackIntent
}

/**
 * 设置反馈。选类型 + 写描述后可提交；联系方式可选。
 * 接口未定，[Submit] 只走 [SubmitFeedbackUseCase] 占位。
 */
@HiltViewModel
internal class FeedbackViewModel @Inject constructor(
    private val submitFeedback: SubmitFeedbackUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    private var onClose: () -> Unit = {}

    fun bind(onClose: () -> Unit) {
        this.onClose = onClose
        // 每次进入都从空表单重开，避免上次提交成功后再次点开仍停在 Done
        _uiState.value = FeedbackUiState()
    }

    fun onIntent(intent: FeedbackIntent) {
        when (intent) {
            is FeedbackIntent.SelectType -> _uiState.update { it.copy(type = intent.type) }
            is FeedbackIntent.DetailsChanged -> _uiState.update {
                it.copy(details = intent.value.take(FeedbackDetailsMaxLength))
            }
            is FeedbackIntent.ContactChanged -> _uiState.update { it.copy(contact = intent.value) }
            FeedbackIntent.Submit -> submit()
            FeedbackIntent.Back -> onClose()
        }
    }

    private fun submit() {
        val state = _uiState.value
        val type = state.type ?: return
        if (!state.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(submitting = true) }
            when (
                submitFeedback(
                    FeedbackDraft(
                        type = type,
                        details = state.details,
                        contact = state.contact,
                    ),
                )
            ) {
                is Outcome.Ok -> _uiState.update {
                    it.copy(submitting = false, step = FeedbackStep.Done)
                }
                is Outcome.Err -> _uiState.update { it.copy(submitting = false) }
            }
        }
    }
}
