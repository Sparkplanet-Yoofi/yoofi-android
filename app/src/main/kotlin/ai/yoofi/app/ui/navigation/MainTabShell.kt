package ai.yoofi.app.ui.navigation

import ai.yoofi.app.ui.chat.ChatRoomScreen
import ai.yoofi.app.ui.create.CreateScreen
import ai.yoofi.app.ui.gamedetail.GameDetailScreen
import ai.yoofi.app.ui.gamedetail.cast.GameCastScreen
import ai.yoofi.app.ui.gamedetail.character.GameCastCharacterScreen
import ai.yoofi.app.ui.gamedetail.item.GameItemScreen
import ai.yoofi.app.ui.gamedetail.map.GameMapGoResult
import ai.yoofi.app.ui.gamedetail.map.GameMapScreen
import ai.yoofi.app.ui.home.HomeExploreScreen
import ai.yoofi.app.ui.auth.ProfileEditorEntry
import ai.yoofi.app.ui.auth.ProfileSetupScreen
import ai.yoofi.app.ui.me.MeScreen
import ai.yoofi.app.ui.profile.GuestProfileTarget
import ai.yoofi.app.ui.profile.guest.GuestProfileScreen
import ai.yoofi.app.ui.profile.preview.PreviewProfileScreen
import ai.yoofi.app.ui.search.SearchScreen
import ai.yoofi.app.ui.settings.SettingsScreen
import ai.yoofi.app.ui.settings.delete.DeleteAccountScreen
import ai.yoofi.app.ui.settings.blocked.BlockedUsersScreen
import ai.yoofi.app.ui.settings.feedback.FeedbackScreen
import ai.yoofi.app.ui.settings.linked.LinkedAccountsScreen
import ai.yoofi.app.ui.surface.ContentBackdropProvider
import ai.yoofi.app.ui.surface.ContentBackdropRecorder
import ai.yoofi.app.ui.surface.rememberContentBackdropLayer
import ai.yoofi.app.ui.world.GameHomeScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 登录后的四 Tab 壳：只管当前 Tab、底栏、设置子页与资料预览 overlay。
 * 登出或删号成功必须走 [onSignedOut]，由 [YoofiRoot] 把 landing 置空，否则清会话后仍停在 Tab。
 *
 * @param startTab 首次进入落地的 Tab，由调用方按登录结果决定。
 * @param onSignedOut 会话已清，回到登录流。
 */
