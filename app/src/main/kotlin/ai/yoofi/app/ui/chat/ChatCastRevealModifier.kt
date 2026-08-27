package ai.yoofi.app.ui.chat

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/** AGSL 需要 Android 13（API 33）及以上，低版本走降级路径 */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
internal fun isChatCastRevealShaderSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/**
 * 给内容套上人物卡揭示特效，[progress] 从 0 走到 1 表示揭示完成。
 *
 * API 33 及以上用 AGSL 还原 iOS 的 shader；低版本没有 [RuntimeShader]，降级为渐显，
 * 保证业务在 minSdk 24 上仍然可用。
 */
@Composable
internal fun Modifier.chatCastReveal(
    effect: ChatCastRevealEffect,
    progress: Float,
    accentColor: Color,
): Modifier {
    val clamped = progress.coerceIn(0f, 1f)
    if (!isChatCastRevealShaderSupported()) {
        return this.graphicsLayer { alpha = clamped }
    }
    // AGSL 在运行时才编译，个别 ROM 上可能失败，这里兜底回退到渐显而不是让页面崩掉
    val shader = remember { runCatching { RuntimeShader(ChatCastRevealAgsl) }.getOrNull() }
        ?: return this.graphicsLayer { alpha = clamped }
    return this.graphicsLayer {
        shader.setFloatUniform("progress", clamped)
        shader.setFloatUniform("effectValue", effect.shaderId.toFloat())
        shader.setFloatUniform("size", size.width, size.height)
        shader.setFloatUniform(
            "accentColor",
            accentColor.red,
            accentColor.green,
            accentColor.blue,
            accentColor.alpha,
        )
        renderEffect = RenderEffect
            .createRuntimeShaderEffect(shader, ChatCastRevealLayerUniform)
            .asComposeRenderEffect()
    }
}

/** AGSL 里接收图层内容的 uniform 名，必须与 shader 源码一致 */
private const val ChatCastRevealLayerUniform = "layer"
