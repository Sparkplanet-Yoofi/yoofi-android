package ai.yoofi.app.domain.feedback

/** 设置反馈类型，对齐 Figma `2252:17719` 四宫格。 */
enum class FeedbackType { Suggestion, Bug, Content, Other }

/**
 * 提交反馈草稿。[contact] 可空，接接口时原样带上。
 */
data class FeedbackDraft(
    val type: FeedbackType,
    val details: String,
    val contact: String,
)
