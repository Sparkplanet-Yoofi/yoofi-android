package ai.yoofi.app.data.item.preview

import ai.yoofi.shared.item.orbit.Item3dModel
import ai.yoofi.shared.item.orbit.Item3dModelSource
import ai.yoofi.shared.item.orbit.OrbitCameraPreset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demo 模型来源，只读包内资产、不打网。真实来源见 [RemoteItem3dModelSource]。
 *
 * ⚠️ **包内这个 `helmet.glb` 是 Khronos 的 DamagedHelmet 公开样例，不对应任何一件道具。**
 * 它只是用来验证渲染管线，所以点开钥匙看到的是头盔——不是加载错了，是道具自己的 GLB
 * 还没产出（卡在 §9 第 4 项的 image-to-3D 密钥）。每件道具显示自己的模型，
 * 要等服务端按 §12 的契约下发各自的 `model.url`，那时把 `DemoFeature.Item3dPreview`
 * 切到真实实现即可，渲染层一行不动。
 *
 * 只登记一件道具是有意的：留着其余道具走静态卡面，降级路径在开发阶段才是可见可测的
 * （见 `ItemOrbitPreviewCaptureTest.captureFallbackWhenModelMissing`）。
 * 服务端真实数据同样会是「部分道具有 3D」，这里保持同构。
 */
@Singleton
class DemoItem3dModelSource @Inject constructor() : Item3dModelSource {

    override suspend fun load(itemKey: String): Item3dModel? = DemoModels[itemKey]

    private companion object {
        /**
         * 已登记 3D 资产的道具。真资产就位后逐条替换 [placeholderModel]，
         * 未登记的道具自动退回静态卡面，不会空屏。
         */
        val DemoModels = mapOf(
            "key" to placeholderModel("key"),
        )

        /**
         * 用样例模型顶一件道具的位。
         *
         * 相机压到 2.2 是因为这个头盔比多数道具「胖」，用默认距离会顶到视口边缘；
         * 换成真道具后这些参数由服务端随模型一起下发，不再写死在客户端。
         */
        fun placeholderModel(itemKey: String): Item3dModel = Item3dModel(
            itemId = itemKey,
            modelUri = "${Item3dModel.ASSET_SCHEME}models/helmet.glb",
            // 略微俯视开场：正视图看不出立体感，抬高一点能同时看到顶面和正面
            camera = OrbitCameraPreset(baseDistance = 2.2f, initialPitchDeg = 14f),
            // 暖调近黑，接住卡面的金框又不抢道具；纯黑会让暗部细节糊成一片
            backgroundRgb = 0x1C1408,
        )
    }
}
