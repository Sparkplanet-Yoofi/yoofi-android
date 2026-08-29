package ai.yoofi.app.ui.chat

/**
 * 把选中的成员写进草稿：完成正在打的 @ 片段，而不是把名字和 `@` 再拼一次。
 *
 * 只做 `removePrefix("@")` 再前置插入会漏掉句中 / 句尾的触发符，
 * 再叠加 IME 把上一键 `@` 回放，就会变成 `tomy @`。
 */
internal fun applyPickedMention(draft: String, displayName: String): String {
    val mention = "@$displayName "
    val tokenStart = incompleteMentionStart(draft)
    return if (tokenStart != null) {
        draft.substring(0, tokenStart) + mention
    } else {
        appendMention(draft, mention)
    }
}

/**
 * 文末尚未完成的 mention：`@` 或 `@xxx`，且 `@` 在句首或空白后面。
 * 已经空格结束的 `@name ` 不算，避免二次选人把上一名字吃掉。
 */
internal fun incompleteMentionStart(draft: String): Int? {
    val at = draft.lastIndexOf('@')
    if (at < 0) return null
    if (at > 0 && !draft[at - 1].isWhitespace()) return null
    val after = draft.substring(at + 1)
    if (after.any { it.isWhitespace() }) return null
    return at
}

private fun appendMention(draft: String, mention: String): String {
    if (draft.isEmpty() || draft.last().isWhitespace()) return draft + mention
    return "$draft $mention"
}
