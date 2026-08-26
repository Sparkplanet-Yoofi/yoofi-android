package ai.yoofi.app.ui.auth

import ai.yoofi.app.R
import ai.yoofi.app.domain.auth.DemoInvalidEmailOtp
import ai.yoofi.app.ui.ime.ImeOverlayBox
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiAuthError
import ai.yoofi.app.ui.theme.YoofiAuthFieldFill
import ai.yoofi.app.ui.theme.YoofiAuthFocusStroke
import ai.yoofi.app.ui.theme.YoofiAuthOtpEmpty
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Figma 错误态示例码；其余 6 位交由 UseCase 请求 mock 接口。 */
internal const val DemoInvalidOtp = DemoInvalidEmailOtp

private const val OtpLength = 6
private const val ResendSeconds = 59

/**
 * 验证码页，覆盖 Figma 初始 / 输入 / 完成 / 错误：
 * `1761:10121` `1761:10158` `1761:10199` `1761:10245`。
 * 使用系统数字键盘，不画 iOS keypad。键盘走 [ImeOverlayBox]，不顶起 Next。
 */
@Composable
internal fun VerificationCodeScreen(
    email: String,
    code: String,
    showError: Boolean,
    onCodeChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    isVerifying: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val complete = code.length == OtpLength
    val nextEnabled = complete && !showError && !isVerifying
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var remain by remember { mutableIntStateOf(ResendSeconds) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(remain) {
        if (remain > 0) {
            delay(1_000)
            remain -= 1
        }
    }

    ImeOverlayBox(modifier = modifier) {
        AuthBackground()
        Column(modifier = Modifier.fillMaxSize()) {
            AuthSignUpHeader(onBack = onBack)
            Text(
                text = stringResource(R.string.auth_enter_code),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 20.dp, top = 28.dp),
            )
            Text(
                text = stringResource(R.string.auth_code_subtitle, email),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(start = 20.dp, top = 8.dp)
                    .width(AuthPageWidth),
            )
            OtpRow(
                code = code,
                showError = showError,
                focused = focused,
                onCodeChange = onCodeChange,
                modifier = Modifier
                    .padding(start = 20.dp, top = 25.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focused = it.isFocused },
            )
            if (showError) {
                Text(
                    text = stringResource(R.string.auth_code_error),
                    color = YoofiAuthError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 8.dp),
                )
            }
        }
        if (remain > 0) {
            Text(
                text = stringResource(R.string.auth_resend_countdown, remain),
                color = YoofiAccent.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 442.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.auth_resend),
                color = YoofiAccent,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 442.dp)
                    .clickableDismissingIme {
                        remain = ResendSeconds
                    },
            )
        }
        AuthNextButton(
            enabled = nextEnabled,
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 478.dp),
        )
    }
}

@Composable
private fun OtpRow(
    code: String,
    showError: Boolean,
    focused: Boolean,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = code,
        onValueChange = { raw ->
            onCodeChange(raw.filter { it.isDigit() }.take(OtpLength))
        },
        singleLine = true,
        textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.width(AuthPageWidth).height(56.dp),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                repeat(OtpLength) { index ->
                    OtpCell(
                        digit = code.getOrNull(index),
                        highlighted = otpCellHighlighted(
                            index = index,
                            length = code.length,
                            complete = code.length == OtpLength,
                            showError = showError,
                            focused = focused,
                        ),
                        error = showError,
                    )
                }
            }
        },
    )
}

/**
 * 空格灰底；已填 / 当前格紫底。
 * 未满 6 位时当前格（最后一位或首位空格）画聚焦描边；满 6 位无描边。
 */
private fun otpCellHighlighted(
    index: Int,
    length: Int,
    complete: Boolean,
    showError: Boolean,
    focused: Boolean,
): Boolean {
    if (showError || complete || !focused) return false
    val current = if (length == 0) 0 else length - 1
    return index == current
}

@Composable
private fun OtpCell(
    digit: Char?,
    highlighted: Boolean,
    error: Boolean,
) {
    val fill = if (digit != null || highlighted) {
        YoofiAuthFieldFill
    } else {
        YoofiAuthOtpEmpty
    }
    val stroke = when {
        error -> YoofiAuthError
        highlighted -> YoofiAuthFocusStroke
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .size(45.dp, 56.dp)
            .clip(AuthFieldShape)
            .background(fill)
            .then(
                if (stroke == Color.Transparent) {
                    Modifier
                } else {
                    Modifier.border(1.dp, stroke, AuthFieldShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (digit != null) {
            Text(
                text = digit.toString(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun VerificationCodeScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        VerificationCodeScreen(
            email = "test@gmail.com",
            code = DemoInvalidOtp,
            showError = true,
            onCodeChange = {},
            onBack = {},
            onNext = {},
        )
    }
}
