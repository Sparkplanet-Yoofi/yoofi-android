package ai.yoofi.app.ui.auth

import ai.yoofi.app.core.common.Outcome
import ai.yoofi.app.domain.avatar.PersistEncodedAvatarUseCase
import ai.yoofi.app.domain.avatar.PersistPickedAvatarUseCase
import ai.yoofi.app.domain.avatar.PrepareCameraCaptureUseCase
import ai.yoofi.app.domain.avatar.ResolveTakePhotoUseCase
import ai.yoofi.app.domain.avatar.StageAvatarCropUseCase
import ai.yoofi.app.domain.avatar.TakePhotoDecision
import ai.yoofi.app.domain.profile.MarkProfileCompletedUseCase
import ai.yoofi.app.domain.profile.ProfileDraft
import ai.yoofi.app.domain.profile.UpdateProfileUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileSetupUiState(
    val showAvatarSheet: Boolean = false,
    val avatarPath: String? = null,
    val cropSourcePath: String? = null,
    /**
     * 同路径覆盖写头像时递增，驱动界面重新解码
     */
    val avatarRevision: Long = 0L,
    val isPicking: Boolean = false,
    val avatarError: Boolean = false,
    /** 仅编辑入口的保存失败；创建入口仍用 Screen 本地态。 */
    val editSaveFailed: Boolean = false,
)

sealed interface ProfileSetupIntent {
    data object OpenAvatarSheet : ProfileSetupIntent
    data object DismissAvatarSheet : ProfileSetupIntent
    data object ChooseFromGallery : ProfileSetupIntent
    data object TakePhoto : ProfileSetupIntent
    data class CameraPermissionResult(
        val granted: Boolean,
        val shouldShowRationale: Boolean,
    ) : ProfileSetupIntent
    data class ImagePicked(val uri: String) : ProfileSetupIntent
    data class CameraCaptureResult(val success: Boolean) : ProfileSetupIntent
    data object PickerCancelled : ProfileSetupIntent
    data object CaptureUnavailable : ProfileSetupIntent
    data object CropCancelled : ProfileSetupIntent
    data object CropFailed : ProfileSetupIntent
    data class CropConfirmed(val encodedPath: String) : ProfileSetupIntent
    data object ClearAvatarError : ProfileSetupIntent

    /** 仅编辑入口使用；创建入口仍走 Screen 里的首次完善逻辑。 */
    data class SubmitEdit(
        val displayName: String,
        val genderKey: String?,
    ) : ProfileSetupIntent
}

sealed interface ProfileSetupSideEffect {
    data object LaunchGallery : ProfileSetupSideEffect
    data class LaunchCamera(val outputUri: String) : ProfileSetupSideEffect
    data object RequestCameraPermission : ProfileSetupSideEffect
    data object OpenAppSettings : ProfileSetupSideEffect

    /** 编辑资料保存成功，由 Screen 关掉编辑页回到 Me。 */
    data object EditSaved : ProfileSetupSideEffect
}

