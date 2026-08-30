package ai.yoofi.app.ui.navigation

import ai.yoofi.app.ui.chat.ChatRoomScreen
import ai.yoofi.app.ui.create.CreateScreen
import ai.yoofi.app.ui.gamedetail.GameDetailScreen
import ai.yoofi.app.ui.home.HomeExploreScreen
import ai.yoofi.app.ui.me.MeScreen
import ai.yoofi.app.ui.profile.GuestProfileTarget
import ai.yoofi.app.ui.profile.guest.GuestProfileScreen
import ai.yoofi.app.ui.search.SearchScreen
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
 * 登录后的四 Tab 壳：只管当前 Tab 与底栏，不掺登录判断。
 *
 * @param startTab 首次进入落地的 Tab，由调用方按登录结果决定。
 */
@Composable
internal fun MainTabShell(
    startTab: YoofiTab,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(startTab) }
    var chatOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    // 打开中的游戏详情页 id；null 表示没开。工程尚未引入 Navigation，先用状态提升代替回退栈
    var detailGameId by remember { mutableStateOf<String?>(null) }
    var guestProfile by remember { mutableStateOf<GuestProfileTarget?>(null) }
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
                    YoofiTab.Me -> MeScreen()
                }
            }
            if (!chatOpen && !searchOpen && detailGameId == null && guestProfile == null) {
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
            if (chatOpen) {
                ChatRoomScreen(
                    onBack = { chatOpen = false },
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
