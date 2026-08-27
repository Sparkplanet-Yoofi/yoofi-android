package ai.yoofi.app.domain.avatar

/**
 * 相机硬件与运行时权限，实现放 data，domain 不碰 android。
 */
interface CameraCapability {
    fun hasHardware(): Boolean

    fun hasPermission(): Boolean
}
