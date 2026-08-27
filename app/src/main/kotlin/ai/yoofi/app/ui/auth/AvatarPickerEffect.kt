package ai.yoofi.app.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

/**
 * 承接 ViewModel 副作用：相册 Photo Picker、系统相机、CAMERA 权限。
 * 相册走系统选择器，不申请 READ_MEDIA / 存储权限（Play 政策）。
 */
@Composable
internal fun AvatarPickerEffect(viewModel: ProfileSetupViewModel) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) {
            viewModel.onIntent(ProfileSetupIntent.PickerCancelled)
        } else {
            viewModel.onIntent(ProfileSetupIntent.ImagePicked(uri.toString()))
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        viewModel.onIntent(ProfileSetupIntent.CameraCaptureResult(success))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val activity = context as? Activity
        val rationale = activity != null &&
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA,
            )
        viewModel.onIntent(
            ProfileSetupIntent.CameraPermissionResult(
                granted = granted,
                shouldShowRationale = rationale,
            ),
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                ProfileSetupSideEffect.LaunchGallery -> {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                        ),
                    )
                }
                is ProfileSetupSideEffect.LaunchCamera -> {
                    val output = runCatching { Uri.parse(effect.outputUri) }.getOrNull()
                    if (output == null) {
                        viewModel.onIntent(ProfileSetupIntent.CaptureUnavailable)
                    } else {
                        val launched = runCatching { cameraLauncher.launch(output) }.isSuccess
                        if (!launched) {
                            viewModel.onIntent(ProfileSetupIntent.CaptureUnavailable)
                        }
                    }
                }
                ProfileSetupSideEffect.RequestCameraPermission -> {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
                ProfileSetupSideEffect.OpenAppSettings -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    runCatching { context.startActivity(intent) }
                }
            }
        }
    }
}
