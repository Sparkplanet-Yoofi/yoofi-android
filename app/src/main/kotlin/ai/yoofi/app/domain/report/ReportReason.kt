package ai.yoofi.app.domain.report

/**
 * 举报原因。文案在 UI 映射，这里只保留稳定 key，接接口时直接上报枚举名。
 */
enum class ReportReason {
    Sexual,
    Violent,
    Political,
    Copyright,
    Harassment,
    Scam,
    Other,
}
