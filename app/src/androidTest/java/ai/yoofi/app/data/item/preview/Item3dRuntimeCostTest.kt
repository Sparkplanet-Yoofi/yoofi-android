package ai.yoofi.app.data.item.preview

import ai.yoofi.app.core.item.preview.ItemPreviewContent
import android.app.ActivityManager
import android.app.Instrumentation
import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.SystemClock
import android.view.Choreographer
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 实时 3D 的运行时开销取证：帧节奏、内存占用、反复进出是否泄漏。
 *
 * 存在的理由是包体之外的另一半代价——Filament 要起一套 GL 上下文、一份 IBL、一份贴图，
 * 这些在低内存机上是真实压力，光看 APK 大小看不出来。数据落盘到
 * `item3d_runtime_cost.txt`，换设备、换模型、升 SceneView 之后重跑对比。
 *
 * 断言只拦「明显坏掉」（渲染根本没跑起来、引擎资源没释放），不拦性能波动——
 * 设备之间差异太大，卡严了只会天天误报。真实数字看落盘文件。
 */
@RunWith(AndroidJUnit4::class)
class Item3dRuntimeCostTest {

    private val instrumentation: Instrumentation
        get() = InstrumentationRegistry.getInstrumentation()

    private val context: Context
        get() = instrumentation.targetContext

    /** 控制预览的挂载与卸载，用来验证引擎资源能否随之释放 */
    private val mounted = mutableStateOf(true)

    private val report = StringBuilder()

