package ai.yoofi.app.ui.auth

import ai.yoofi.app.R
import ai.yoofi.app.core.image.ImageProcessConfig
import ai.yoofi.app.ui.image.ImageCropScreen
import ai.yoofi.app.ui.ime.ImeOverlayBox
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.ime.cursorAtEnd
import ai.yoofi.app.ui.ime.dismissIme
import ai.yoofi.app.ui.ime.rememberCursorAtEndField
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiAuthCaretFrom
import ai.yoofi.app.ui.theme.YoofiAuthCaretTo
import ai.yoofi.app.ui.theme.YoofiAuthError
import ai.yoofi.app.ui.theme.YoofiAuthFieldFill
import ai.yoofi.app.ui.theme.YoofiAuthFocusStroke
import ai.yoofi.app.ui.theme.YoofiAuthIdleButton
import ai.yoofi.app.ui.theme.YoofiAuthIdleStroke
import ai.yoofi.app.ui.theme.YoofiCameraTo
import ai.yoofi.app.ui.theme.YoofiChipText
import ai.yoofi.app.ui.theme.YoofiDialogBg
import ai.yoofi.app.ui.theme.YoofiDialogButton
import ai.yoofi.app.ui.theme.YoofiDialogScrim
import ai.yoofi.app.ui.theme.YoofiGenderSelected
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Figma 初始态默认昵称。 */
internal const val DemoDefaultDisplayName = "User5867"

/** Figma `1761:10630` 占用名，对齐错误态。 */
internal const val DemoTakenDisplayName = "UserNameeeeeeeeeeee1234"

private const val DisplayNameMaxLength = 24

private val GenderChipShape = RoundedCornerShape(12.dp)

private val DialogShape = RoundedCornerShape(16.dp)

private val DialogButtonShape = RoundedCornerShape(20.dp)

private val ToastShape = RoundedCornerShape(8.dp)

/**
 * 展示名警示
 */
private enum class DisplayNameIssue {
    Empty,
    Unavailable,
}

private enum class GenderOption(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
    val rotateIcon: Boolean,
) {
    Male(R.string.auth_gender_male, R.drawable.ic_gender_male, true),
    Female(R.string.auth_gender_female, R.drawable.ic_gender_female, false),
    NonBinary(R.string.auth_gender_nonbinary, R.drawable.ic_gender_nonbinary, true),
    Hide(R.string.auth_gender_hide, R.drawable.ic_gender_hide, false),
}

/**
 * 用户资料填写，覆盖 Figma 七态。
 * 键盘走 [ImeOverlayBox]，不顶起 Continue。
 */
