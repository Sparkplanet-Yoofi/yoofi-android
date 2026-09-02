package ai.yoofi.app.data.item.preview

import ai.yoofi.app.core.item.preview.ItemPreviewContent
import ai.yoofi.app.core.item.preview.ItemPreviewHostRenderer
import ai.yoofi.shared.item.orbit.Item3dModel
import ai.yoofi.shared.item.orbit.Item3dModelSource
import ai.yoofi.shared.item.orbit.OrbitCamera
import ai.yoofi.shared.item.orbit.OrbitEngine
import ai.yoofi.shared.item.orbit.OrbitState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.google.android.filament.Renderer
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.SurfaceType
import io.github.sceneview.createEnvironment
import io.github.sceneview.createRenderer
import io.github.sceneview.node.CameraNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberRenderer
import io.github.sceneview.safeDestroySkybox
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.tan

/**
 * 道具实时 3D 预览：卡面框架不动，只有立绘区里的道具支持 360° 环绕、捏合缩放、放大后平移。
 *
 * 相对早先的序列帧方案，这里把「预渲染一堆角度的图」换成「运行时解相机位姿」，
 * 于是任意角度都有画面而不是就近查表，素材也从每个道具上百张图变成一个 GLB。
 *
 * 分工与序列帧时代一致：交互与相机数学全在 shared（[OrbitEngine] / [OrbitCamera]），
 * 本文件只做三件平台相关的事——采集手势、把位姿写进 Filament 相机、驱动帧循环。
 * 本文件是**唯一**允许 import `io.github.sceneview` 的地方。
 *
 * 设备不支持实时 3D、道具没有模型、或模型还在加载时，自动退回静态卡面，不空屏也不报错。
 */
@Singleton
class SceneViewItemPreviewHostRenderer @Inject constructor(
    private val modelSource: Item3dModelSource,
    private val capability: Item3dCapability,
) : ItemPreviewHostRenderer {

    @Composable
    override fun Render(
        content: ItemPreviewContent,
        modifier: Modifier,
    ) {
        val model by produceState<Item3dModel?>(initialValue = null, content.imageKey) {
            value = if (capability.supportsRealtime3d) modelSource.load(content.imageKey) else null
        }
        val current = model
        ItemPreviewCardFace(
            content = content,
            modifier = modifier.clip(ItemPreviewCardShape),
            art = { artModifier ->
                if (current != null) {
                    // 相机、模型这些引擎对象都只在首次组合时建，换道具必须整块重建，
                    // 否则新道具会沿用上一件的焦距和缩放基准
                    key(current.itemId) {
                        OrbitArt(
                            model = current,
                            fallback = { fallbackModifier ->
                                StaticItemArt(
                                    imageKey = content.imageKey,
                                    contentDescription = content.name,
                                    modifier = fallbackModifier,
                                )
                            },
                            modifier = artModifier,
                        )
                    }
                } else {
                    StaticItemArt(
                        imageKey = content.imageKey,
                        contentDescription = content.name,
                        modifier = artModifier,
                    )
                }
            },
        )
    }
}

