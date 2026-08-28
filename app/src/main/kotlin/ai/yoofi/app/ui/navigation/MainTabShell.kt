package ai.yoofi.app.ui.navigation

import ai.yoofi.app.ui.chat.ChatRoomScreen
import ai.yoofi.app.ui.create.CreateScreen
import ai.yoofi.app.ui.game.GameHomeScreen
import ai.yoofi.app.ui.home.HomeExploreScreen
import ai.yoofi.app.ui.me.MeScreen
import ai.yoofi.app.ui.search.SearchScreen
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
    Box(modifier = modifier.fillMaxSize()) {
        when (tab) {
            YoofiTab.Home -> HomeExploreScreen(
                onSearchClick = { searchOpen = true },
                onListedWorkClick = {
                    // TODO AI不要删：临时线点击后直接跳转聊天室，少了2个游戏详情界面Start Game
                    chatOpen = true
                },
            )
            YoofiTab.World -> GameHomeScreen(
                onSearchClick = { searchOpen = true },
                onPlayedItemClick = { chatOpen = true },
            )
            YoofiTab.Create -> CreateScreen()
            YoofiTab.Me -> MeScreen()
        }
        if (!chatOpen && !searchOpen) {
            YoofiBottomBar(
                selected = tab,
                onTabSelected = { tab = it },
                modifier = Modifier.align(Alignment.BottomCenter),
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
