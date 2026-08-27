package ai.yoofi.app.ui.auth

import ai.yoofi.app.core.common.Outcome
import ai.yoofi.app.domain.avatar.AvatarLocalStore
import ai.yoofi.app.domain.avatar.CameraCapability
import ai.yoofi.app.domain.avatar.PersistEncodedAvatarUseCase
import ai.yoofi.app.domain.avatar.PersistPickedAvatarUseCase
import ai.yoofi.app.domain.avatar.PrepareCameraCaptureUseCase
import ai.yoofi.app.domain.avatar.ResolveTakePhotoUseCase
import ai.yoofi.app.domain.avatar.StageAvatarCropUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileSetupViewModelTest {

    @Test
    fun `打开相册会关掉弹层并进入 picking`() {
        val viewModel = viewModel()
        viewModel.onIntent(ProfileSetupIntent.OpenAvatarSheet)
        assertTrue(viewModel.uiState.value.showAvatarSheet)
        viewModel.onIntent(ProfileSetupIntent.ChooseFromGallery)
        assertFalse(viewModel.uiState.value.showAvatarSheet)
        assertTrue(viewModel.uiState.value.isPicking)
    }

    @Test
    fun `picking 中忽略重复打开弹层`() {
        val viewModel = viewModel()
        viewModel.onIntent(ProfileSetupIntent.ChooseFromGallery)
        viewModel.onIntent(ProfileSetupIntent.OpenAvatarSheet)
        assertFalse(viewModel.uiState.value.showAvatarSheet)
    }

    @Test
    fun `无摄像头硬件则报错且不进入 picking`() {
        val viewModel = viewModel(hardware = false)
        viewModel.onIntent(ProfileSetupIntent.TakePhoto)
        assertTrue(viewModel.uiState.value.avatarError)
        assertFalse(viewModel.uiState.value.isPicking)
    }

    @Test
    fun `取消选择退出 picking`() {
        val viewModel = viewModel()
        viewModel.onIntent(ProfileSetupIntent.ChooseFromGallery)
        viewModel.onIntent(ProfileSetupIntent.PickerCancelled)
        assertFalse(viewModel.uiState.value.isPicking)
        assertEquals(null, viewModel.uiState.value.avatarPath)
    }

    private fun viewModel(
        hardware: Boolean = true,
        permission: Boolean = true,
    ): ProfileSetupViewModel {
        val store = object : AvatarLocalStore {
            override fun createCaptureUri(): String = "content://yoofi/capture.jpg"

            override suspend fun persistFromUri(sourceUri: String): Outcome<String> {
                return Outcome.Ok("/tmp/avatar.jpg")
            }

            override suspend fun stageFromUri(sourceUri: String): Outcome<String> {
                return Outcome.Ok("/tmp/crop_source.jpg")
            }

            override suspend fun persistEncodedFile(sourcePath: String): Outcome<String> {
                return Outcome.Ok("/tmp/avatar.jpg")
            }

            override fun discardCapture(uri: String) = Unit

            override fun discardStagedCrop() = Unit
        }
        val camera = object : CameraCapability {
            override fun hasHardware(): Boolean = hardware

            override fun hasPermission(): Boolean = permission
        }
        return ProfileSetupViewModel(
            resolveTakePhoto = ResolveTakePhotoUseCase(camera),
            prepareCameraCapture = PrepareCameraCaptureUseCase(store),
            persistPickedAvatar = PersistPickedAvatarUseCase(store),
            stageAvatarCrop = StageAvatarCropUseCase(store),
            persistEncodedAvatar = PersistEncodedAvatarUseCase(store),
        )
    }
}