@Composable
private fun OrbitArt(
    model: Item3dModel,
    fallback: @Composable (Modifier) -> Unit,
    modifier: Modifier,
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    val instance = rememberModelInstance(modelLoader, model.loadLocation)

    // 环境只留 IBL，天空盒置空。道具预览要的是这张 IBL 提供的打光，
    // 而天空盒是块纯色幕布，留着只会把下面的清屏色盖掉，背景就没法配置了。
    //
    // 工厂先 remember 住再交出去——SceneView 这几个 remember* 助手把工厂 lambda 当 remember 的
    // key，而捕获了变量的 lambda 每次重组都是新对象，不固定住引擎资源会随重组反复重建。
    val environmentFactory = remember(environmentLoader) {
        {
            // 顶层工厂只肯把 IBL 和纯色天空盒捆在一起给，接过来拆掉天空盒再重新组装
            val prefab = createEnvironment(environmentLoader, isOpaque = true)
            prefab.skybox?.let(environmentLoader.engine::safeDestroySkybox)
            environmentLoader.createEnvironment(indirectLight = prefab.indirectLight)
        }
    }
    val environment = rememberEnvironment(
        environmentLoader = environmentLoader,
        environment = environmentFactory,
    )

    // 视口底色。3D 渲染面独立于 Compose 图层，卡面底纹透不上来（TextureView 的 isOpaque
    // 只管 Android 侧合成，Filament 的 SwapChain 没开 CONFIG_TRANSPARENT 就没有 alpha 通道），
    // 所以背景只能由 Filament 清出来。clearColor 收线性值，设计给的 sRGB 得先解码，否则偏亮。
    val rendererFactory = remember(engine) { { createRenderer(engine) } }
    val renderer = rememberRenderer(engine = engine, creator = rendererFactory)
    val clearOptions = remember(model.backgroundRgb) {
        Renderer.ClearOptions().apply {
            clear = true
            clearColor = floatArrayOf(
                srgbToLinear(model.backgroundRed),
                srgbToLinear(model.backgroundGreen),
                srgbToLinear(model.backgroundBlue),
                1f,
            )
        }
    }
    SideEffect {
        renderer.clearOptions = clearOptions
    }

    var state by remember(model.itemId) {
        mutableStateOf(
            OrbitState(
                yawDeg = model.camera.initialYawDeg,
                pitchDeg = model.camera.initialPitchDeg,
            ),
        )
    }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var lastFrameNanos by remember(model.itemId) { mutableLongStateOf(0L) }

    val cameraNode = rememberCameraNode(engine) {
        focalLength = focalLengthOf(model.camera.verticalFovDeg)
        // 放大到上限时相机会贴得很近，默认 near 足够小，这里只把 far 收紧以保住深度精度
        far = FarPlane
    }

    Box(
        modifier = modifier
            .onSizeChanged { viewport = it }
            // SceneView 内部对 SurfaceView 设了恒返回 true 的 OnTouchListener，
            // 事件到不了 Main pass，只能在 Initial 阶段抢下来。顺带也挡住了外层弹窗跟着一起拖。
            .pointerInput(model.itemId) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val tracker = VelocityTracker()
                    var event: PointerEvent
                    do {
                        event = awaitPointerEvent(PointerEventPass.Initial)

                        val zoom = event.calculateZoom()
                        if (zoom != 1f) {
                            val centroid = event.calculateCentroid(useCurrent = true)
                            state = OrbitEngine.onZoom(
                                state = state,
                                zoomFactor = zoom,
                                focusXRatio = centroid.x / size.width - 0.5f,
                                focusYRatio = centroid.y / size.height - 0.5f,
                                limits = model.limits,
                            )
                        }
                        val pan = event.calculatePan()
                        if (pan.x != 0f || pan.y != 0f) {
                            state = OrbitEngine.onDrag(
                                state = state,
                                dragXPx = pan.x,
                                dragYPx = pan.y,
                                viewportWidthPx = size.width.toFloat(),
                                viewportHeightPx = size.height.toFloat(),
                                limits = model.limits,
                            )
                        }
                        // 单指时才记录甩动速度，多指是缩放不该产生环绕惯性
                        if (event.changes.size == 1) {
                            val change = event.changes[0]
                            tracker.addPosition(change.uptimeMillis, change.position)
                        }
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })

                    state = OrbitEngine.onRelease(
                        state = state,
                        velocityXPxPerSec = tracker.calculateVelocity().x,
                        viewportWidthPx = size.width.toFloat(),
                        limits = model.limits,
                    )
                }
            },
    ) {
        if (instance == null) {
            // 模型未就绪（加载中或该道具没有 3D 资产）先顶上静态立绘，避免弹窗里出现空洞
            fallback(Modifier.fillMaxSize())
            return@Box
        }

        Scene(
            modifier = Modifier.fillMaxSize(),
            // 用 TextureSurface 而非默认的 SurfaceView：预览是弹在对话框里的，SurfaceView
            // 走独立合成层、不参与常规 z-order，会穿到卡面之上；TextureView 老实待在层级里。
            surfaceType = SurfaceType.TextureSurface,
            engine = engine,
            modelLoader = modelLoader,
            environmentLoader = environmentLoader,
            renderer = renderer,
            environment = environment,
            cameraNode = cameraNode,
            // 关掉内置的轨道控制器，改由 OrbitEngine 统一三端手势语义
            cameraManipulator = null,
            onGestureListener = null,
            onFrame = { frameTimeNanos ->
                val previous = lastFrameNanos
                lastFrameNanos = frameTimeNanos
                if (previous != 0L && !OrbitEngine.isIdle(state)) {
                    state = OrbitEngine.advance(state, (frameTimeNanos - previous) / NanosPerSecond)
                }
                applyCamera(cameraNode, state, model, viewport)
            },
        ) {
            ModelNode(
                modelInstance = instance,
                // 把最长边归一化到 1 个单位，相机预设才能对所有道具通用
                scaleToUnits = 1f,
                apply = {
                    // 归一化只改缩放不改位置，模型原点未必在几何中心；不修正的话
                    // 转起来会绕着偏心的轴甩。centerOrigin(0,0,0) 是空操作，帮不上忙。
                    position = -center * scale
                },
            )
        }
    }
}

