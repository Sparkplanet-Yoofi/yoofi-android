package ai.yoofi.app.data.item.preview

import ai.yoofi.app.core.item.preview.ItemPreviewContent
import android.app.Instrumentation
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * 实时 3D 预览的渲染取证：截出侧视 / 顶视 / 底视三态，人工确认俯仰轴真的能看到顶和底，
 * 且转的只是道具、卡面框架保持不动。
 *
 * 这里刻意不用 `createComposeRule`：它的 composition 跑在 unconfined 调度器上，
 * SceneView 内部 `withContext(Dispatchers.IO)` 读完模型后会在 IO 线程恢复，
 * 而 Filament 只接受创建引擎的那个线程（否则 panic "This thread has not been adopted"）。
 * 走真实 Activity 的 setContent，composition 才落在 AndroidUiDispatcher.Main 上，
 * 也才是线上真正跑的路径。
 *
 * 手势同理用 Instrumentation 注入真实 MotionEvent，顺带验证了
 * SurfaceView 触摸拦截那条链路（见渲染器里的 Initial pass 说明）。
 *
 * 不做像素断言——模型或光照一换就全量变化，断死会天天误报。
 */
@RunWith(AndroidJUnit4::class)
class ItemOrbitPreviewCaptureTest {

    private val instrumentation: Instrumentation
        get() = InstrumentationRegistry.getInstrumentation()

    @Test
    fun captureOrbitPoses() {
        val context = instrumentation.targetContext
        val renderer = SceneViewItemPreviewHostRenderer(
            modelSource = DemoItem3dModelSource(),
            capability = Item3dCapability(context),
        )

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        renderer.Render(
                            content = ItemPreviewContent(
                                // key 挂的是真 GLB（见 DemoItem3dModelSource）
                                imageKey = "key",
                                name = "Name",
                                description = "Add a description Enter a description",
                            ),
                            modifier = Modifier.size(width = CardWidthDp.dp, height = CardHeightDp.dp),
                        )
                    }
                }
            }

            settle()
            dump("orbit_side.png")

            // 450dp 的卡片居中，立绘区只占它上面 74%，换算到整屏约 0.25~0.62。
            // ACTION_DOWN 必须落在这个区间里，压到下方文字区手势就进不了 3D 视口。
            dragVertical(fromRatio = 0.30f, toRatio = 0.58f)
            settle()
            dump("orbit_top.png")

            // 从俯视上限走到仰视下限要 170°，单次拖动只够 138°，拆两段
            dragVertical(fromRatio = 0.58f, toRatio = 0.30f)
            dragVertical(fromRatio = 0.58f, toRatio = 0.30f)
            settle()
            dump("orbit_bottom.png")
        }
    }

    /**
     * 降级取证：道具没有 3D 资产时退回静态卡面，不空屏也不崩。
     *
     * 用未登记的 imageKey 触发，走的是和「设备不支持实时 3D」完全相同的那条分支
     * （见渲染器里 `model == null` 的处理），所以不必伪造设备能力也能覆盖到。
     */
    @Test
    fun captureFallbackWhenModelMissing() {
        val context = instrumentation.targetContext
        val renderer = SceneViewItemPreviewHostRenderer(
            modelSource = DemoItem3dModelSource(),
            capability = Item3dCapability(context),
        )

        ActivityScenario.launch(ComponentActivity::class.java).use {
            it.onActivity { activity ->
                activity.setContent {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        renderer.Render(
                            content = ItemPreviewContent(
                                imageKey = "knife",
                                name = "Name",
                                description = "Add a description Enter a description",
                            ),
                            modifier = Modifier.size(CardWidthDp.dp, CardHeightDp.dp),
                        )
                    }
                }
            }
            settle()
            dump("orbit_fallback.png")
        }
    }

    /** 在屏幕中线上竖直拖动，比例相对整屏高度 */
    private fun dragVertical(fromRatio: Float, toRatio: Float) {
        val metrics = instrumentation.targetContext.resources.displayMetrics
        val x = metrics.widthPixels / 2f
        val fromY = metrics.heightPixels * fromRatio
        val toY = metrics.heightPixels * toRatio
        val downTime = SystemClock.uptimeMillis()

        sendPointer(MotionEvent.ACTION_DOWN, downTime, x, fromY)
        val steps = 16
        for (step in 1..steps) {
            val y = fromY + (toY - fromY) * step / steps
            sendPointer(MotionEvent.ACTION_MOVE, downTime, x, y)
            SystemClock.sleep(16)
        }
        sendPointer(MotionEvent.ACTION_UP, downTime, x, toY)
        // 抬手会带上惯性，等它停下来再截图，否则截到的是滑行中的中间态
        SystemClock.sleep(1200)
    }

    private fun sendPointer(action: Int, downTime: Long, x: Float, y: Float) {
        val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)
        instrumentation.sendPointerSync(event)
        event.recycle()
    }

    /** 首帧要等 Filament 建上下文、读 GLB、上传贴图，比解一张序列帧慢得多 */
    private fun settle() {
        instrumentation.waitForIdleSync()
        SystemClock.sleep(RenderWaitMs)
        instrumentation.waitForIdleSync()
    }

    private fun dump(name: String) {
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        val dir = instrumentation.targetContext.getExternalFilesDir(null)
        FileOutputStream(File(dir, name)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
    }

    private companion object {
        const val RenderWaitMs = 3000L
        const val CardWidthDp = 300
        const val CardHeightDp = 450
    }
}
