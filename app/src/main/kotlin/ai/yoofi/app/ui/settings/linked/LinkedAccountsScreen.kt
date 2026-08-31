package ai.yoofi.app.ui.settings.linked

import ai.yoofi.app.R
import ai.yoofi.app.domain.auth.LinkedAccount
import ai.yoofi.app.domain.auth.LinkedAccountProvider
import ai.yoofi.app.ui.auth.AuthBackground
import ai.yoofi.app.ui.auth.AuthSignUpHeader
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiSnackbarContainer
import ai.yoofi.app.ui.theme.YoofiSnackbarContent
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val PagePad = 20.dp
private val CardShape = RoundedCornerShape(12.dp)
private val CardFill = Color.White.copy(alpha = 0.1f)

/**
 * 关联账号，对齐 Figma `2252:17106` / `2252:17155`。
 */
@Composable
internal fun LinkedAccountsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LinkedAccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.bind() }
    LinkedAccountsLayout(
        state = state,
        onBack = onBack,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun LinkedAccountsLayout(
    state: LinkedAccountsUiState,
    onBack: () -> Unit,
    onIntent: (LinkedAccountsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val unlinkedMessage = stringResource(R.string.settings_linked_unlinked_snackbar)
    LaunchedEffect(state.snackbar) {
        val kind = state.snackbar ?: return@LaunchedEffect
        val message = when (kind) {
            LinkedAccountsSnackbar.Unlinked -> unlinkedMessage
        }
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        onIntent(LinkedAccountsIntent.ConsumeSnackbar)
    }
    BackHandler {
        if (state.overlay != LinkedAccountsOverlay.None) {
            onIntent(LinkedAccountsIntent.DismissOverlay)
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
                title = stringResource(R.string.settings_linked_accounts),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PagePad)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.accounts.forEach { account ->
                    LinkedAccountRow(
                        account = account,
                        lastLinked = account.linked && state.linkedCount <= 1,
                        onClick = {
                            onIntent(LinkedAccountsIntent.ClickAccount(account.provider))
                        },
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = YoofiSnackbarContainer,
                contentColor = YoofiSnackbarContent,
                shape = RoundedCornerShape(12.dp),
            )
        }
        when (state.overlay) {
            LinkedAccountsOverlay.None -> Unit
            LinkedAccountsOverlay.ConfirmUnlink -> UnlinkConfirmDialog(
                providerLabel = providerLabel(state.pendingProvider),
                onConfirm = { onIntent(LinkedAccountsIntent.ConfirmUnlink) },
                onDismiss = { onIntent(LinkedAccountsIntent.DismissOverlay) },
            )
            LinkedAccountsOverlay.LastAccount -> LastAccountDialog(
                onDismiss = { onIntent(LinkedAccountsIntent.DismissOverlay) },
            )
        }
    }
}

@Composable
private fun LinkedAccountRow(
    account: LinkedAccount,
    lastLinked: Boolean,
    onClick: () -> Unit,
) {
    val action = if (account.linked) {
        stringResource(R.string.settings_linked_unlink)
    } else {
        stringResource(R.string.settings_linked_link)
    }
    val actionColor = if (account.linked && lastLinked) {
        Color.White.copy(alpha = 0.5f)
    } else {
        YoofiAccent
    }
    val identity = if (account.linked) {
        account.maskedIdentity
    } else {
        stringResource(R.string.settings_linked_not_linked)
    }
    val identityColor = if (account.linked) Color.White else Color.White.copy(alpha = 0.5f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CardShape)
            .background(CardFill)
            .clickableDismissingIme(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(account.provider.iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = identity,
            color = identityColor,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = action,
            color = actionColor,
            fontSize = 12.sp,
        )
    }
}

@get:DrawableRes
private val LinkedAccountProvider.iconRes: Int
    get() = when (this) {
        LinkedAccountProvider.Google -> R.drawable.ic_auth_google
        LinkedAccountProvider.Apple -> R.drawable.ic_auth_apple
    }

@Composable
private fun providerLabel(provider: LinkedAccountProvider?): String {
    return when (provider) {
        LinkedAccountProvider.Google -> stringResource(R.string.settings_linked_provider_google)
        LinkedAccountProvider.Apple -> stringResource(R.string.settings_linked_provider_apple)
        null -> ""
    }
}

private fun sampleAccounts(bothLinked: Boolean) = listOf(
    LinkedAccount(
        provider = LinkedAccountProvider.Google,
        linked = true,
        maskedIdentity = "z***@gmail.com",
    ),
    LinkedAccount(
        provider = LinkedAccountProvider.Apple,
        linked = bothLinked,
        maskedIdentity = "z***@gmail.com",
    ),
)

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LinkedAccountsDualPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        LinkedAccountsLayout(
            state = LinkedAccountsUiState(accounts = sampleAccounts(bothLinked = true)),
            onBack = {},
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LinkedAccountsSinglePreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        LinkedAccountsLayout(
            state = LinkedAccountsUiState(accounts = sampleAccounts(bothLinked = false)),
            onBack = {},
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LinkedAccountsUnlinkPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        LinkedAccountsLayout(
            state = LinkedAccountsUiState(
                accounts = sampleAccounts(bothLinked = true),
                overlay = LinkedAccountsOverlay.ConfirmUnlink,
                pendingProvider = LinkedAccountProvider.Google,
            ),
            onBack = {},
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LinkedAccountsLastPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        LinkedAccountsLayout(
            state = LinkedAccountsUiState(
                accounts = sampleAccounts(bothLinked = false),
                overlay = LinkedAccountsOverlay.LastAccount,
                pendingProvider = LinkedAccountProvider.Google,
            ),
            onBack = {},
            onIntent = {},
        )
    }
}
