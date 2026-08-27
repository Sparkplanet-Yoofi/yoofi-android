package ai.yoofi.app.domain.avatar

import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveTakePhotoUseCaseTest {

    @Test
    fun `无摄像头硬件则不可用`() {
        val useCase = ResolveTakePhotoUseCase(
            FakeCameraCapability(hardware = false, permission = true),
        )
        assertEquals(TakePhotoDecision.Unavailable, useCase())
    }

    @Test
    fun `有硬件无权限则请求授权`() {
        val useCase = ResolveTakePhotoUseCase(
            FakeCameraCapability(hardware = true, permission = false),
        )
        assertEquals(TakePhotoDecision.NeedPermission, useCase())
    }

    @Test
    fun `有硬件且已授权则直接拍照`() {
        val useCase = ResolveTakePhotoUseCase(
            FakeCameraCapability(hardware = true, permission = true),
        )
        assertEquals(TakePhotoDecision.Ready, useCase())
    }
}

private class FakeCameraCapability(
    private val hardware: Boolean,
    private val permission: Boolean,
) : CameraCapability {
    override fun hasHardware(): Boolean = hardware

    override fun hasPermission(): Boolean = permission
}
