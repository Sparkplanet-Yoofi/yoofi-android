package ai.yoofi.app.domain.avatar

/**
 * 点击拍照后的下一步，不含 Android API
 */
enum class TakePhotoDecision {
    /**
     * 无摄像头硬件
     */
    Unavailable,
    /**
     * 未授 CAMERA，UI 去请求
     */
    NeedPermission,
    /**
     * 可以调系统相机
     */
    Ready,
}
