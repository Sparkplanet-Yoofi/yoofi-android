package ai.yoofi.app.ui.settings.delete

import ai.yoofi.app.R
import ai.yoofi.app.ui.auth.AuthBackground
import ai.yoofi.app.ui.auth.AuthFieldShape
import ai.yoofi.app.ui.auth.AuthSignUpHeader
import ai.yoofi.app.ui.gamedetail.DetailActionBrush
import ai.yoofi.app.ui.ime.ImeOverlayBox
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.ime.cursorAtEnd
import ai.yoofi.app.ui.ime.rememberCursorAtEndField
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiAuthCaretFrom
import ai.yoofi.app.ui.theme.YoofiAuthCaretTo
import ai.yoofi.app.ui.theme.YoofiAuthError
import ai.yoofi.app.ui.theme.YoofiAuthFieldFill
import ai.yoofi.app.ui.theme.YoofiAuthFocusStroke
import ai.yoofi.app.ui.theme.YoofiAuthIdleStroke
import ai.yoofi.app.ui.theme.YoofiCameraTo
import ai.yoofi.app.ui.theme.YoofiDialogButton
import ai.yoofi.app.ui.theme.YoofiInactive
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val PagePad = 20.dp
private val CardShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(100.dp)
private val WarningCardFill = Color(0x4D746C86)
private val SuccessRingBrush = Brush.horizontalGradient(
    listOf(YoofiAuthFocusStroke, YoofiCameraTo),
)

/**
 * 注销账号流，对齐 Figma `2252:16542` / `2252:16583` / `2252:16629` / `2252:16685`。
 */
