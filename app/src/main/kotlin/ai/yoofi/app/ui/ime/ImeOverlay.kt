package ai.yoofi.app.ui.ime

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.semantics.Role

/**
 * 含输入框的全屏页默认用键盘覆盖：布局不随 IME 顶起；点非输入区收起键盘。
 *
 * 页面根用 [ImeOverlayBox]；非输入点击用 [clickableDismissingIme]；
 * 不要对根布局 / 底栏写 `imePadding()` 或按 `WindowInsets.ime` 改按钮位置。
 * 仅当产品书面要求「内容贴键盘」时才用 [imeAvoidingPadding]，且只垫需要抬起的那一层，
 * 背景图仍铺满；继续叠 [ImeOverlayBox] 以保持 ADJUST_NOTHING，避免和 Manifest
 * `adjustResize` 叠一次。
 */
@Composable
fun ImeOverlayBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    ImeOverlayEffect()
    Box(
        modifier = modifier
            .fillMaxSize()
            .dismissImeOnOutsideTap(),
        content = content,
    )
}

/**
 * 本页使用「键盘覆盖」：窗口不因 IME 调整尺寸，布局保持设计稿坐标。
 * 离开页面时恢复进入前的 softInputMode。优先用 [ImeOverlayBox]，不必单独调这个。
 */
@Composable
fun ImeOverlayEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
            ?: return@DisposableEffect onDispose { }
        val previous = window.attributes.softInputMode
        val state = previous and WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE
        window.setSoftInputMode(
            state or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING,
        )
        onDispose { window.setSoftInputMode(previous) }
    }
}

/** 点击空白/非输入区时收起键盘，不拦截子组件自身的点击。 */
fun Modifier.dismissImeOnOutsideTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val dismiss = remember(focusManager, keyboard) {
        { dismissIme(focusManager, keyboard) }
    }
    pointerInput(dismiss) {
        detectTapGestures(onTap = { dismiss() })
    }
}

/**
 * 非输入控件点击：先收起键盘再执行 [onClick]。
 * 输入框请继续用 TextField，不要套这个。
 */
fun Modifier.clickableDismissingIme(
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClick: () -> Unit,
): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    clickable(enabled = enabled, role = role) {
        dismissIme(focusManager, keyboard)
        onClick()
    }
}

/**
 * 把内容推到 IME 与导航栏之上。键盘收起后只剩导航栏 inset，布局回落。
 *
 * 只给「要抬起」的那一层用（聊天室的 Header + 列表 + Footer），不要套在全屏背景上。
 * 必须和 [ImeOverlayBox] 一起用：窗口保持 ADJUST_NOTHING，抬起量只来自这一处 inset，
 * 不会和 `adjustResize` 叠两次。
 */
fun Modifier.imeAvoidingPadding(): Modifier = composed {
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
}

fun dismissIme(
    focusManager: FocusManager,
    keyboard: SoftwareKeyboardController?,
) {
    focusManager.clearFocus()
    keyboard?.hide()
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
