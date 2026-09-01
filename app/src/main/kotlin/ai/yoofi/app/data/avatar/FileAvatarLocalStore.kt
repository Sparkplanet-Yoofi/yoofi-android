package ai.yoofi.app.data.avatar

import ai.yoofi.shared.common.AppError
import ai.yoofi.shared.common.Outcome
import ai.yoofi.app.domain.avatar.AvatarLocalStore
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class FileAvatarLocalStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AvatarLocalStore {

    override fun createCaptureUri(): String {
        val dir = captureDir().apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, CaptureFileName)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return FileProvider.getUriForFile(context, authority(), file).toString()
    }

    override suspend fun persistFromUri(sourceUri: String): Outcome<String> = withContext(
        Dispatchers.IO,
    ) {
        val uri = runCatching { Uri.parse(sourceUri) }.getOrNull()
            ?: return@withContext Outcome.Err(AppError.Unknown)
        if (uri.scheme.isNullOrBlank()) {
            return@withContext Outcome.Err(AppError.Unknown)
        }
        try {
            val mime = context.contentResolver.getType(uri)
            if (mime != null && !mime.startsWith("image/")) {
                return@withContext Outcome.Err(AppError.Unknown)
            }
            val temp = File(captureDir().apply { mkdirs() }, "import.jpg")
            val copied = copyLimited(uri, temp)
            if (!copied) {
                temp.delete()
                return@withContext Outcome.Err(AppError.Unknown)
            }
            val bitmap = decodeDownsampled(temp)
            temp.delete()
            if (bitmap == null) {
                return@withContext Outcome.Err(AppError.Unknown)
            }
            val destDir = File(context.filesDir, AvatarDir).apply { mkdirs() }
            val dest = File(destDir, ProfileFileName)
            FileOutputStream(dest).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JpegQuality, out)
            }
            bitmap.recycle()
            Outcome.Ok(dest.absolutePath)
        } catch (_: Exception) {
            Outcome.Err(AppError.Unknown)
        }
    }

    override suspend fun stageFromUri(sourceUri: String): Outcome<String> = withContext(
        Dispatchers.IO,
    ) {
        val uri = runCatching { Uri.parse(sourceUri) }.getOrNull()
            ?: return@withContext Outcome.Err(AppError.Unknown)
        if (uri.scheme.isNullOrBlank()) {
            return@withContext Outcome.Err(AppError.Unknown)
        }
        try {
            val mime = context.contentResolver.getType(uri)
            if (mime != null && !mime.startsWith("image/")) {
                return@withContext Outcome.Err(AppError.Unknown)
            }
            val dest = File(captureDir().apply { mkdirs() }, CropSourceFileName)
            if (dest.exists()) {
                dest.delete()
            }
            val copied = copyLimited(uri, dest)
            if (!copied) {
                dest.delete()
                return@withContext Outcome.Err(AppError.Unknown)
            }
            Outcome.Ok(dest.absolutePath)
        } catch (_: Exception) {
            Outcome.Err(AppError.Unknown)
        }
    }

    override suspend fun persistEncodedFile(sourcePath: String): Outcome<String> = withContext(
        Dispatchers.IO,
    ) {
        val source = File(sourcePath)
        if (!source.exists() || source.length() <= 0L) {
            return@withContext Outcome.Err(AppError.Unknown)
        }
        try {
            val destDir = File(context.filesDir, AvatarDir).apply { mkdirs() }
            val dest = File(destDir, ProfileFileName)
            if (source.canonicalPath == dest.canonicalPath) {
                if (!isDecodableJpeg(dest)) {
                    return@withContext Outcome.Err(AppError.Unknown)
                }
                return@withContext Outcome.Ok(dest.absolutePath)
            }
            val tmp = File(destDir, ProfileTmpFileName)
            if (tmp.exists() && !tmp.delete()) {
                return@withContext Outcome.Err(AppError.Unknown)
            }
            source.inputStream().use { input ->
                FileOutputStream(tmp).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }
            }
            if (!isDecodableJpeg(tmp)) {
                tmp.delete()
                return@withContext Outcome.Err(AppError.Unknown)
            }
            if (dest.exists() && !dest.delete()) {
                tmp.delete()
                return@withContext Outcome.Err(AppError.Unknown)
            }
            if (!tmp.renameTo(dest)) {
                tmp.copyTo(dest, overwrite = true)
                tmp.delete()
            }
            if (!isDecodableJpeg(dest)) {
                return@withContext Outcome.Err(AppError.Unknown)
            }
            Outcome.Ok(dest.absolutePath)
        } catch (_: Exception) {
            Outcome.Err(AppError.Unknown)
        }
    }

    override fun discardCapture(uri: String) {
        val parsed = runCatching { Uri.parse(uri) }.getOrNull() ?: return
        val path = parsed.path ?: return
        val file = File(captureDir(), CaptureFileName)
        if (path.endsWith(CaptureFileName) && file.exists()) {
            file.delete()
        }
    }

    override fun discardStagedCrop() {
        val dir = captureDir()
        File(dir, CropSourceFileName).delete()
        File(dir, CropResultFileName).delete()
    }

    private fun copyLimited(uri: Uri, dest: File): Boolean {
        val input = context.contentResolver.openInputStream(uri) ?: return false
        input.use { stream ->
            FileOutputStream(dest).use { out ->
                val buffer = ByteArray(16 * 1024)
                var total = 0L
                while (true) {
                    val read = stream.read(buffer)
                    if (read <= 0) break
                    total += read
                    if (total > MaxSourceBytes) {
                        dest.delete()
                        return false
                    }
                    out.write(buffer, 0, read)
                }
            }
        }
        return dest.length() > 0L
    }

    private fun isDecodableJpeg(file: File): Boolean {
        if (!file.exists() || file.length() <= 0L) return false
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        return bounds.outWidth > 0 && bounds.outHeight > 0
    }

    private fun decodeDownsampled(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val sample = sampleSize(bounds.outWidth, bounds.outHeight, MaxEdgePx)
        val decoded = BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample },
        ) ?: return null
        return applyExif(file, decoded)
    }

    private fun applyExif(file: File, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true,
        )
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    private fun sampleSize(width: Int, height: Int, maxEdge: Int): Int {
        var size = 1
        while (width / size > maxEdge || height / size > maxEdge) {
            size *= 2
        }
        return size
    }

    private fun captureDir(): File = File(context.cacheDir, AvatarDir)

    private fun authority(): String = "${context.packageName}.fileprovider"

    private companion object {
        const val AvatarDir = "avatars"
        const val CaptureFileName = "capture.jpg"
        const val CropSourceFileName = "crop_source.jpg"
        const val CropResultFileName = "crop_result.jpg"
        const val ProfileFileName = "profile.jpg"
        const val ProfileTmpFileName = "profile.tmp.jpg"
        const val MaxSourceBytes = 15L * 1024 * 1024
        const val MaxEdgePx = 512
        const val JpegQuality = 90
    }
}