@Composable
internal fun ProfileSetupScreen(
    onSkip: () -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
) {
    val avatarState by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf(DemoDefaultDisplayName) }
    var gender by remember { mutableStateOf<GenderOption?>(null) }
    var nameIssue by remember { mutableStateOf<DisplayNameIssue?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }
    var hasFailedOnce by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AvatarPickerEffect(viewModel)

    Box(modifier = modifier.fillMaxSize()) {
        ProfileSetupContent(
            name = name,
            onNameChange = { value ->
                name = value.take(DisplayNameMaxLength)
                nameIssue = if (value.isBlank() && nameIssue != null) {
                    DisplayNameIssue.Empty
                } else {
                    null
                }
            },
            gender = gender,
            onGenderChange = { selected ->
                gender = if (gender == selected) null else selected
            },
            nameIssue = nameIssue,
            saving = saving,
            saveFailed = saveFailed,
            avatarError = avatarState.avatarError,
            showAvatarSheet = avatarState.showAvatarSheet,
            avatarPath = avatarState.avatarPath,
            avatarRevision = avatarState.avatarRevision,
            isPickingAvatar = avatarState.isPicking,
            onCameraClick = { viewModel.onIntent(ProfileSetupIntent.OpenAvatarSheet) },
            onDismissAvatarSheet = {
                viewModel.onIntent(ProfileSetupIntent.DismissAvatarSheet)
            },
            onChooseGallery = { viewModel.onIntent(ProfileSetupIntent.ChooseFromGallery) },
            onTakePhoto = { viewModel.onIntent(ProfileSetupIntent.TakePhoto) },
            onSkip = onSkip,
            onContinue = {
                if (saving) return@ProfileSetupContent
                if (name.isBlank()) {
                    nameIssue = DisplayNameIssue.Empty
                    saveFailed = false
                    return@ProfileSetupContent
                }
                if (name == DemoTakenDisplayName) {
                    nameIssue = DisplayNameIssue.Unavailable
                    saveFailed = false
                    return@ProfileSetupContent
                }
                nameIssue = null
                saveFailed = false
                saving = true
                scope.launch {
                    delay(900)
                    saving = false
                    if (!hasFailedOnce) {
                        hasFailedOnce = true
                        // 服务端更新失败：输入框红框 + 占用文案。
                        nameIssue = DisplayNameIssue.Unavailable
                    } else {
                        onCompleted()
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        val cropPath = avatarState.cropSourcePath
        if (cropPath != null) {
            ImageCropScreen(
                sourcePath = cropPath,
                config = ImageProcessConfig.Avatar,
                onConfirm = { encoded ->
                    viewModel.onIntent(ProfileSetupIntent.CropConfirmed(encoded))
                },
                onCancel = { viewModel.onIntent(ProfileSetupIntent.CropCancelled) },
                onFailed = { viewModel.onIntent(ProfileSetupIntent.CropFailed) },
            )
        }
    }
}

@Composable
private fun ProfileSetupContent(
    name: String,
    onNameChange: (String) -> Unit,
    gender: GenderOption?,
    onGenderChange: (GenderOption) -> Unit,
    nameIssue: DisplayNameIssue?,
    saving: Boolean,
    saveFailed: Boolean,
    avatarError: Boolean,
    showAvatarSheet: Boolean,
    avatarPath: String?,
    avatarRevision: Long,
    isPickingAvatar: Boolean,
    onCameraClick: () -> Unit,
    onDismissAvatarSheet: () -> Unit,
    onChooseGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val skipAlpha = if (saving) 0.4f else 0.5f

    ImeOverlayBox(modifier = modifier) {
        AuthBackground()
        Column(modifier = Modifier.fillMaxSize()) {
            ProfileSetupHeader(
                skipAlpha = skipAlpha,
                skipEnabled = !saving,
                onSkip = onSkip,
            )
            Spacer(modifier = Modifier.height(28.dp))
            ProfileAvatar(
                avatarPath = avatarPath,
                avatarRevision = avatarRevision,
                enabled = !saving && !isPickingAvatar,
                onCameraClick = onCameraClick,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            // 左右各 20，对齐 Figma `1761:10292`；宽屏铺满，避免只 pad start 偏左
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text = stringResource(R.string.auth_display_name),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 40.dp),
                )
                DisplayNameField(
                    value = name,
                    onValueChange = onNameChange,
                    showError = nameIssue != null,
                    focused = focused,
                    enabled = !saving,
                    onFocusChange = { focused = it },
                    onDone = {
                        dismissIme(focusManager, keyboard)
                        onContinue()
                    },
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                )
                Box(modifier = Modifier.height(40.dp)) {
                    val issue = nameIssue
                    if (issue != null) {
                        Text(
                            text = stringResource(
                                when (issue) {
                                    DisplayNameIssue.Empty -> {
                                        R.string.auth_display_name_empty
                                    }
                                    DisplayNameIssue.Unavailable -> {
                                        R.string.auth_display_name_taken
                                    }
                                },
                            ),
                            color = YoofiAuthError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.auth_gender_optional),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                GenderGrid(
                    selected = gender,
                    enabled = !saving,
                    onSelect = onGenderChange,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                )
            }
        }
        if (saving || saveFailed || avatarError) {
            ProfileStatusToast(
                saving = saving,
                avatarError = avatarError && !saving && !saveFailed,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 677.dp),
            )
        }
        ProfileContinueButton(
            saving = saving,
            onClick = onContinue,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = 734.dp),
        )
        if (showAvatarSheet) {
            ChangeAvatarDialog(
                picking = isPickingAvatar,
                onGallery = onChooseGallery,
                onCamera = onTakePhoto,
                onCancel = onDismissAvatarSheet,
            )
        }
    }
}

@Composable
private fun ProfileSetupHeader(
    skipAlpha: Float,
    skipEnabled: Boolean,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(AuthHeaderHeight),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.auth_profile_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.auth_profile_subtitle),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Text(
            text = stringResource(R.string.auth_profile_skip),
            color = Color.White.copy(alpha = skipAlpha),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .clickableDismissingIme(
                    enabled = skipEnabled,
                    onClick = onSkip,
                ),
        )
    }
}

