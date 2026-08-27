package ai.yoofi.app.domain.avatar

/**
 * 拍照前先判硬件与权限，避免无相机设备直接崩溃。
 */
class ResolveTakePhotoUseCase(
    private val camera: CameraCapability,
) {
    operator fun invoke(): TakePhotoDecision = when {
        !camera.hasHardware() -> TakePhotoDecision.Unavailable
        !camera.hasPermission() -> TakePhotoDecision.NeedPermission
        else -> TakePhotoDecision.Ready
    }
}
