package ai.yoofi.app.ui.gamedetail.report

import ai.yoofi.shared.common.Outcome
import ai.yoofi.app.domain.report.ReportDraft
import ai.yoofi.app.domain.report.ReportReason
import ai.yoofi.app.domain.report.SubmitReportUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val ReportDetailsMaxLength = 500
internal const val ReportScreenshotMax = 3

internal enum class ReportStep { Reason, Details, Done }

internal data class ReportContentUiState(
    val target: ReportTarget? = null,
    val step: ReportStep = ReportStep.Reason,
    val reason: ReportReason? = null,
    val details: String = "",
    val screenshotUris: List<String> = emptyList(),
    val submitting: Boolean = false,
) {
    val canGoNext: Boolean get() = reason != null
    val canSubmit: Boolean get() = details.isNotBlank() && !submitting
    val canAddScreenshot: Boolean get() = screenshotUris.size < ReportScreenshotMax
}

internal sealed interface ReportContentIntent {
    data class SelectReason(val reason: ReportReason) : ReportContentIntent
    data object Next : ReportContentIntent
    data class DetailsChanged(val value: String) : ReportContentIntent
    data class AddScreenshot(val uri: String) : ReportContentIntent
    data class RemoveScreenshot(val uri: String) : ReportContentIntent
    data object Submit : ReportContentIntent
    data object Back : ReportContentIntent
    data object Cancel : ReportContentIntent
}

/**
 * 内容举报三步：原因 → 详情 → 成功。
 * 接口未定，[Submit] 只走 [SubmitReportUseCase] 占位。
 */
@HiltViewModel
internal class ReportContentViewModel @Inject constructor(
    private val submitReport: SubmitReportUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportContentUiState())
    val uiState: StateFlow<ReportContentUiState> = _uiState.asStateFlow()

    private var closeReport: () -> Unit = {}

    fun bind(target: ReportTarget, onClose: () -> Unit) {
        closeReport = onClose
        // 每次进入都从原因页重开，避免上次提交成功后再次点开仍停在 Done
        _uiState.value = ReportContentUiState(target = target)
    }

    fun onIntent(intent: ReportContentIntent) {
        when (intent) {
            is ReportContentIntent.SelectReason -> _uiState.update {
                it.copy(reason = intent.reason)
            }
            ReportContentIntent.Next -> {
                if (!_uiState.value.canGoNext) return
                _uiState.update { it.copy(step = ReportStep.Details) }
            }
            is ReportContentIntent.DetailsChanged -> _uiState.update {
                it.copy(details = intent.value.take(ReportDetailsMaxLength))
            }
            is ReportContentIntent.AddScreenshot -> _uiState.update { state ->
                if (!state.canAddScreenshot || intent.uri in state.screenshotUris) {
                    state
                } else {
                    state.copy(screenshotUris = state.screenshotUris + intent.uri)
                }
            }
            is ReportContentIntent.RemoveScreenshot -> _uiState.update { state ->
                state.copy(screenshotUris = state.screenshotUris.filterNot { it == intent.uri })
            }
            ReportContentIntent.Submit -> submit()
            ReportContentIntent.Back -> onBack()
            ReportContentIntent.Cancel -> closeReport()
        }
    }

    private fun onBack() {
        when (_uiState.value.step) {
            ReportStep.Reason, ReportStep.Done -> closeReport()
            ReportStep.Details -> _uiState.update { it.copy(step = ReportStep.Reason) }
        }
    }

    private fun submit() {
        val state = _uiState.value
        val target = state.target ?: return
        val reason = state.reason ?: return
        if (!state.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(submitting = true) }
            when (
                submitReport(
                    ReportDraft(
                        gameId = target.gameId,
                        reason = reason,
                        details = state.details,
                        screenshotUris = state.screenshotUris,
                    ),
                )
            ) {
                is Outcome.Ok -> _uiState.update {
                    it.copy(submitting = false, step = ReportStep.Done)
                }
                is Outcome.Err -> _uiState.update { it.copy(submitting = false) }
            }
        }
    }
}
