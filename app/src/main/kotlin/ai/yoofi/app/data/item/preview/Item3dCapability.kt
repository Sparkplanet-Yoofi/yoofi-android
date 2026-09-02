package ai.yoofi.app.data.item.preview

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 实时 3D 的设备准入判定。
 *
 * manifest 里把 OpenGL ES 3.0 声明成了可选（不让 Play 按它过滤设备），代价就是
 * 运行时必须自己确认能力，否则在老设备上会崩在 Filament 初始化。
 */
@Singleton
class Item3dCapability @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    /**
     * 除了硬性的 GLES 版本，内存偏小的设备也一并挡掉。
     *
     * 依据是真机实测（Pixel 7a，见 `Item3dRuntimeCostTest`）：开一次预览常驻涨 245MB，
     * 其中 graphics 150MB、native 72MB。这两块都不受 Java 堆上限约束，但 LMK 照杀，
     * 在小内存机上就是「看个道具把后台的自己搞没了」。
     *
     * 只看 `isLowRamDevice` 不够——厂商一般只给 1GB 以下的机器打这个标记，
     * 3GB 的低端机会整片漏网，所以再卡一道总内存。
     */
    val supportsRealtime3d: Boolean by lazy {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return@lazy false
        if (manager.isLowRamDevice) return@lazy false

        val memory = ActivityManager.MemoryInfo().also(manager::getMemoryInfo)
        if (memory.totalMem < MinTotalMemBytes) return@lazy false

        manager.deviceConfigurationInfo.reqGlEsVersion >= GlEs30
    }

    private companion object {
        /** reqGlEsVersion 高 16 位是主版本，低 16 位是次版本 */
        const val GlEs30 = 0x30000

        /**
         * 总内存门槛。注意 totalMem 报的是可用物理内存，比标称低一档
         * （标称 8GB 的 Pixel 7a 报 7447MB），所以这条实际挡掉的是标称 4GB 以下的机器。
         */
        const val MinTotalMemBytes = 3L * 1024 * 1024 * 1024
    }
}
