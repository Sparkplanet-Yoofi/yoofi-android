package ai.yoofi.app.ui.settings.blocked

import ai.yoofi.app.R
import ai.yoofi.app.domain.block.BlockedUser
import ai.yoofi.app.domain.block.GetBlockedUsersUseCase
import ai.yoofi.app.ui.auth.AuthBackground
import ai.yoofi.app.ui.auth.AuthSignUpHeader
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

private val PagePad = 20.dp
private val CardShape = RoundedCornerShape(12.dp)
private val CardFill = Color.White.copy(alpha = 0.1f)
private val RowDivider = Color.White.copy(alpha = 0.07f)
private const val ToastMillis = 2_000L

/**
 * 黑名单，对齐 Figma `2252:17322` / `2252:17548` / `2252:17465`。
 */
@Composable
internal fun BlockedUsersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BlockedUsersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.bind() }
    BlockedUsersLayout(
        state = state,
        onBack = onBack,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun BlockedUsersLayout(
    state: BlockedUsersUiState,
    onBack: () -> Unit,
    onIntent: (BlockedUsersIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.snackbar) {
        if (state.snackbar == null) return@LaunchedEffect
        delay(ToastMillis)
        onIntent(BlockedUsersIntent.ConsumeSnackbar)
    }
    BackHandler {
        if (state.pendingUser != null) {
            onIntent(BlockedUsersIntent.DismissOverlay)
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
                title = stringResource(R.string.settings_blocked_users),
            )
            if (state.users.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = PagePad)
                        .padding(top = 20.dp, bottom = 24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardShape)
                            .background(CardFill),
                    ) {
                        state.users.forEachIndexed { index, user ->
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                        .height(1.dp)
                                        .background(RowDivider),
                                )
                            }
                            BlockedUserRow(
                                user = user,
                                onUnblock = {
                                    onIntent(BlockedUsersIntent.RequestUnblock(user.id))
                                },
                            )
                        }
                    }
                }
            }
        }
        if (state.snackbar == BlockedUsersSnackbar.Unblocked) {
            UserUnblockedToast(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp),
            )
        }
        state.pendingUser?.let { user ->
            UnblockConfirmDialog(
                user = user,
                onConfirm = { onIntent(BlockedUsersIntent.ConfirmUnblock) },
                onDismiss = { onIntent(BlockedUsersIntent.DismissOverlay) },
            )
        }
    }
}

@Composable
private fun BlockedUserRow(
    user: BlockedUser,
    onUnblock: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(blockedAvatarRes(user.avatarKey)),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName,
                color = Color.White,
                fontSize = 14.sp,
            )
            Text(
                text = stringResource(R.string.settings_blocked_on, user.blockedOn),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(
            text = stringResource(R.string.settings_blocked_unblock),
            color = YoofiAccent,
            fontSize = 12.sp,
            modifier = Modifier.clickableDismissingIme(
                role = Role.Button,
                onClick = onUnblock,
            ),
        )
    }
}

private val sampleUsers = GetBlockedUsersUseCase()()

@DrawableRes
internal fun blockedAvatarRes(key: String): Int = when (key) {
    "jenny" -> R.drawable.img_blocked_jenny
    "lopez" -> R.drawable.img_blocked_lopez
    "lavgine" -> R.drawable.img_blocked_lavgine
    "troy" -> R.drawable.img_blocked_troy
    "sony" -> R.drawable.img_blocked_sony
    else -> R.drawable.img_blocked_jenny
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BlockedUsersPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        BlockedUsersLayout(
            state = BlockedUsersUiState(users = sampleUsers),
            onBack = {},
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BlockedUsersConfirmPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        BlockedUsersLayout(
            state = BlockedUsersUiState(
                users = sampleUsers,
                pendingUser = sampleUsers.first(),
            ),
            onBack = {},
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BlockedUsersToastPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        BlockedUsersLayout(
            state = BlockedUsersUiState(
                users = sampleUsers.drop(1),
                snackbar = BlockedUsersSnackbar.Unblocked,
            ),
            onBack = {},
            onIntent = {},
        )
    }
}
