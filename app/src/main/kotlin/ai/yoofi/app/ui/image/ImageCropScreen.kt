package ai.yoofi.app.ui.image

import ai.yoofi.app.R
import ai.yoofi.app.core.image.ImageCropExporter
import ai.yoofi.app.core.image.ImageProcessConfig
import ai.yoofi.app.core.image.crop.ImageCropHost
import ai.yoofi.app.core.image.crop.ImageCropHostRenderer
import ai.yoofi.app.core.image.crop.ImageCropSession
import ai.yoofi.app.core.image.crop.ImageCropSpec
import ai.yoofi.app.di.imageCropHostRenderer
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 通用裁剪页。业务只依赖 [ImageCropHostRenderer] 与 [ImageProcessConfig]，
 * 不 import 第三方裁剪库。
 */
@Composable
fun ImageCropScreen(
    sourcePath: String,
    onConfirm: (encodedPath: String) -> Unit,
    onCancel: () -> Unit,
    onFailed: () -> Unit,
    modifier: Modifier = Modifier,
    config: ImageProcessConfig = ImageProcessConfig.Avatar,
    host: ImageCropHostRenderer? = null,
) {
    val context = LocalContext.current
    val renderer = host ?: remember(context) { imageCropHostRenderer(context) }
    val spec = remember(config) { ImageCropSpec.from(config) }
    var session by remember(sourcePath) { mutableStateOf<ImageCropSession?>(null) }
    var confirming by remember { mutableStateOf(false) }
    var confirmFailed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = !confirming) { onCancel() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        CropHeader(
            enabled = !confirming,
            rotateEnabled = spec.allowRotate && session != null && !confirming,
            onBack = onCancel,
            onRotate = { session?.rotateBy(90) },
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            ImageCropHost(
                sourcePath = sourcePath,
                spec = spec,
                renderer = renderer,
                onSessionReady = { session = it },
                onLoadFailed = onFailed,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (confirmFailed) {
            Text(
                text = stringResource(R.string.auth_avatar_pick_failed),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
            )
        }
        CropConfirmButton(
            enabled = session != null && !confirming,
            confirming = confirming,
            onClick = {
                val cropSession = session ?: return@CropConfirmButton
                confirming = true
                confirmFailed = false
                scope.launch {
                    val bitmap = cropSession.cropBitmap()
                    if (bitmap == null) {
                        confirming = false
                        confirmFailed = true
                        return@launch
                    }
                    val encoded = withContext(Dispatchers.IO) {
                        writeCroppedJpeg(sourcePath, bitmap, config)
                    }
                    if (encoded == null) {
                        confirming = false
                        confirmFailed = true
                    } else {
                        onConfirm(encoded)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        )
    }
}

@Composable
private fun CropHeader(
    enabled: Boolean,
    rotateEnabled: Boolean,
    onBack: () -> Unit,
    onRotate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(CropHeaderHeight),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_auth_back),
            contentDescription = stringResource(R.string.cd_auth_back),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .size(24.dp)
                .clickableDismissingIme(enabled = enabled, onClick = onBack),
        )
        Text(
            text = stringResource(R.string.auth_crop_title),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center),
        )
        Image(
            painter = painterResource(R.drawable.ic_crop_rotate),
            contentDescription = stringResource(R.string.cd_auth_crop_rotate),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .size(24.dp)
                .graphicsLayer { alpha = if (rotateEnabled) 1f else 0.4f }
                .clickableDismissingIme(enabled = rotateEnabled, onClick = onRotate),
        )
    }
}

@Composable
private fun CropConfirmButton(
    enabled: Boolean,
    confirming: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
            .clip(CropPillShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(YoofiStartGameFrom, YoofiStartGameTo),
                ),
            )
            .clickableDismissingIme(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(
                if (confirming) {
                    R.string.auth_crop_processing
                } else {
                    R.string.auth_crop_confirm
                },
            ),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun writeCroppedJpeg(
    sourcePath: String,
    bitmap: Bitmap,
    config: ImageProcessConfig,
): String? {
    return try {
        val parent = File(sourcePath).parentFile ?: return null
        val dest = File(parent, CropResultFileName)
        if (!ImageCropExporter.writeJpeg(bitmap, dest, config)) {
            null
        } else {
            dest.absolutePath
        }
    } finally {
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}

private const val CropResultFileName = "crop_result.jpg"

private val CropHeaderHeight = 60.dp

private val CropPillShape = RoundedCornerShape(1000.dp)

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ImageCropScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        ImageCropScreen(
            sourcePath = "",
            onConfirm = {},
            onCancel = {},
            onFailed = {},
            host = PreviewCropHostRenderer,
        )
    }
}

/** Preview 不走 Hilt / CanHub，只占位中间裁剪区。 */
private object PreviewCropHostRenderer : ImageCropHostRenderer {
    @Composable
    override fun Render(
        sourcePath: String,
        spec: ImageCropSpec,
        onSessionReady: (ImageCropSession) -> Unit,
        onLoadFailed: () -> Unit,
        modifier: Modifier,
    ) {
        LaunchedEffect(Unit) {
            onSessionReady(PreviewCropSession)
        }
        Box(
            modifier = modifier.background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(350.dp)
                    .background(Color(0xFF2C2C2C)),
            )
        }
    }
}

private object PreviewCropSession : ImageCropSession {
    override fun rotateBy(degrees: Int) = Unit

    override fun cropBitmap(): Bitmap? = null
}