@HiltViewModel
internal class ProfileSetupViewModel @Inject constructor(
    private val resolveTakePhoto: ResolveTakePhotoUseCase,
    private val prepareCameraCapture: PrepareCameraCaptureUseCase,
    private val persistPickedAvatar: PersistPickedAvatarUseCase,
    private val stageAvatarCrop: StageAvatarCropUseCase,
    private val persistEncodedAvatar: PersistEncodedAvatarUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val markProfileCompleted: MarkProfileCompletedUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<ProfileSetupSideEffect>(capacity = Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private var pendingCaptureUri: String? = null
    private var cameraPermissionAsked: Boolean = false

    /**
     * 切换入口时清掉上一入口留下的弹层 / 失败态，头像文件可以沿用。
     * [entry] 留给接创建/编辑拉取接口时按入口预填。
     */
    fun bind(entry: ProfileEditorEntry) {
        _uiState.update {
            it.copy(
                showAvatarSheet = false,
                editSaveFailed = false,
                avatarError = false,
            )
        }
    }

    fun onIntent(intent: ProfileSetupIntent) {
        when (intent) {
            ProfileSetupIntent.OpenAvatarSheet -> {
                if (blockedForSheet()) return
                _uiState.update {
                    it.copy(showAvatarSheet = true, avatarError = false)
                }
            }
            ProfileSetupIntent.DismissAvatarSheet -> {
                _uiState.update { it.copy(showAvatarSheet = false) }
            }
            ProfileSetupIntent.ChooseFromGallery -> startGallery()
            ProfileSetupIntent.TakePhoto -> startTakePhoto()
            is ProfileSetupIntent.CameraPermissionResult -> onPermissionResult(intent)
            is ProfileSetupIntent.ImagePicked -> stageForCrop(intent.uri)
            is ProfileSetupIntent.CameraCaptureResult -> onCameraResult(intent.success)
            ProfileSetupIntent.PickerCancelled -> finishPicking(discardCapture = true)
            ProfileSetupIntent.CaptureUnavailable -> {
                finishPicking(discardCapture = true)
                _uiState.update { it.copy(avatarError = true) }
            }
            ProfileSetupIntent.CropCancelled -> closeCrop(error = false)
            ProfileSetupIntent.CropFailed -> closeCrop(error = true)
            is ProfileSetupIntent.CropConfirmed -> persistCropped(intent.encodedPath)
            ProfileSetupIntent.ClearAvatarError -> {
                _uiState.update { it.copy(avatarError = false) }
            }
            is ProfileSetupIntent.SubmitEdit -> submitEdit(intent)
        }
    }

    /** 创建入口提交成功：会话标已完善，再由 Screen 关页。Skip 不要调。 */
    fun onCreateSucceeded() {
        markProfileCompleted()
    }

    private fun submitEdit(intent: ProfileSetupIntent.SubmitEdit) {
        viewModelScope.launch {
            _uiState.update { it.copy(editSaveFailed = false) }
            when (
                updateProfile(
                    ProfileDraft(
                        displayName = intent.displayName,
                        genderKey = intent.genderKey,
                    ),
                )
            ) {
                is Outcome.Ok -> emit(ProfileSetupSideEffect.EditSaved)
                is Outcome.Err -> _uiState.update { it.copy(editSaveFailed = true) }
            }
        }
    }

    private fun blockedForSheet(): Boolean {
        val state = _uiState.value
        return state.isPicking || state.cropSourcePath != null
    }

    private fun startGallery() {
        if (blockedForSheet()) return
        _uiState.update {
            it.copy(showAvatarSheet = false, isPicking = true, avatarError = false)
        }
        emit(ProfileSetupSideEffect.LaunchGallery)
    }

    private fun startTakePhoto() {
        if (blockedForSheet()) return
        _uiState.update {
            it.copy(showAvatarSheet = false, avatarError = false)
        }
        when (resolveTakePhoto()) {
            TakePhotoDecision.Unavailable -> {
                _uiState.update { it.copy(avatarError = true) }
            }
            TakePhotoDecision.NeedPermission -> {
                _uiState.update { it.copy(isPicking = true) }
                emit(ProfileSetupSideEffect.RequestCameraPermission)
            }
            TakePhotoDecision.Ready -> launchCamera()
        }
    }

    private fun onPermissionResult(result: ProfileSetupIntent.CameraPermissionResult) {
        if (result.granted) {
            cameraPermissionAsked = true
            launchCamera()
            return
        }
        val permanentDeny = !result.shouldShowRationale && cameraPermissionAsked
        cameraPermissionAsked = true
        finishPicking(discardCapture = true)
        if (permanentDeny) {
            emit(ProfileSetupSideEffect.OpenAppSettings)
        }
    }

    private fun launchCamera() {
        val uri = runCatching { prepareCameraCapture() }.getOrNull()
        if (uri.isNullOrBlank()) {
            finishPicking(discardCapture = false)
            _uiState.update { it.copy(avatarError = true) }
            return
        }
        pendingCaptureUri = uri
        _uiState.update { it.copy(isPicking = true, showAvatarSheet = false) }
        emit(ProfileSetupSideEffect.LaunchCamera(uri))
    }

    private fun onCameraResult(success: Boolean) {
        val uri = pendingCaptureUri
        pendingCaptureUri = null
        if (!success || uri.isNullOrBlank()) {
            if (!uri.isNullOrBlank()) {
                prepareCameraCapture.discard(uri)
            }
            finishPicking(discardCapture = false)
            return
        }
        // 与相册一致：先拷到缓存再进裁剪页，不直接写头像。
        stageForCrop(uri, discardCaptureAfter = true)
    }

    private fun stageForCrop(
        sourceUri: String,
        discardCaptureAfter: Boolean = false,
    ) {
        viewModelScope.launch {
            val path = stageAvatarCrop(sourceUri)
            if (discardCaptureAfter) {
                prepareCameraCapture.discard(sourceUri)
            }
            if (path == null) {
                finishPicking(discardCapture = false)
                _uiState.update { it.copy(avatarError = true) }
            } else {
                _uiState.update {
                    it.copy(
                        cropSourcePath = path,
                        isPicking = false,
                        showAvatarSheet = false,
                        avatarError = false,
                    )
                }
            }
        }
    }

    private fun persistCropped(encodedPath: String) {
        viewModelScope.launch {
            val path = persistEncodedAvatar(encodedPath)
            stageAvatarCrop.discard()
            if (path == null) {
                closeCrop(error = true)
            } else {
                _uiState.update {
                    it.copy(
                        avatarPath = path,
                        cropSourcePath = null,
                        avatarRevision = it.avatarRevision + 1L,
                        isPicking = false,
                        showAvatarSheet = false,
                        avatarError = false,
                    )
                }
            }
        }
    }

    private fun closeCrop(error: Boolean) {
        stageAvatarCrop.discard()
        _uiState.update {
            it.copy(
                cropSourcePath = null,
                isPicking = false,
                avatarError = error,
            )
        }
    }

    private fun persist(sourceUri: String) {
        viewModelScope.launch {
            val path = persistPickedAvatar(sourceUri)
            prepareCameraCapture.discard(sourceUri)
            if (path == null) {
                finishPicking(discardCapture = false)
                _uiState.update { it.copy(avatarError = true) }
            } else {
                pendingCaptureUri = null
                _uiState.update {
                    it.copy(
                        avatarPath = path,
                        avatarRevision = it.avatarRevision + 1L,
                        isPicking = false,
                        showAvatarSheet = false,
                        avatarError = false,
                    )
                }
            }
        }
    }

    private fun finishPicking(discardCapture: Boolean) {
        val uri = pendingCaptureUri
        pendingCaptureUri = null
        if (discardCapture && !uri.isNullOrBlank()) {
            prepareCameraCapture.discard(uri)
        }
        _uiState.update { it.copy(isPicking = false) }
    }

    private fun emit(effect: ProfileSetupSideEffect) {
        _sideEffect.trySend(effect)
    }
}
