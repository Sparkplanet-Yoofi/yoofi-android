package ai.yoofi.app.ui.gamedetail.report

import ai.yoofi.app.domain.gamedetail.GameDetail

/**
 * 打开举报页所需的作品摘要。详情页只传这些，举报表单不回头读 GameDetail。
 */
internal data class ReportTarget(
    val gameId: String,
    val title: String,
    val authorName: String,
    val coverKey: String,
)

internal fun GameDetail.toReportTarget(): ReportTarget = ReportTarget(
    gameId = id,
    title = title,
    authorName = author.name,
    coverKey = coverKey,
)
