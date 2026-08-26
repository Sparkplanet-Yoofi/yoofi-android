package ai.yoofi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ai.yoofi.app.ui.navigation.YoofiApp
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * AndroidEntryPoint 让本 Activity 成为 Hilt 注入点，
 * 后续 ViewModel 通过 hiltViewModel() 获取时依赖此注解
 *
 * @author JackXu
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 深色 Game 顶栏：状态栏图标用浅色，避免动态取色冲掉 Figma 色值
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
                YoofiApp()
            }
        }
    }
}
