package ai.yoofi.app.ui.navigation

import ai.yoofi.app.ui.create.CreateScreen
import ai.yoofi.app.ui.game.GameHomeScreen
import ai.yoofi.app.ui.home.HomeExploreScreen
import ai.yoofi.app.ui.me.MeScreen
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
    Box(modifier = modifier.fillMaxSize()) {
        when (tab) {
            YoofiTab.Home -> HomeExploreScreen()
            YoofiTab.World -> GameHomeScreen()
            YoofiTab.Create -> CreateScreen()
            YoofiTab.Me -> MeScreen()
        }
        YoofiBottomBar(
            selected = tab,
            onTabSelected = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
