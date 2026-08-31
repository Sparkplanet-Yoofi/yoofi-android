package ai.yoofi.app.ui.me

import ai.yoofi.app.R
import ai.yoofi.app.domain.profile.MineProfilePresence
import ai.yoofi.app.ui.pager.animateToRealPage
import ai.yoofi.app.ui.pager.loopingPageCount
import ai.yoofi.app.ui.pager.loopingStartPage
import ai.yoofi.app.ui.pager.realPageIndex
import ai.yoofi.app.ui.profile.ProfileIdentityCard
import ai.yoofi.app.ui.profile.ProfileLorebookEmptyPane
import ai.yoofi.app.ui.profile.ProfilePageBackground
import ai.yoofi.app.ui.profile.ProfilePrimaryTab
import ai.yoofi.app.ui.profile.ProfilePrimaryTabs
import ai.yoofi.app.ui.profile.ProfileWorkKind
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiSnackbarContainer
import ai.yoofi.app.ui.theme.YoofiSnackbarContent
import ai.yoofi.app.ui.theme.YoofiVipText
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/**
 * 我的页：主态 / 空态由 [MeViewModel] 解析，客态不进本页。
 */
@Composable
fun MeScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onPreviewProfile: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onSetupProfile: () -> Unit = {},
) {
    val viewModel: MeViewModel = hiltViewModel()
    val presence by viewModel.presence.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    MeLayout(
        presence = presence,
        onSettingsClick = onSettingsClick,
        onPreviewProfile = onPreviewProfile,
        onEditProfile = onEditProfile,
        onSetupProfile = onSetupProfile,
        modifier = modifier,
    )
}

@Composable
internal fun MeLayout(
    presence: MineProfilePresence,
    onSettingsClick: () -> Unit,
    onPreviewProfile: () -> Unit,
    onEditProfile: () -> Unit,
    onSetupProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strategy = presence.strategy()
    val primaryTabs = ProfilePrimaryTab.entries
    val cycle = primaryTabs.size
    val pagerState = rememberPagerState(
        initialPage = loopingStartPage(cycle),
        pageCount = { loopingPageCount(cycle) },
    )
    val primaryTab = primaryTabs[realPageIndex(pagerState.currentPage, cycle)]
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val userId = stringResource(R.string.me_user_id)
    val copyLabel = stringResource(R.string.cd_copy_id)
    val copiedMessage = stringResource(R.string.me_id_copied)
    var workKind by remember { mutableStateOf(ProfileWorkKind.StoryGame) }
    val identity = strategy.identity(userId)
    Box(modifier = modifier.fillMaxSize()) {
        ProfilePageBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 120.dp),
        ) {
            MeTopBar(
                onSettingsClick = onSettingsClick,
                onPreviewProfile = onPreviewProfile,
            )
            ProfileIdentityCard(
                identity = identity,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 1.dp),
                nameAccessory = {
                    Image(
                        painter = painterResource(R.drawable.ic_edit_pencil),
                        contentDescription = stringResource(strategy.pencilCdRes),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(
                                role = Role.Button,
                                onClick = strategy.onPencil(
                                    onEditProfile = onEditProfile,
                                    onSetupProfile = onSetupProfile,
                                ),
                            ),
                    )
                },
                idTrailing = strategy.wrapIdTrailing {
                    Image(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = stringResource(R.string.cd_copy_id),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                role = Role.Button,
                                onClick = {
                                    copyUserIdToClipboard(
                                        context,
                                        label = copyLabel,
                                        userId = userId,
                                    )
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            message = copiedMessage,
                                            duration = SnackbarDuration.Short,
                                        )
                                    }
                                },
                            )
                            .padding(6.dp),
                    )
                },
                trailing = { GetVipChip() },
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
                    ProfilePrimaryTab.Creations -> strategy.Creations(
                        workKind = workKind,
                        onWorkKindChange = { workKind = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 128.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = YoofiSnackbarContainer,
                contentColor = YoofiSnackbarContent,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
private fun MeTopBar(
    onSettingsClick: () -> Unit,
    onPreviewProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_settings_hex),
            contentDescription = stringResource(R.string.cd_settings),
            modifier = Modifier
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onSettingsClick),
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                .clickable(role = Role.Button, onClick = onPreviewProfile)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_preview_card),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.me_preview_profile),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun GetVipChip() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFA4B5FF),
                        Color(0xFFD9F3FF),
                        Color(0xFFE5DDFF),
                        Color(0xFFB951DE),
                    ),
                ),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.me_get_vip),
            color = YoofiVipText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
        )
        Spacer(Modifier.width(4.dp))
        Image(
            painter = painterResource(R.drawable.ic_vip_caret),
            contentDescription = null,
            modifier = Modifier
                .size(7.dp, 6.dp)
                .rotate(90f),
        )
    }
}

/**
 * 将用户 ID 写入系统剪贴板。label 供系统粘贴面板展示，不进入业务逻辑。
 */
private fun copyUserIdToClipboard(context: Context, label: String, userId: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, userId))
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MeScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        MeLayout(
            presence = MineProfilePresence.Populated,
            onSettingsClick = {},
            onPreviewProfile = {},
            onEditProfile = {},
            onSetupProfile = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MeVacantPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        MeLayout(
            presence = MineProfilePresence.Vacant,
            onSettingsClick = {},
            onPreviewProfile = {},
            onEditProfile = {},
            onSetupProfile = {},
        )
    }
}