@Composable
internal fun MainTabShell(
    startTab: YoofiTab,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(startTab) }
    var chatOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    // 打开中的游戏详情页 id；null 表示没开。工程尚未引入 Navigation，先用状态提升代替回退栈
    var detailGameId by remember { mutableStateOf<String?>(null) }
    var guestProfile by remember { mutableStateOf<GuestProfileTarget?>(null) }
    var profileEditor by remember { mutableStateOf<ProfileEditorEntry?>(null) }
    var settingsOpen by remember { mutableStateOf(false) }
    var deleteAccountOpen by remember { mutableStateOf(false) }
    var linkedAccountsOpen by remember { mutableStateOf(false) }
    var blockedUsersOpen by remember { mutableStateOf(false) }
    var feedbackOpen by remember { mutableStateOf(false) }
    var previewProfileOpen by remember { mutableStateOf(false) }
    var gameCastOpen by remember { mutableStateOf(false) }
    var gameMapOpen by remember { mutableStateOf(false) }
    var gameItemsOpen by remember { mutableStateOf(false) }
    var pendingItemMessage by remember { mutableStateOf<String?>(null) }
    var pendingMapGo by remember { mutableStateOf<GameMapGoResult?>(null) }
    var castCharacterId by remember { mutableStateOf<String?>(null) }
    val backdropLayer = rememberContentBackdropLayer()
    ContentBackdropProvider(backdropLayer) {
        Box(modifier = modifier.fillMaxSize()) {
            ContentBackdropRecorder(backdropLayer) {
                when (tab) {
                    YoofiTab.Home -> HomeExploreScreen(
                        onSearchClick = { searchOpen = true },
                        onListedWorkClick = { gameId -> detailGameId = gameId },
                    )
                    YoofiTab.World -> GameHomeScreen(
                        onSearchClick = { searchOpen = true },
                        onPlayedItemClick = { chatOpen = true },
                    )
                    YoofiTab.Create -> CreateScreen()
                    YoofiTab.Me -> MeScreen(
                        onSettingsClick = { settingsOpen = true },
                        onPreviewProfile = { previewProfileOpen = true },
                        onEditProfile = { profileEditor = ProfileEditorEntry.Edit },
                        onSetupProfile = { profileEditor = ProfileEditorEntry.Create },
                    )
                }
            }
            if (!chatOpen &&
                !searchOpen &&
                detailGameId == null &&
                guestProfile == null &&
                profileEditor == null &&
                !settingsOpen &&
                !deleteAccountOpen &&
                !linkedAccountsOpen &&
                !blockedUsersOpen &&
                !feedbackOpen &&
                !previewProfileOpen &&
                !gameCastOpen &&
                !gameMapOpen &&
                !gameItemsOpen &&
                castCharacterId == null
            ) {
                YoofiBottomBar(
                    selected = tab,
                    onTabSelected = { tab = it },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
            detailGameId?.let { gameId ->
                GameDetailScreen(
                    gameId = gameId,
                    onBack = { detailGameId = null },
                    // 详情页留在栈上：从聊天室返回时回到详情，符合「进游戏再退出」的预期
                    onContinueGame = { chatOpen = true },
                    onOpenGuestProfile = { guestProfile = it },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            guestProfile?.let { target ->
                GuestProfileScreen(
                    target = target,
                    onBack = { guestProfile = null },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            profileEditor?.let { entry ->
                ProfileSetupScreen(
                    entry = entry,
                    onSkip = { profileEditor = null },
                    onCompleted = { profileEditor = null },
                    onEditFinished = { profileEditor = null },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (chatOpen) {
                ChatRoomScreen(
                    onBack = {
                        castCharacterId = null
                        gameCastOpen = false
                        gameMapOpen = false
                        gameItemsOpen = false
                        pendingItemMessage = null
                        pendingMapGo = null
                        chatOpen = false
                    },
                    onOpenCast = { gameCastOpen = true },
                    onOpenMap = { gameMapOpen = true },
                    onOpenItems = { gameItemsOpen = true },
                    pendingItemMessage = pendingItemMessage,
                    onPendingItemConsumed = { pendingItemMessage = null },
                    pendingMapGo = pendingMapGo,
                    onPendingMapConsumed = { pendingMapGo = null },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (gameCastOpen) {
                GameCastScreen(
                    onBack = {
                        castCharacterId = null
                        gameCastOpen = false
                    },
                    onClose = {
                        castCharacterId = null
                        gameCastOpen = false
                    },
                    onOpenCharacter = { castCharacterId = it },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            castCharacterId?.let { characterId ->
                GameCastCharacterScreen(
                    characterId = characterId,
                    onClose = { castCharacterId = null },
                    onContinueGame = {
                        castCharacterId = null
                        gameCastOpen = false
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (gameMapOpen) {
                GameMapScreen(
                    onBack = { gameMapOpen = false },
                    onClose = { gameMapOpen = false },
                    onGoToLocation = { result ->
                        gameMapOpen = false
                        pendingMapGo = result
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (gameItemsOpen) {
                GameItemScreen(
                    onBack = { gameItemsOpen = false },
                    onClose = { gameItemsOpen = false },
                    onUseItem = { message ->
                        gameItemsOpen = false
                        pendingItemMessage = message
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (settingsOpen) {
                SettingsScreen(
                    onBack = {
                        linkedAccountsOpen = false
                        blockedUsersOpen = false
                        feedbackOpen = false
                        settingsOpen = false
                    },
                    onLinkedAccounts = { linkedAccountsOpen = true },
                    onBlockedUsers = { blockedUsersOpen = true },
                    onFeedback = { feedbackOpen = true },
                    onDeleteAccount = { deleteAccountOpen = true },
                    onSignedOut = onSignedOut,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (linkedAccountsOpen) {
                LinkedAccountsScreen(
                    onBack = { linkedAccountsOpen = false },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (blockedUsersOpen) {
                BlockedUsersScreen(
                    onBack = { blockedUsersOpen = false },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (feedbackOpen) {
                FeedbackScreen(
                    onClose = { feedbackOpen = false },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (previewProfileOpen) {
                PreviewProfileScreen(
                    onBack = { previewProfileOpen = false },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (deleteAccountOpen) {
                DeleteAccountScreen(
                    onClose = { deleteAccountOpen = false },
                    onAccountDeleted = {
                        deleteAccountOpen = false
                        settingsOpen = false
                        onSignedOut()
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (searchOpen) {
                SearchScreen(
                    onBack = { searchOpen = false },
                    onStoryClick = {
                        // TODO AI不要删：临时线点击后直接跳转聊天室，少了2个游戏详情界面Start Game
                        searchOpen = false
                        chatOpen = true
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
