package ai.yoofi.app.ui.settings

import ai.yoofi.app.BuildConfig
import ai.yoofi.app.R
import ai.yoofi.app.ui.auth.AuthBackground
import ai.yoofi.app.ui.auth.AuthSignUpHeader
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiAuthError
import ai.yoofi.app.ui.theme.YoofiDialogButton
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val PagePad = 20.dp
private val CardShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(100.dp)
private val CardFill = Color.White.copy(alpha = 0.1f)
private val RowDivider = Color.White.copy(alpha = 0.07f)

/**
 * 设置页，对齐 Figma `2304:20673`。
 * 其余行先画出入口，子页未定时点击为空实现。
 */
@Composable
internal fun SettingsScreen(
    onBack: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.bind(onSignedOut) }
    SettingsLayout(
        logoutConfirm = state.logoutConfirm,
        onBack = onBack,
        onDeleteAccount = onDeleteAccount,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsLayout(
    logoutConfirm: Boolean,
    onBack: () -> Unit,
    onDeleteAccount: () -> Unit,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        if (logoutConfirm) {
            onIntent(SettingsIntent.DismissLogout)
        } else {
            onBack()
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        AuthBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            AuthSignUpHeader(
                onBack = onBack,
                title = stringResource(R.string.settings_title),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PagePad)
                    .padding(top = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                SettingsGroup(title = stringResource(R.string.settings_general)) {
                    SettingsValueRow(
                        title = stringResource(R.string.settings_language),
                        value = stringResource(R.string.settings_language_english),
                    )
                }
                SettingsGroup(title = stringResource(R.string.settings_privacy)) {
                    SettingsNavRow(title = stringResource(R.string.settings_linked_accounts))
                }
                SettingsGroup(title = stringResource(R.string.settings_privacy)) {
                    SettingsNavRow(title = stringResource(R.string.settings_privacy_settings))
                    SettingsRowDivider()
                    SettingsNavRow(title = stringResource(R.string.settings_blocked_users))
                }
                SettingsGroup(title = stringResource(R.string.settings_support_about)) {
                    SettingsNavRow(title = stringResource(R.string.settings_feedback))
                    SettingsRowDivider()
                    SettingsNavRow(title = stringResource(R.string.settings_terms))
                    SettingsRowDivider()
                    SettingsNavRow(title = stringResource(R.string.settings_privacy_policy))
                }
            }
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PagePad),
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = PagePad)
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(PillShape)
                    .background(YoofiDialogButton)
                    .clickableDismissingIme(
                        role = Role.Button,
                        onClick = { onIntent(SettingsIntent.RequestLogout) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.settings_log_out),
                    color = YoofiAuthError,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.settings_delete_account),
                color = YoofiAuthError,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableDismissingIme(
                        role = Role.Button,
                        onClick = onDeleteAccount,
                    )
                    .padding(vertical = 4.dp),
            )
            Spacer(Modifier.height(20.dp))
        }
        if (logoutConfirm) {
            LogoutConfirmDialog(
                onConfirm = { onIntent(SettingsIntent.ConfirmLogout) },
                onDismiss = { onIntent(SettingsIntent.DismissLogout) },
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(CardFill),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsNavRow(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickableDismissingIme(role = Role.Button, onClick = {})
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
        )
        Image(
            painter = painterResource(R.drawable.ic_auth_back),
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .rotate(180f),
        )
    }
}

@Composable
private fun SettingsValueRow(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
        )
        Text(
            text = value,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun SettingsRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(1.dp)
            .background(RowDivider),
    )
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SettingsScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        SettingsLayout(
            logoutConfirm = false,
            onBack = {},
            onDeleteAccount = {},
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SettingsLogoutPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        SettingsLayout(
            logoutConfirm = true,
            onBack = {},
            onDeleteAccount = {},
            onIntent = {},
        )
    }
}