@Composable
private fun ProfileAvatar(
    avatarPath: String?,
    avatarRevision: Long,
    enabled: Boolean,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 头像固定写 profile.jpg，路径不变；必须用 revision 强制重新解码。
    val avatarBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        avatarPath,
        avatarRevision,
    ) {
        if (avatarPath.isNullOrBlank()) {
            value = null
            return@produceState
        }
        val decoded = withContext(Dispatchers.IO) {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(avatarPath, options)
        }
        value = decoded?.asImageBitmap()
    }
    Box(
        modifier = modifier
            .size(96.dp)
            .clickableDismissingIme(
                enabled = enabled,
                onClick = onCameraClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(YoofiAuthFieldFill),
            contentAlignment = Alignment.Center,
        ) {
            val loaded = avatarBitmap
            if (loaded != null) {
                key(avatarPath, avatarRevision) {
                    Image(
                        bitmap = loaded,
                        contentDescription = stringResource(R.string.cd_auth_avatar),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_profile_face),
                    contentDescription = stringResource(R.string.cd_auth_avatar),
                    modifier = Modifier.size(width = 48.dp, height = 57.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(YoofiCameraTo, YoofiAuthFocusStroke),
                    ),
                )
                .clickableDismissingIme(
                    enabled = enabled,
                    onClick = onCameraClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_profile_camera),
                contentDescription = stringResource(R.string.cd_auth_camera),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun DisplayNameField(
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    focused: Boolean,
    enabled: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stroke = when {
        showError -> YoofiAuthError
        focused -> YoofiAuthFocusStroke
        else -> YoofiAuthIdleStroke
    }
    val field = rememberCursorAtEndField(value, onValueChange)
    BasicTextField(
        value = field.value,
        onValueChange = field.onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        ),
        cursorBrush = Brush.verticalGradient(
            colors = listOf(YoofiAuthCaretFrom, YoofiAuthCaretTo),
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .cursorAtEnd(field)
            .onFocusChanged { onFocusChange(it.isFocused) },
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(YoofiAuthFieldFill, AuthFieldShape)
                    .border(1.dp, stroke, AuthFieldShape),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp, end = 48.dp),
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.auth_display_name_placeholder),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp,
                        )
                    }
                    inner()
                }
                if (value.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.auth_display_name_counter,
                            value.length,
                        ),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun GenderGrid(
    selected: GenderOption?,
    enabled: Boolean,
    onSelect: (GenderOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GenderChip(
                option = GenderOption.Male,
                selected = selected == GenderOption.Male,
                enabled = enabled,
                onClick = { onSelect(GenderOption.Male) },
                modifier = Modifier.weight(1f),
            )
            GenderChip(
                option = GenderOption.Female,
                selected = selected == GenderOption.Female,
                enabled = enabled,
                onClick = { onSelect(GenderOption.Female) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GenderChip(
                option = GenderOption.NonBinary,
                selected = selected == GenderOption.NonBinary,
                enabled = enabled,
                onClick = { onSelect(GenderOption.NonBinary) },
                modifier = Modifier.weight(1f),
            )
            GenderChip(
                option = GenderOption.Hide,
                selected = selected == GenderOption.Hide,
                enabled = enabled,
                onClick = { onSelect(GenderOption.Hide) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun GenderChip(
    option: GenderOption,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stroke = if (selected) YoofiAuthFocusStroke else YoofiAuthIdleStroke
    val labelColor = if (selected) YoofiGenderSelected else YoofiChipText
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(GenderChipShape)
            .background(YoofiAuthFieldFill)
            .border(1.dp, stroke, GenderChipShape)
            .clickableDismissingIme(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(option.iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .then(if (option.rotateIcon) Modifier.rotate(180f) else Modifier),
        )
        Text(
            text = stringResource(option.labelRes),
            color = labelColor,
            fontSize = 14.sp,
            maxLines = 1,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun ProfileContinueButton(
    saving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .graphicsLayer { alpha = if (saving) 0.5f else 1f }
            .clip(AuthPillShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(YoofiStartGameFrom, YoofiStartGameTo),
                ),
            )
            .clickableDismissingIme(enabled = !saving, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.auth_continue_yoofi),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ProfileStatusToast(
    saving: Boolean,
    avatarError: Boolean,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "profile-loader")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "profile-loader-rot",
    )
    Row(
        modifier = modifier
            .background(YoofiAuthIdleButton, ToastShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (saving) {
            Image(
                painter = painterResource(R.drawable.ic_profile_loader),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation),
            )
            Text(
                text = stringResource(R.string.auth_profile_saving),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_profile_error),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(
                    if (avatarError) {
                        R.string.auth_avatar_pick_failed
                    } else {
                        R.string.auth_profile_save_failed
                    },
                ),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(177.dp),
            )
        }
    }
}

/**
 * 显示更改头像对话框
 */
@Composable
private fun ChangeAvatarDialog(
    picking: Boolean,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(YoofiDialogScrim)
                .clickableDismissingIme(onClick = onCancel),
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 542.dp)
                .fillMaxWidth()
                .height(238.dp)
                .clip(DialogShape)
                .background(YoofiDialogBg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.auth_change_avatar),
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth(),
            )
            Column(
                modifier = Modifier.padding(top = 22.dp, start = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AvatarDialogButton(
                    label = stringResource(R.string.auth_choose_gallery),
                    textColor = Color.White,
                    enabled = !picking,
                    onClick = onGallery,
                )
                AvatarDialogButton(
                    label = stringResource(R.string.auth_take_photo),
                    textColor = Color.White,
                    enabled = !picking,
                    onClick = onCamera,
                )
                AvatarDialogButton(
                    label = stringResource(R.string.auth_cancel),
                    textColor = YoofiAuthError,
                    enabled = !picking,
                    onClick = onCancel,
                )
            }
        }
    }
}

@Composable
private fun AvatarDialogButton(
    label: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(DialogButtonShape)
            .background(YoofiDialogButton)
            .clickableDismissingIme(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ProfileSetupScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        ProfileSetupContent(
            name = DemoDefaultDisplayName,
            onNameChange = {},
            gender = null,
            onGenderChange = {},
            nameIssue = null,
            saving = false,
            saveFailed = false,
            avatarError = false,
            showAvatarSheet = false,
            avatarPath = null,
            avatarRevision = 0L,
            isPickingAvatar = false,
            onCameraClick = {},
            onDismissAvatarSheet = {},
            onChooseGallery = {},
            onTakePhoto = {},
            onSkip = {},
            onContinue = {},
        )
    }
}
