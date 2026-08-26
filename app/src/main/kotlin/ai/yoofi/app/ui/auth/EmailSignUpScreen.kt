package ai.yoofi.app.ui.auth

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiAuthCaretFrom
import ai.yoofi.app.ui.theme.YoofiAuthCaretTo
import ai.yoofi.app.ui.theme.YoofiAuthError
import ai.yoofi.app.ui.theme.YoofiAuthFieldFill
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EmailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

internal fun isValidEmail(value: String): Boolean = EmailRegex.matches(value.trim())

/**
 * 邮箱注册，覆盖 Figma 初始 / 输入 / 错误三态：`1761:10014` `1761:10049` `1761:10084`。
 */
@Composable
internal fun EmailSignUpScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trimmed = email.trim()
    val valid = isValidEmail(trimmed)
    val showError = email.isNotEmpty() && !valid
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeOpen = WindowInsets.ime.getBottom(density) > 0

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(modifier = modifier.fillMaxSize()) {
        AuthBackground()
        Column(modifier = Modifier.fillMaxSize()) {
            AuthSignUpHeader(onBack = onBack)
            Text(
                text = stringResource(R.string.auth_enter_email),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 20.dp, top = 28.dp),
            )
            Text(
                text = stringResource(R.string.auth_email_subtitle),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(start = 20.dp, top = 8.dp)
                    .width(AuthPageWidth),
            )
            EmailField(
                value = email,
                onValueChange = onEmailChange,
                showError = showError,
                focused = focused,
                onFocusChange = { focused = it },
                onNext = {
                    if (valid) {
                        focusManager.clearFocus()
                        onNext()
                    }
                },
                modifier = Modifier
                    .padding(start = 20.dp, top = 42.dp)
                    .focusRequester(focusRequester),
            )
            if (showError) {
                Text(
                    text = stringResource(R.string.auth_email_error),
                    color = YoofiAuthError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 8.dp),
                )
            }
        }
        AuthNextButton(
            enabled = valid,
            onClick = onNext,
            modifier = if (imeOpen) {
                Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
                    .padding(bottom = 12.dp)
            } else {
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 438.dp)
            },
        )
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    focused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        ),
        cursorBrush = Brush.verticalGradient(
            colors = listOf(YoofiAuthCaretFrom, YoofiAuthCaretTo),
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        modifier = modifier
            .width(AuthPageWidth)
            .height(46.dp)
            .onFocusChanged { onFocusChange(it.isFocused) },
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(YoofiAuthFieldFill, AuthFieldShape)
                    .authFieldBorder(error = showError, focused = focused && !showError),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.auth_email_placeholder),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp,
                        )
                    }
                    inner()
                }
            }
        },
    )
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun EmailSignUpScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        EmailSignUpScreen(
            email = "test@gmail.com",
            onEmailChange = {},
            onBack = {},
            onNext = {},
        )
    }
}
