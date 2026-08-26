package ai.yoofi.app.ui.navigation

import ai.yoofi.app.ui.auth.AuthFlow
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
 * 先走登录 UI，成功后再进四 Tab 壳。
 * 验证码接口返回已填资料时落在 Home Tab；资料填写完成仍默认 World。
 */
@Composable
fun YoofiApp(modifier: Modifier = Modifier) {
    var loggedIn by remember { mutableStateOf(false) }
    var startTab by remember { mutableStateOf(YoofiTab.World) }
    if (!loggedIn) {
        AuthFlow(
            onLoggedIn = { loggedIn = true },
            onEnterHome = {
                startTab = YoofiTab.Home
                loggedIn = true
            },
            modifier = modifier,
        )
        return
    }
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