    /** 持续拖动下的帧节奏与内存占用 */
    @Test
    fun steadyStateCost() {
        report.appendLine("=== 设备 ===")
        report.appendLine("model=${Build.MODEL} sdk=${Build.VERSION.SDK_INT}")
        report.appendLine("lowRam=${context.isLowRamDevice()} totalMem=${context.totalMemMb()}MB")
        report.appendLine("supportsRealtime3d=${Item3dCapability(context).supportsRealtime3d}")

        val baseline = pssMb()
        report.appendLine("=== 稳态开销 ===")
        report.appendLine("baseline PSS=${baseline}MB")
        report.append(memoryBreakdown())

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.installContent()
            settle(FirstFrameWaitMs)

            val loaded = pssMb()
            report.appendLine("模型加载后 PSS=${loaded}MB（增量 ${loaded - baseline}MB）")
            report.append(memoryBreakdown())

            // 不注入手势：SceneView 每帧都无条件重绘整个场景，负载与有没有交互无关，
            // 拖动只是多算一个相机矩阵。静置采集反而更稳，也不受输入注入被系统限流的干扰。
            val frames = recordFrames(scenario) { SystemClock.sleep(SampleDurationMs) }

            val peak = pssMb()
            report.appendLine("拖动中 PSS=${peak}MB（增量 ${peak - baseline}MB）")
            report.append(frames.describe())

            assertTrue("渲染没跑起来，只收到 ${frames.count} 帧", frames.count > MinFrames)
            assertTrue("帧节奏严重劣化，中位帧长 ${frames.medianMs}ms", frames.medianMs < MaxMedianMs)
        }
        flush()
    }

    /**
     * 反复进出预览，确认引擎、模型、环境这些 Filament 资源都随 DisposableEffect 回收了。
     *
     * 卡的是「持续增长」而不是「占用绝对值」——后者受 GL 驱动缓存影响，不可控。
     */
    @Test
    fun repeatedMountDoesNotLeak() {
        report.appendLine("=== 反复进出 ===")
        val baseline = pssMb()
        report.appendLine("启动前 PSS=${baseline}MB")

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.installContent()

            // 先跑一轮把 GL 驱动、着色器编译这些一次性开销撑满，再取基线，
            // 否则第一轮的自然增长会被误判成泄漏
            settle(FirstFrameWaitMs)
            report.appendLine("挂载中 PSS=${pssMb()}MB")
            scenario.setMounted(false)
            settle(TeardownWaitMs)

            val warm = pssMb()
            report.appendLine("卸载后 PSS=${warm}MB（相对启动前 +${warm - baseline}MB）")
            report.append(memoryBreakdown())

            repeat(MountCycles) { cycle ->
                scenario.setMounted(true)
                settle(RemountWaitMs)
                scenario.setMounted(false)
                settle(TeardownWaitMs)
                report.appendLine("第 ${cycle + 1} 轮结束 PSS=${pssMb()}MB")
            }

            val growth = pssMb() - warm
            report.appendLine("$MountCycles 轮净增长=${growth}MB")

            // 再多等一会儿：Filament 的资源销毁是异步的，GL 驱动也会缓着不立刻还给系统，
            // 短等待下的残留读数会把「延迟归还」误报成「没释放」
            settle(DrainWaitMs)
            val drained = pssMb()
            report.appendLine("静置 ${DrainWaitMs / 1000}s 后 PSS=${drained}MB（相对启动前 +${drained - baseline}MB）")
            report.append(memoryBreakdown())

            assertTrue("疑似泄漏：$MountCycles 轮进出后净增 ${growth}MB", growth < LeakBudgetMb)
        }
        flush()
    }

    // -- 场景挂载 ------------------------------------------------------------

    private fun ActivityScenario<ComponentActivity>.installContent() = onActivity { activity ->
        val renderer = SceneViewItemPreviewHostRenderer(
            modelSource = DemoItem3dModelSource(),
            capability = Item3dCapability(activity),
        )
        activity.setContent {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (mounted.value) {
                    renderer.Render(
                        content = PreviewContent,
                        modifier = Modifier.size(CardWidthDp.dp, CardHeightDp.dp),
                    )
                }
            }
        }
    }

    private fun ActivityScenario<ComponentActivity>.setMounted(value: Boolean) = onActivity {
        mounted.value = value
    }

    // -- 帧节奏采集 ----------------------------------------------------------

    /**
     * Choreographer 的节拍能反映主线程有没有被渲染拖住：SceneView 的帧循环挂在
     * `withFrameNanos` 上，GPU 跟不上时这里会一起被拉长。
     */
    private fun recordFrames(
        scenario: ActivityScenario<ComponentActivity>,
        action: () -> Unit,
    ): FrameStats {
        val intervalsNanos = mutableListOf<Long>()
        val collecting = AtomicBoolean(true)
        val started = CountDownLatch(1)

        scenario.onActivity {
            Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
                private var last = 0L
                override fun doFrame(frameTimeNanos: Long) {
                    if (last != 0L) intervalsNanos += frameTimeNanos - last
                    last = frameTimeNanos
                    if (collecting.get()) Choreographer.getInstance().postFrameCallback(this)
                }
            })
            started.countDown()
        }
        started.await(LatchTimeoutSec, TimeUnit.SECONDS)

        action()

        collecting.set(false)
        instrumentation.waitForIdleSync()
        return FrameStats(intervalsNanos.map { it / 1_000_000f })
    }

    private class FrameStats(intervalsMs: List<Float>) {
        private val sorted = intervalsMs.sorted()
        val count get() = sorted.size
        val medianMs: Float get() = sorted.getOrElse(sorted.size / 2) { 0f }
        private val p95Ms: Float get() = sorted.getOrElse((sorted.size * 0.95f).toInt()) { 0f }
        private val janky get() = sorted.count { it > JankThresholdMs }

        fun describe() = buildString {
            appendLine(
                "帧数=$count 中位=${medianMs.fmt()}ms p95=${p95Ms.fmt()}ms " +
                    "估算 fps=${(if (medianMs > 0) 1000 / medianMs else 0f).fmt()}",
            )
            appendLine("超 ${JankThresholdMs}ms 的帧=$janky / $count")
        }

        private fun Float.fmt() = "%.1f".format(this)
    }

    // -- 采样 ----------------------------------------------------------------

    private fun pssMb(): Int {
        // 先催一次 GC，否则读到的是还没回收的垃圾，泄漏判断会失真
        Runtime.getRuntime().gc()
        SystemClock.sleep(GcSettleMs)
        val info = Debug.MemoryInfo()
        Debug.getMemoryInfo(info)
        return info.totalPss / 1024
    }

    /**
     * 拆开看这笔内存花在哪。graphics 是 GPU 侧的纹理与渲染目标，
     * 它决定了低端机能不能扛住——Java 堆限制管不到这块，但 LMK 会照杀。
     */
    private fun memoryBreakdown(): String {
        val info = Debug.MemoryInfo()
        Debug.getMemoryInfo(info)
        return MemorySummaryKeys.joinToString(
            prefix = "  分项(MB): ",
            separator = " ",
        ) { key ->
            val mb = (info.getMemoryStat("summary.$key")?.toIntOrNull() ?: 0) / 1024
            "$key=$mb"
        } + "\n"
    }

    private fun settle(waitMs: Long) {
        instrumentation.waitForIdleSync()
        SystemClock.sleep(waitMs)
        instrumentation.waitForIdleSync()
    }

    private fun Context.isLowRamDevice() =
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).isLowRamDevice

    private fun Context.totalMemMb(): Long {
        val info = ActivityManager.MemoryInfo()
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(info)
        return info.totalMem / (1024 * 1024)
    }

    private fun flush() {
        File(context.getExternalFilesDir(null), "item3d_runtime_cost.txt")
            .appendText(report.toString())
        report.clear()
    }

    private companion object {
        const val CardWidthDp = 300
        const val CardHeightDp = 450
        const val FirstFrameWaitMs = 3000L
        const val RemountWaitMs = 2500L
        const val TeardownWaitMs = 1500L
        const val DrainWaitMs = 5000L
        const val GcSettleMs = 300L
        const val LatchTimeoutSec = 5L
        const val SampleDurationMs = 5000L
        const val MountCycles = 6
        const val JankThresholdMs = 20f

        /** 低于这个帧数说明渲染压根没起来，而不是慢 */
        const val MinFrames = 30

        /** 中位帧长上限，约合 20fps。卡的是「卡成幻灯片」，不是「没到 60fps」 */
        const val MaxMedianMs = 50f

        /** 6 轮进出的内存净增上限。GL 驱动缓存有正常波动，卡太死会误报 */
        const val LeakBudgetMb = 40

        val MemorySummaryKeys = listOf("java-heap", "native-heap", "graphics", "code", "system")

        val PreviewContent = ItemPreviewContent(
            imageKey = "key",
            name = "Name",
            description = "Add a description Enter a description",
        )
    }
}
