package ai.yoofi.app.data.image.crop.canhub

import ai.yoofi.app.core.image.crop.ImageCropSession
import android.graphics.Bitmap
import com.canhub.cropper.CropImageView

/** CanHub [CropImageView] 的会话适配。第三方类型不得泄漏到此文件之外。 */
internal class CanHubImageCropSession(
    private val view: CropImageView,
) : ImageCropSession {
    override fun rotateBy(degrees: Int) {
        view.rotateImage(degrees)
    }

    override fun cropBitmap(): Bitmap? {
        return view.getCroppedImage(
            reqWidth = 0,
            reqHeight = 0,
            options = CropImageView.RequestSizeOptions.NONE,
        )
    }
}
