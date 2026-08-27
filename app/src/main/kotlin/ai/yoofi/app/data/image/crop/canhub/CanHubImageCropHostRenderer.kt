package ai.yoofi.app.data.image.crop.canhub

import ai.yoofi.app.core.image.crop.ImageCropHostRenderer
import ai.yoofi.app.core.image.crop.ImageCropSession
import ai.yoofi.app.core.image.crop.ImageCropSpec
import android.graphics.Color
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.canhub.cropper.CropImageView
import java.io.File
import javax.inject.Inject

/**
 * CanHub 裁剪视口。全项目只有本文件与 [CanHubImageCropSession] 可 import `com.canhub`。
 */
class CanHubImageCropHostRenderer @Inject constructor() : ImageCropHostRenderer {
    @Composable
    override fun Render(
        sourcePath: String,
        spec: ImageCropSpec,
        onSessionReady: (ImageCropSession) -> Unit,
        onLoadFailed: () -> Unit,
        modifier: Modifier,
    ) {
        val ready = rememberUpdatedState(onSessionReady)
        val failed = rememberUpdatedState(onLoadFailed)
        key(sourcePath, spec.aspectWidth, spec.aspectHeight) {
            AndroidView(
                modifier = modifier,
                factory = { context ->
                    CropImageView(context).apply {
                        setBackgroundColor(Color.BLACK)
                        guidelines = CropImageView.Guidelines.ON
                        setFixedAspectRatio(true)
                        setAspectRatio(spec.aspectWidth, spec.aspectHeight)
                        isAutoZoomEnabled = true
                        setMultiTouchEnabled(true)
                        setCenterMoveEnabled(true)
                        setOnSetImageUriCompleteListener { view, _, error ->
                            if (error != null) {
                                failed.value()
                            } else {
                                ready.value(CanHubImageCropSession(view))
                            }
                        }
                        setImageUriAsync(sourcePath.toImageUri())
                    }
                },
                update = { view ->
                    view.setAspectRatio(spec.aspectWidth, spec.aspectHeight)
                    view.setFixedAspectRatio(true)
                },
            )
        }
    }
}

private fun String.toImageUri(): Uri {
    val trimmed = trim()
    return if (trimmed.contains("://")) {
        Uri.parse(trimmed)
    } else {
        Uri.fromFile(File(trimmed))
    }
}
