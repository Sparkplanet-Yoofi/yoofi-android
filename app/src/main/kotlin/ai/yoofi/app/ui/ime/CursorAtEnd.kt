package ai.yoofi.app.ui.ime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 输入框获焦 / 外部回填时光标落到文案末尾。
 *
 * 所有产品输入框必须走这里，禁止把 [String] 直接喂给 `BasicTextField`。
 *
 * 不能只在 [onFocusChanged] 或 `SideEffect` 里改选区：选 @ / 灵感后立刻
 * `requestFocus` 时，`BasicTextField` 会在焦点回调之后把选区打回 0，再经
 * [onValueChange] 写回来。此时文案已经是新的，旧逻辑以为「不用同步」，
 * 光标就钉在开头。必须组合期内对齐文案，并在获焦后的帧里盖回末尾。
 *
 * 获焦之后玩家仍可点选中间；只有「刚获焦 / 刚回填」那一两帧会吞掉选区 0。
 */
class CursorAtEndField internal constructor(
    val value: TextFieldValue,
    val onValueChange: (TextFieldValue) -> Unit,
    internal val onFocusChanged: (Boolean) -> Unit,
)

@Composable
fun rememberCursorAtEndField(
    text: String,
    onTextChange: (String) -> Unit,
): CursorAtEndField {
    val onTextChangeLatest by rememberUpdatedState(onTextChange)
    var value by remember { mutableStateOf(textFieldValueAtEnd(text)) }
    var focused by remember { mutableStateOf(false) }
    // 获焦握手窗口：IME 把光标打回 0 时改回末尾，窗口结束后用户点中间生效
    var absorbZeroSelection by remember { mutableStateOf(false) }
    var snapToken by remember { mutableIntStateOf(0) }

    // 外部回填必须在组合期改选区。SideEffect 太晚，本帧已按选区 0 提交
    if (value.text != text) {
        value = textFieldValueAtEnd(text)
        absorbZeroSelection = true
        snapToken += 1
    }

    LaunchedEffect(snapToken) {
        if (snapToken == 0) return@LaunchedEffect
        withFrameNanos { }
        if (value.text == text) {
            value = textFieldValueAtEnd(text)
        }
        // 再等一帧：选 @ 后抢焦点时，IME 回放触发键往往比选区复位更晚
        withFrameNanos { }
        if (value.text == text) {
            value = textFieldValueAtEnd(text)
        }
        withFrameNanos { }
        absorbZeroSelection = false
    }

    return CursorAtEndField(
        value = value,
        onFocusChanged = { nowFocused ->
            val gained = nowFocused && !focused
            focused = nowFocused
            if (gained) {
                value = textFieldValueAtEnd(value.text)
                absorbZeroSelection = true
                snapToken += 1
            }
        },
        onValueChange = { next ->
            val accepted = acceptExternalOrImeEdit(
                incoming = next,
                sourceText = text,
                handshake = absorbZeroSelection,
            )
            value = accepted
            // 握手期外部回填是唯一真相，禁止把 IME 残留（如触发用的 @）写回
            if (!absorbZeroSelection && accepted.text != text) {
                onTextChangeLatest(accepted.text)
            }
        },
    )
}

/** 接到 [androidx.compose.foundation.text.BasicTextField] 的 modifier 链上。 */
fun Modifier.cursorAtEnd(field: CursorAtEndField): Modifier =
    onFocusChanged { state ->
        field.onFocusChanged(state.isFocused)
    }

internal fun textFieldValueAtEnd(text: String): TextFieldValue =
    TextFieldValue(
        text = text,
        selection = TextRange(text.length),
        composition = null,
    )

/**
 * 外部刚回填时：文案以 [sourceText] 为准。
 * IME 常把上一键（选 @ 前打出的 `@`）接到末尾，变成 `tomy @`，必须丢掉。
 */
internal fun acceptExternalOrImeEdit(
    incoming: TextFieldValue,
    sourceText: String,
    handshake: Boolean,
): TextFieldValue {
    if (!handshake) return incoming
    if (incoming.text != sourceText) {
        return textFieldValueAtEnd(sourceText)
    }
    return absorbFocusResetToStart(
        incoming = incoming,
        displayedText = sourceText,
        absorbZeroSelection = true,
    )
}

/**
 * 获焦 / 回填后的一两帧里，同一文案且选区被打回开头时改回末尾。
 * 用户开始改字，或点到中间，原样放行。
 */
internal fun absorbFocusResetToStart(
    incoming: TextFieldValue,
    displayedText: String,
    absorbZeroSelection: Boolean,
): TextFieldValue {
    if (!absorbZeroSelection) return incoming
    if (incoming.text != displayedText) return incoming
    val collapsedAtStart = incoming.selection.collapsed && incoming.selection.start == 0
    if (collapsedAtStart && incoming.text.isNotEmpty()) {
        return textFieldValueAtEnd(incoming.text)
    }
    return incoming
}
