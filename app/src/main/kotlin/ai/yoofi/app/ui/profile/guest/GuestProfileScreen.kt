package ai.yoofi.app.ui.profile.guest

import ai.yoofi.app.R
import ai.yoofi.app.ui.pager.animateToRealPage
import ai.yoofi.app.ui.pager.loopingPageCount
import ai.yoofi.app.ui.pager.loopingStartPage
import ai.yoofi.app.ui.pager.realPageIndex
import ai.yoofi.app.ui.profile.GuestProfileTarget
import ai.yoofi.app.ui.profile.ProfileCreationsPane
import ai.yoofi.app.ui.profile.ProfileIdentity
import ai.yoofi.app.ui.profile.ProfileIdentityCard
import ai.yoofi.app.ui.profile.ProfileLorebookEmptyPane
import ai.yoofi.app.ui.profile.ProfilePageBackground
import ai.yoofi.app.ui.profile.ProfilePrimaryTab
import ai.yoofi.app.ui.profile.ProfilePrimaryTabs
import ai.yoofi.app.ui.profile.ProfileStat
import ai.yoofi.app.ui.profile.ProfileWorkKind
import ai.yoofi.app.ui.profile.profileAvatarRes
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiAuthFocusStroke
import ai.yoofi.app.ui.theme.YoofiCameraTo
import ai.yoofi.app.ui.theme.YoofiSnackbarContainer
import ai.yoofi.app.ui.theme.YoofiSnackbarContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/**
 * 我的客态，对齐 Figma `982:12908`。
 * 三点菜单对齐 `1943:14159`，确认拉黑对齐 `1943:14078`。
 */
@Composable
internal fun GuestProfileScreen(
    target: GuestProfileTarget,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GuestProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(target.userId) { viewModel.bind(target) }
    val bound = state.target ?: target
    GuestProfileLayout(
        target = bound,
        following = state.following,
        overlay = state.overlay,
        snackbar = state.snackbar,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun GuestProfileLayout(
    target: GuestProfileTarget,
    following: Boolean,
    overlay: GuestProfileOverlay,
    snackbar: GuestSnackbar?,
    onIntent: (GuestProfileIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryTabs = ProfilePrimaryTab.entries
    val cycle = primaryTabs.size
    val pagerState = rememberPagerState(
        initialPage = loopingStartPage(cycle),
        pageCount = { loopingPageCount(cycle) },
    )
    val primaryTab = primaryTabs[realPageIndex(pagerState.currentPage, cycle)]
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val blockMessage = stringResource(R.string.guest_block_snackbar)
    var workKind by remember { mutableStateOf(ProfileWorkKind.StoryGame) }
    val avatarRes = profileAvatarRes(target.avatarKey)
    val identity = ProfileIdentity(
        displayName = target.displayName,
        publicId = stringResource(R.string.me_user_id),
        avatarRes = avatarRes,
        stats = listOf(
            ProfileStat(
                count = stringResource(R.string.me_following_count),
                label = stringResource(R.string.me_following_label),
            ),
            ProfileStat(
                count = stringResource(R.string.me_follower_count),
                label = stringResource(R.string.me_follower_label),
            ),
        ),
    )
    LaunchedEffect(snackbar) {
        val kind = snackbar ?: return@LaunchedEffect
        val message = when (kind) {
            GuestSnackbar.BlockUser -> blockMessage
        }
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        onIntent(GuestProfileIntent.ConsumeSnackbar)
    }
    BackHandler {
        if (overlay != GuestProfileOverlay.None) {
            onIntent(GuestProfileIntent.DismissOverlay)
        } else {
            onBack()
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        ProfilePageBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            GuestTopBar(
                onBack = onBack,
                onMore = { onIntent(GuestProfileIntent.OpenMenu) },
            )
            ProfileIdentityCard(
                identity = identity,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 1.dp),
                avatarBadge = {
                    FollowPlusBadge(
                        following = following,
                        onClick = { onIntent(GuestProfileIntent.ToggleFollow) },
                    )
                },
            )
            Spacer(Modifier.height(16.dp))
            ProfilePrimaryTabs(
                selected = primaryTab,
                onSelected = { tab ->
                    scope.launch {
                        pagerState.animateToRealPage(primaryTabs.indexOf(tab), cycle)
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                beyondViewportPageCount = 1,
                key = { page -> page },
            ) { page ->
                when (primaryTabs[realPageIndex(page, cycle)]) {
                    ProfilePrimaryTab.Lorebook -> {
                        ProfileLorebookEmptyPane(Modifier.fillMaxSize())
                    }
                    ProfilePrimaryTab.Creations -> ProfileCreationsPane(
                        workKind = workKind,
                        onWorkKindChange = { workKind = it },
                        showDraftsBadge = false,
                        modifier = Modifier.fillMaxSize(),
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
        when (overlay) {
            GuestProfileOverlay.None -> Unit
            GuestProfileOverlay.Menu -> GuestBlockMenuSheet(
                onBlockUser = { onIntent(GuestProfileIntent.RequestBlock) },
                onDismiss = { onIntent(GuestProfileIntent.DismissOverlay) },
            )
            GuestProfileOverlay.ConfirmBlock -> GuestBlockConfirmDialog(
                displayName = target.displayName,
                avatarRes = avatarRes,
                onConfirm = { onIntent(GuestProfileIntent.ConfirmBlock) },
                onDismiss = { onIntent(GuestProfileIntent.DismissOverlay) },
            )
        }
    }
}

@Composable
private fun GuestTopBar(
    onBack: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_auth_back),
            contentDescription = stringResource(R.string.cd_detail_back),
            modifier = Modifier
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onBack),
        )
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.ic_detail_more),
            contentDescription = stringResource(R.string.cd_guest_more),
            modifier = Modifier
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onMore),
        )
    }
}

/** Figma `982:12968`：头像右下 20 圆钮，渐变 `#5257FF` → `#906AEF`。 */
@Composable
private fun FollowPlusBadge(
    following: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    listOf(YoofiCameraTo, YoofiAuthFocusStroke),
                ),
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_follow_plus),
            contentDescription = stringResource(
                if (following) R.string.cd_guest_following else R.string.cd_guest_follow,
            ),
            modifier = Modifier.size(13.dp),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GuestProfilePreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        GuestProfileLayout(
            target = GuestProfileTarget(
                userId = "author-anmi",
                displayName = "Jenny",
                avatarKey = "avatar-author",
            ),
            following = false,
            overlay = GuestProfileOverlay.None,
            snackbar = null,
            onIntent = {},
            onBack = {},
        )
    }
}