/**
 * 把观察状态写进 Filament 相机。
 *
 * 放在 onFrame 而不是 LaunchedEffect：相机是命令式对象，跟着渲染节拍更新才不会与
 * 引擎自己的帧循环错位，惯性滑行时也不会因为重组时机而抖。
 */
private fun applyCamera(
    cameraNode: CameraNode,
    state: OrbitState,
    model: Item3dModel,
    viewport: IntSize,
) {
    if (viewport.width <= 0 || viewport.height <= 0) return
    val pose = OrbitCamera.poseOf(
        state = state,
        baseDistance = model.camera.baseDistance,
        verticalFovDeg = model.camera.verticalFovDeg,
        aspect = viewport.width.toFloat() / viewport.height.toFloat(),
    )
    cameraNode.worldPosition = Float3(pose.eye.x, pose.eye.y, pose.eye.z)
    cameraNode.lookAt(
        targetWorldPosition = Float3(pose.target.x, pose.target.y, pose.target.z),
        upDirection = Float3(pose.up.x, pose.up.y, pose.up.z),
        smooth = false,
    )
}

/**
 * 垂直视场角换算成镜头焦距。
 *
 * shared 用视场角描述相机（RealityKit、Three.js 都是这套），Filament 却按 35mm 等效焦距
 * 建投影矩阵，换算就落在适配层。取 12 是因为全画幅传感器高 24mm，半高即 12mm。
 */
private fun focalLengthOf(verticalFovDeg: Float): Double =
    (SensorHalfHeightMm / tan(verticalFovDeg * DegToRad / 2f)).toDouble()

/**
 * sRGB 传输函数的逆变换。Filament 的颜色接口一律收线性值。
 *
 * 注意底色和模型贴图一样要过引擎的色调映射，最终成像会比设计稿的色号暗一档。
 * 这是整条管线的一致行为，不是这里算错了——底色要对味请在真机上调，别拿取色器比设计稿。
 */
private fun srgbToLinear(channel: Float): Float =
    if (channel <= 0.04045f) channel / 12.92f else ((channel + 0.055f) / 1.055f).pow(2.4f)

private const val SensorHalfHeightMm = 12f
private const val DegToRad = (PI / 180.0).toFloat()
private const val NanosPerSecond = 1_000_000_000f
private const val FarPlane = 50f
