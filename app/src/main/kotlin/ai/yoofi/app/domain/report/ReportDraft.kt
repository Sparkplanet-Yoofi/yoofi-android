package ai.yoofi.app.domain.report

/**
 * 提交举报用的草稿。截图可选，详情必填。
 */
data class ReportDraft(
    val gameId: String,
    val reason: ReportReason,
    val details: String,
    val screenshotUris: List<String> = emptyList(),
)
