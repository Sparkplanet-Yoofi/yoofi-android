package ai.yoofi.app.ui.auth

import ai.yoofi.app.core.common.Outcome
import ai.yoofi.app.domain.avatar.AvatarLocalStore
import ai.yoofi.app.domain.avatar.CameraCapability
import ai.yoofi.app.domain.avatar.PersistEncodedAvatarUseCase
import ai.yoofi.app.domain.avatar.PersistPickedAvatarUseCase
import ai.yoofi.app.domain.avatar.PrepareCameraCaptureUseCase
import ai.yoofi.app.domain.avatar.ResolveTakePhotoUseCase
import ai.yoofi.app.domain.avatar.StageAvatarCropUseCase
import ai.yoofi.app.domain.auth.AuthSession
import ai.yoofi.app.domain.auth.User
import ai.yoofi.app.domain.auth.UserSessionStore
import ai.yoofi.app.domain.profile.MarkProfileCompletedUseCase
import ai.yoofi.app.domain.profile.UpdateProfileUseCase
import ai.yoofi.app.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
    fun `编辑入口保存成功发出 EditSaved`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(
            ProfileSetupIntent.SubmitEdit(displayName = "Jenny", genderKey = "Female"),
        )
        assertEquals(ProfileSetupSideEffect.EditSaved, viewModel.sideEffect.first())
    }

    @Test
    fun `取消选择退出 picking`() {
        val viewModel = viewModel()
        viewModel.onIntent(ProfileSetupIntent.ChooseFromGallery)
        viewModel.onIntent(ProfileSetupIntent.PickerCancelled)
        assertFalse(viewModel.uiState.value.isPicking)
        assertEquals(null, viewModel.uiState.value.avatarPath)
    }

    @Test
    fun `创建成功会把会话标成已完善`() {
        val sessionStore = FakeSessionStore()
        sessionStore.save(
            AuthSession(
                user = User(userId = 1L, nickname = "mock", avatarUrl = ""),
                accessToken = "at",
                refreshToken = "rt",
                accessExpiresIn = 1,
                refreshExpiresIn = 1,
                isNewUser = true,
                profileCompleted = false,
            ),
        )
        val viewModel = viewModel(sessionStore = sessionStore)
        viewModel.onCreateSucceeded()
        assertTrue(sessionStore.currentSession()?.profileCompleted == true)
    }

    private fun viewModel(
        hardware: Boolean = true,
        permission: Boolean = true,
        sessionStore: UserSessionStore = FakeSessionStore(),
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
            updateProfile = UpdateProfileUseCase(),
            markProfileCompleted = MarkProfileCompletedUseCase(sessionStore),
        )
    }
}

private class FakeSessionStore : UserSessionStore {
    private var session: AuthSession? = null

    override fun save(session: AuthSession) {
        this.session = session
    }

    override fun currentUser(): User? = session?.user

    override fun currentAccessToken(): String? = session?.accessToken

    override fun currentSession(): AuthSession? = session

    override fun clear() {
        session = null
    }
}