@Composable
internal fun DeleteAccountScreen(
    onClose: () -> Unit,
    onAccountDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeleteAccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.bind(onClose = onClose, onDeleted = onAccountDeleted) }
    DeleteAccountLayout(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun DeleteAccountLayout(
    state: DeleteAccountUiState,
    onIntent: (DeleteAccountIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onIntent(DeleteAccountIntent.Back) }
    ImeOverlayBox(modifier = modifier) {
        AuthBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            AuthSignUpHeader(
                onBack = { onIntent(DeleteAccountIntent.Back) },
                title = stringResource(R.string.settings_delete_account),
            )
            when (state.step) {
                DeleteAccountStep.Warning -> WarningStep(
                    acknowledged = state.acknowledged,
                    nextEnabled = state.canGoNext,
                    onToggleAck = { onIntent(DeleteAccountIntent.ToggleAck) },
                    onNext = { onIntent(DeleteAccountIntent.Next) },
                    onCancel = { onIntent(DeleteAccountIntent.Cancel) },
                    modifier = Modifier.weight(1f),
                )
                DeleteAccountStep.Password -> PasswordStep(
                    password = state.password,
                    passwordVisible = state.passwordVisible,
                    phrase = state.passwordPhrase,
                    confirmEnabled = state.canConfirmPassword,
                    onPasswordChange = { onIntent(DeleteAccountIntent.PasswordChanged(it)) },
                    onToggleVisible = { onIntent(DeleteAccountIntent.TogglePasswordVisible) },
                    onPhraseChange = { onIntent(DeleteAccountIntent.PasswordPhraseChanged(it)) },
                    onConfirm = { onIntent(DeleteAccountIntent.ConfirmPassword) },
                    onCancel = { onIntent(DeleteAccountIntent.Cancel) },
                    modifier = Modifier.weight(1f),
                )
                DeleteAccountStep.Email -> EmailStep(
                    email = stringResource(R.string.settings_delete_email_sample),
                    code = state.emailCode,
                    phrase = state.emailPhrase,
                    codeCooldownSec = state.codeCooldownSec,
                    canSendCode = state.canSendCode,
                    confirmEnabled = state.canConfirmEmail,
                    onCodeChange = { onIntent(DeleteAccountIntent.EmailCodeChanged(it)) },
                    onPhraseChange = { onIntent(DeleteAccountIntent.EmailPhraseChanged(it)) },
                    onSendCode = { onIntent(DeleteAccountIntent.SendCode) },
                    onConfirm = { onIntent(DeleteAccountIntent.ConfirmEmail) },
                    onCancel = { onIntent(DeleteAccountIntent.Cancel) },
                    modifier = Modifier.weight(1f),
                )
                DeleteAccountStep.Done -> DoneStep(
                    redirectSeconds = state.redirectSeconds,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WarningStep(
    acknowledged: Boolean,
    nextEnabled: Boolean,
    onToggleAck: () -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PagePad)
                .padding(top = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(WarningCardFill)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Image(
                        painter = painterResource(R.drawable.ic_circle_alert),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.settings_delete_warning_title),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_delete_warning_intro),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 28.dp),
                )
                Spacer(Modifier.height(8.dp))
                WarningRule(R.string.settings_delete_warning_1)
                Spacer(Modifier.height(8.dp))
                WarningRule(R.string.settings_delete_warning_2)
                Spacer(Modifier.height(8.dp))
                WarningRule(R.string.settings_delete_warning_3)
                Spacer(Modifier.height(8.dp))
                WarningRule(R.string.settings_delete_warning_4)
            }
            Spacer(Modifier.height(40.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableDismissingIme(role = Role.Checkbox, onClick = onToggleAck),
                verticalAlignment = Alignment.Top,
            ) {
                if (acknowledged) {
                    Image(
                        painter = painterResource(R.drawable.ic_delete_ack),
                        contentDescription = stringResource(R.string.cd_delete_ack),
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    )
                }
                Text(
                    text = stringResource(R.string.settings_delete_ack),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        DeleteBottomActions(
            primaryLabel = stringResource(R.string.auth_next),
            primaryEnabled = nextEnabled,
            primaryBrush = true,
            onPrimary = onNext,
            onCancel = onCancel,
        )
    }
}

@Composable
private fun WarningRule(textRes: Int) {
    Text(
        text = stringResource(textRes),
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier.padding(start = 28.dp),
    )
}

@Composable
private fun PasswordStep(
    password: String,
    passwordVisible: Boolean,
    phrase: String,
    confirmEnabled: Boolean,
    onPasswordChange: (String) -> Unit,
    onToggleVisible: () -> Unit,
    onPhraseChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PagePad)
                .padding(top = 20.dp),
        ) {
            DeleteFieldLabel(stringResource(R.string.settings_delete_password))
            Spacer(Modifier.height(12.dp))
            DeleteInputField(
                value = password,
                onValueChange = onPasswordChange,
                hint = stringResource(R.string.settings_delete_password_hint),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailing = {
                    Image(
                        painter = painterResource(R.drawable.ic_eye_off),
                        contentDescription = stringResource(R.string.cd_toggle_password),
                        modifier = Modifier
                            .size(16.dp)
                            .clickableDismissingIme(onClick = onToggleVisible),
                    )
                },
            )
            Spacer(Modifier.height(40.dp))
            DeleteFieldLabel(stringResource(R.string.settings_delete_confirm_label))
            Spacer(Modifier.height(12.dp))
            DeleteInputField(
                value = phrase,
                onValueChange = onPhraseChange,
                hint = stringResource(R.string.settings_delete_confirm_hint),
            )
        }
        DeleteBottomActions(
            primaryLabel = stringResource(R.string.settings_delete_confirm),
            primaryEnabled = confirmEnabled,
            primaryBrush = false,
            onPrimary = onConfirm,
            onCancel = onCancel,
        )
    }
}

