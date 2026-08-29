package ai.yoofi.app.ui.ime

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class CursorAtEndTest {

    @Test
    fun `构造时选区在文案末尾`() {
        val value = textFieldValueAtEnd("@tomy ")
        assertEquals("@tomy ", value.text)
        assertEquals(TextRange("@tomy ".length), value.selection)
    }

    @Test
    fun `握手窗口内同一文案选区被打回开头则改回末尾`() {
        val incoming = TextFieldValue("@tomy ", TextRange.Zero)
        val accepted = absorbFocusResetToStart(
            incoming = incoming,
            displayedText = "@tomy ",
            absorbZeroSelection = true,
        )
        assertEquals(TextRange("@tomy ".length), accepted.selection)
    }

    @Test
    fun `握手窗口内用户点中间则保留选区`() {
        val incoming = TextFieldValue("@tomy ", TextRange(2))
        val accepted = absorbFocusResetToStart(
            incoming = incoming,
            displayedText = "@tomy ",
            absorbZeroSelection = true,
        )
        assertEquals(TextRange(2), accepted.selection)
    }

    @Test
    fun `窗口结束后选区 0 不再改写以免锁死开头`() {
        val incoming = TextFieldValue("@tomy ", TextRange.Zero)
        val accepted = absorbFocusResetToStart(
            incoming = incoming,
            displayedText = "@tomy ",
            absorbZeroSelection = false,
        )
        assertEquals(TextRange.Zero, accepted.selection)
    }

    @Test
    fun `用户开始改字时不吸收选区`() {
        val incoming = TextFieldValue("@tomy hello", TextRange(11))
        val accepted = absorbFocusResetToStart(
            incoming = incoming,
            displayedText = "@tomy ",
            absorbZeroSelection = true,
        )
        assertEquals("@tomy hello", accepted.text)
        assertEquals(TextRange(11), accepted.selection)
    }

    @Test
    fun `握手期丢掉 IME 把触发 at 接到末尾的回放`() {
        val incoming = TextFieldValue("@tomy @", TextRange(7))
        val accepted = acceptExternalOrImeEdit(
            incoming = incoming,
            sourceText = "@tomy ",
            handshake = true,
        )
        assertEquals("@tomy ", accepted.text)
        assertEquals(TextRange("@tomy ".length), accepted.selection)
    }

    @Test
    fun `握手期丢掉名字插到触发 at 前面的回放`() {
        val incoming = TextFieldValue("tomy @", TextRange(0))
        val accepted = acceptExternalOrImeEdit(
            incoming = incoming,
            sourceText = "@tomy ",
            handshake = true,
        )
        assertEquals("@tomy ", accepted.text)
    }

    @Test
    fun `握手结束后才把 IME 当作用户输入`() {
        val incoming = TextFieldValue("@tomy hello", TextRange(11))
        val accepted = acceptExternalOrImeEdit(
            incoming = incoming,
            sourceText = "@tomy ",
            handshake = false,
        )
        assertEquals("@tomy hello", accepted.text)
    }
}
