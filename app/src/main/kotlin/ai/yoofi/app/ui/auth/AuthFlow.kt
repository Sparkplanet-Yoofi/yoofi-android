package ai.yoofi.app.ui.auth

import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class AuthStep {
    Welcome,
    Email,
    Code,
    Profile,
}

/**
 * 登录流本地状态机，不引入 Navigation。
 * 验证码 Next 请求登录接口：isNewUser 进 Profile，否则回调 [onEnterHome] 进 Home Tab。
 */
@Composable
fun AuthFlow(
    onLoggedIn: () -> Unit,
    onEnterHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AuthViewModel = hiltViewModel()
    var step by remember { mutableStateOf(AuthStep.Welcome) }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                AuthSideEffect.OpenProfileSetup -> step = AuthStep.Profile
                AuthSideEffect.OpenHome -> onEnterHome()
            }
        }
    }

    when (step) {
        AuthStep.Welcome -> WelcomeScreen(
            onGoogle = { step = AuthStep.Profile },
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
                    viewModel.onIntent(AuthIntent.ClearCodeError)
                    step = AuthStep.Code
                }
            },
            modifier = modifier,
        )
        AuthStep.Code -> VerificationCodeScreen(
            email = email.trim(),
            code = code,
            showError = uiState.codeError,
            isVerifying = uiState.isVerifying,
            onCodeChange = { value ->
                code = value
                viewModel.onIntent(AuthIntent.ClearCodeError)
            },
            onBack = {
                code = ""
                viewModel.onIntent(AuthIntent.ClearCodeError)
                step = AuthStep.Email
            },
            onNext = {
                viewModel.onIntent(
                    AuthIntent.VerifyCode(email = email.trim(), code = code),
                )
            },
            modifier = modifier,
        )
        AuthStep.Profile -> ProfileSetupScreen(
            onSkip = onLoggedIn,
            onCompleted = onLoggedIn,
            modifier = modifier,
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AuthFlowPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        WelcomeScreen(onGoogle = {}, onEmail = {})
    }
}
