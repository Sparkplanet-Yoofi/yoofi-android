package ai.yoofi.app.ui.navigation

import ai.yoofi.app.ui.auth.AuthFlow
import ai.yoofi.app.ui.auth.AuthLandingTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * 应用根节点：只做「未登录走登录流，已登录进 Tab 壳」的分流。
 * 落地 Tab 由 [AuthFlow] 回传的 [AuthLandingTarget] 决定，Tab 切换交给 [MainTabShell]。
 */
@Composable
fun YoofiRoot(modifier: Modifier = Modifier) {
    // null 表示尚未登录；登录成功时一并确定落地 Tab，避免两个独立状态拼出非法组合。
    var landing by remember { mutableStateOf<AuthLandingTarget?>(null) }
    val target = landing
    if (target == null) {
        AuthFlow(
            onAuthenticated = { landing = it },
            modifier = modifier,
        )
        return
    }
    MainTabShell(startTab = target.toStartTab(), modifier = modifier)
}

private fun AuthLandingTarget.toStartTab(): YoofiTab = when (this) {
    AuthLandingTarget.Home -> YoofiTab.Home
}
