package ai.yoofi.app.ui.chat

import android.graphics.RuntimeShader
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** 守住 AGSL 源码可编译，并覆盖 iOS shader 的全部 8 个效果分支 */
@RunWith(AndroidJUnit4::class)
class ChatCastRevealShaderTest {

    @Test
    fun agslCompilesAndAcceptsEveryEffect() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        val shader = RuntimeShader(ChatCastRevealAgsl)
        ChatCastRevealEffect.entries.forEach { effect ->
            shader.setFloatUniform("effectValue", effect.shaderId.toFloat())
            shader.setFloatUniform("progress", 0.5f)
            shader.setFloatUniform("size", 298f, 416f)
            shader.setFloatUniform("accentColor", 1f, 0.83f, 0.54f, 1f)
        }
    }
}