@Composable
private fun EmailStep(
    email: String,
    code: String,
    phrase: String,
    codeCooldownSec: Int,
    canSendCode: Boolean,
    confirmEnabled: Boolean,
    onCodeChange: (String) -> Unit,
    onPhraseChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PagePad)
                .padding(top = 20.dp),
        ) {
            DeleteFieldLabel(stringResource(R.string.settings_delete_email))
            Spacer(Modifier.height(12.dp))
            DeleteReadonlyField(value = email)
            Spacer(Modifier.height(40.dp))
            DeleteFieldLabel(stringResource(R.string.settings_delete_code))
            Spacer(Modifier.height(12.dp))
            DeleteInputField(
                value = code,
                onValueChange = onCodeChange,
                hint = stringResource(R.string.settings_delete_code_hint),
                trailing = {
                    val label = if (codeCooldownSec > 0) {
                        stringResource(R.string.settings_delete_code_wait, codeCooldownSec)
                    } else {
                        stringResource(R.string.settings_delete_send_code)
                    }
                    Text(
                        text = label,
                        color = YoofiAccent,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .alpha(if (canSendCode) 1f else 0.5f)
                            .clickableDismissingIme(
                                enabled = canSendCode,
                                onClick = onSendCode,
                            ),
                    )
                },
            )
            Spacer(Modifier.height(40.dp))
            DeleteFieldLabel(stringResource(R.string.settings_delete_confirm_label))
            Spacer(Modifier.height(12.dp))
            DeleteInputField(
                value = phrase,
                onValueChange = onPhraseChange,
                hint = stringResource(R.string.settings_delete_confirm_hint),
            )
        }
        DeleteBottomActions(
            primaryLabel = stringResource(R.string.settings_delete_confirm),
            primaryEnabled = confirmEnabled,
            primaryBrush = false,
            onPrimary = onConfirm,
            onCancel = onCancel,
        )
    }
}

@Composable
private fun DoneStep(
    redirectSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PagePad)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SuccessRingBrush),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_report_check),
                contentDescription = null,
                modifier = Modifier.size(width = 20.dp, height = 15.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.settings_delete_done_title),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.settings_delete_done_thanks))
                append("\n")
                append(stringResource(R.string.settings_delete_done_redirect_before))
                withStyle(
                    SpanStyle(
                        color = YoofiAccent,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append("$redirectSeconds ")
                }
                append(stringResource(R.string.settings_delete_done_seconds))
            },
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun DeleteFieldLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun DeleteReadonlyField(value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(YoofiAuthFieldFill, AuthFieldShape)
            .border(1.dp, YoofiAuthIdleStroke, AuthFieldShape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = value,
            color = YoofiInactive,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun DeleteInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    val field = rememberCursorAtEndField(value, onValueChange)
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = field.value,
        onValueChange = field.onValueChange,
        singleLine = true,
        visualTransformation = visualTransformation,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        ),
        cursorBrush = Brush.verticalGradient(
            colors = listOf(YoofiAuthCaretFrom, YoofiAuthCaretTo),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .cursorAtEnd(field)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(YoofiAuthFieldFill, AuthFieldShape)
                    .border(
                        1.dp,
                        if (focused) YoofiAuthFocusStroke else YoofiAuthIdleStroke,
                        AuthFieldShape,
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp,
                        )
                    }
                    inner()
                }
                if (trailing != null) {
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        trailing()
                    }
                }
            }
        },
    )
}

@Composable
private fun DeleteBottomActions(
    primaryLabel: String,
    primaryEnabled: Boolean,
    primaryBrush: Boolean,
    onPrimary: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PagePad)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val primaryBg = if (primaryBrush) {
            Modifier.background(DetailActionBrush, PillShape)
        } else {
            Modifier.background(YoofiAuthError, PillShape)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .alpha(if (primaryEnabled) 1f else 0.5f)
                .clip(PillShape)
                .then(primaryBg)
                .clickableDismissingIme(enabled = primaryEnabled, onClick = onPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = primaryLabel,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(PillShape)
                .background(YoofiDialogButton)
                .clickableDismissingIme(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.auth_cancel),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DeleteWarningPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        DeleteAccountLayout(
            state = DeleteAccountUiState(acknowledged = true),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DeletePasswordPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        DeleteAccountLayout(
            state = DeleteAccountUiState(step = DeleteAccountStep.Password),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DeleteEmailPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        DeleteAccountLayout(
            state = DeleteAccountUiState(step = DeleteAccountStep.Email),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DeleteDonePreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        DeleteAccountLayout(
            state = DeleteAccountUiState(step = DeleteAccountStep.Done),
            onIntent = {},
        )
    }
}
