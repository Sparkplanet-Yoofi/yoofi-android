package ai.yoofi.app.ui.auth

import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

private enum class AuthStep {
    Welcome,
    Email,
    Code,
}

/**
 * 登录流本地状态机，不引入 Navigation。
 * 邮箱有效则进验证码；验证码 `121111` 对齐 Figma 错误态，其余 6 位演示通过。
 */
@Composable
fun AuthFlow(
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableStateOf(AuthStep.Welcome) }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }

    when (step) {
        AuthStep.Welcome -> WelcomeScreen(
            onGoogle = onLoggedIn,
            onEmail = { step = AuthStep.Email },
            modifier = modifier,
        )
        AuthStep.Email -> EmailSignUpScreen(
            email = email,
            onEmailChange = { email = it },
            onBack = { step = AuthStep.Welcome },
            onNext = {
                if (isValidEmail(email)) {
                    code = ""
                    codeError = false
                    step = AuthStep.Code
                }
            },
            modifier = modifier,
        )
        AuthStep.Code -> VerificationCodeScreen(
            email = email.trim(),
            code = code,
            showError = codeError,
            onCodeChange = { value ->
                code = value
                codeError = false
            },
            onBack = {
                code = ""
                codeError = false
                step = AuthStep.Email
            },
            onNext = {
                if (code == DemoInvalidOtp) {
                    codeError = true
                } else if (code.length == 6) {
                    onLoggedIn()
                }
            },
            modifier = modifier,
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AuthFlowPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        AuthFlow(onLoggedIn = {})
    }
}
