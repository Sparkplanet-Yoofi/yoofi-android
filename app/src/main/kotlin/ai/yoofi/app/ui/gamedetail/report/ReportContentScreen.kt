package ai.yoofi.app.ui.gamedetail.report

import ai.yoofi.app.R
import ai.yoofi.app.domain.report.ReportReason
import ai.yoofi.app.ui.auth.AuthBackground
import ai.yoofi.app.ui.auth.AuthSignUpHeader
import ai.yoofi.app.ui.gamedetail.DetailActionBrush
import ai.yoofi.app.ui.gamedetail.detailCoverRes
import ai.yoofi.app.ui.ime.ImeOverlayBox
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.ime.cursorAtEnd
import ai.yoofi.app.ui.ime.rememberCursorAtEndField
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiAuthFieldFill
import ai.yoofi.app.ui.theme.YoofiAuthFocusStroke
import ai.yoofi.app.ui.theme.YoofiAuthIdleStroke
import ai.yoofi.app.ui.theme.YoofiCameraTo
import ai.yoofi.app.ui.theme.YoofiChipText
import ai.yoofi.app.ui.theme.YoofiDialogButton
import ai.yoofi.app.ui.theme.YoofiGenderSelected
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val PagePad = 20.dp
private val CardShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(100.dp)
private val WorkCardFill = Color(0x4D746C86)
private val SuccessRingBrush = Brush.horizontalGradient(
    listOf(YoofiAuthFocusStroke, YoofiCameraTo),
)

/**
 * 内容举报流，对齐 Figma `2252:18328` / `2252:18374` / `2252:18531`。
 */
@Composable
internal fun ReportContentScreen(
    target: ReportTarget,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportContentViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(target.gameId) { viewModel.bind(target, onClose) }
    val bound = state.target ?: target
    ReportContentLayout(
        target = bound,
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun ReportContentLayout(
    target: ReportTarget,
    state: ReportContentUiState,
    onIntent: (ReportContentIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onIntent(ReportContentIntent.Back) }
    ImeOverlayBox(modifier = modifier) {
        AuthBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            AuthSignUpHeader(
                onBack = { onIntent(ReportContentIntent.Back) },
                title = stringResource(
                    if (state.step == ReportStep.Done) {
                        R.string.report_feedback_title
                    } else {
                        R.string.report_content
                    },
                ),
            )
            when (state.step) {
                ReportStep.Reason -> ReasonStep(
                    target = target,
                    selected = state.reason,
                    nextEnabled = state.canGoNext,
                    onSelect = { onIntent(ReportContentIntent.SelectReason(it)) },
                    onNext = { onIntent(ReportContentIntent.Next) },
                    modifier = Modifier.weight(1f),
                )
                ReportStep.Details -> DetailsStep(
                    details = state.details,
                    screenshotUris = state.screenshotUris,
                    canAddScreenshot = state.canAddScreenshot,
                    submitEnabled = state.canSubmit,
                    onDetailsChange = { onIntent(ReportContentIntent.DetailsChanged(it)) },
                    onAddScreenshot = { onIntent(ReportContentIntent.AddScreenshot(it)) },
                    onRemoveScreenshot = { onIntent(ReportContentIntent.RemoveScreenshot(it)) },
                    onSubmit = { onIntent(ReportContentIntent.Submit) },
                    onCancel = { onIntent(ReportContentIntent.Cancel) },
                    modifier = Modifier.weight(1f),
                )
                ReportStep.Done -> DoneStep(
                    onBack = { onIntent(ReportContentIntent.Back) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ReasonStep(
    target: ReportTarget,
    selected: ReportReason?,
    nextEnabled: Boolean,
    onSelect: (ReportReason) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PagePad),
        ) {
            Spacer(Modifier.height(16.dp))
            ReportedWorkCard(target = target)
            Spacer(Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.report_reason_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(18.dp))
            ReportReason.entries.forEach { reason ->
                ReasonChip(
                    label = stringResource(reason.labelRes),
                    selected = reason == selected,
                    onClick = { onSelect(reason) },
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        ReportPrimaryButton(
            label = stringResource(R.string.report_next),
            enabled = nextEnabled,
            onClick = onNext,
            modifier = Modifier
                .padding(horizontal = PagePad)
                .padding(bottom = 20.dp),
        )
    }
}

@Composable
private fun ReportedWorkCard(target: ReportTarget) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(CardShape)
            .background(WorkCardFill)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(detailCoverRes(target.coverKey)),
            contentDescription = null,
            modifier = Modifier
                .size(width = 64.dp, height = 84.dp)
                .clip(CardShape),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = target.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = target.authorName,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun ReasonChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val stroke = if (selected) YoofiAuthFocusStroke else YoofiAuthIdleStroke
    val textColor = if (selected) YoofiGenderSelected else YoofiChipText
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CardShape)
            .background(YoofiAuthFieldFill)
            .border(1.dp, stroke, CardShape)
            .clickableDismissingIme(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun DetailsStep(
    details: String,
    screenshotUris: List<String>,
    canAddScreenshot: Boolean,
    submitEnabled: Boolean,
    onDetailsChange: (String) -> Unit,
    onAddScreenshot: (String) -> Unit,
    onRemoveScreenshot: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { onAddScreenshot(it.toString()) }
    }
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PagePad),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.report_details_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(18.dp))
            DetailsField(details = details, onDetailsChange = onDetailsChange)
            Spacer(Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.report_screenshot_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.report_screenshot_hint),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                screenshotUris.forEach { uri ->
                    ScreenshotThumb(
                        uri = uri,
                        onRemove = { onRemoveScreenshot(uri) },
                    )
                }
                if (canAddScreenshot) {
                    AddScreenshotTile(
                        onClick = {
                            picker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = PagePad)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ReportPrimaryButton(
                label = stringResource(R.string.report_submit),
                enabled = submitEnabled,
                onClick = onSubmit,
            )
            ReportSecondaryButton(
                label = stringResource(R.string.auth_cancel),
                onClick = onCancel,
            )
        }
    }
}

@Composable
private fun DetailsField(
    details: String,
    onDetailsChange: (String) -> Unit,
) {
    val field = rememberCursorAtEndField(details, onDetailsChange)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(CardShape)
            .background(YoofiAuthFieldFill)
            .border(1.dp, YoofiAuthIdleStroke, CardShape)
            .padding(12.dp),
    ) {
        BasicTextField(
            value = field.value,
            onValueChange = field.onValueChange,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
            ),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
                .cursorAtEnd(field),
            decorationBox = { inner ->
                if (details.isEmpty()) {
                    Text(
                        text = stringResource(R.string.report_details_hint),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                    )
                }
                inner()
            },
        )
        Text(
            text = stringResource(R.string.report_details_count, details.length),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun AddScreenshotTile(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CardShape)
            .background(YoofiAuthFieldFill)
            .border(1.dp, YoofiAuthIdleStroke, CardShape)
            .clickableDismissingIme(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_circle_plus),
            contentDescription = stringResource(R.string.cd_report_add_screenshot),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun ScreenshotThumb(
    uri: String,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }
    }
    // Figma：96 缩略图，20 关闭钮叠在右上，各探出 8
    Box(modifier = Modifier.size(104.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(96.dp)
                .clip(CardShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CardShape),
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.ic_thumb_close),
            contentDescription = stringResource(R.string.cd_report_remove_screenshot),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .clip(CircleShape)
                .clickableDismissingIme(onClick = onRemove),
        )
    }
}

@Composable
private fun DoneStep(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = PagePad)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SuccessRingBrush),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_report_check),
                    contentDescription = null,
                    modifier = Modifier.size(width = 20.dp, height = 15.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.report_submitted),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.report_submitted_body),
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )
        }
        ReportSecondaryButton(
            label = stringResource(R.string.report_back),
            onClick = onBack,
            modifier = Modifier
                .padding(horizontal = PagePad)
                .padding(bottom = 20.dp),
        )
    }
}

@Composable
private fun ReportPrimaryButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(PillShape)
            .background(DetailActionBrush)
            .clickableDismissingIme(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ReportSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(PillShape)
            .background(YoofiDialogButton)
            .clickableDismissingIme(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@get:StringRes
private val ReportReason.labelRes: Int
    get() = when (this) {
        ReportReason.Sexual -> R.string.report_reason_sexual
        ReportReason.Violent -> R.string.report_reason_violent
        ReportReason.Political -> R.string.report_reason_political
        ReportReason.Copyright -> R.string.report_reason_copyright
        ReportReason.Harassment -> R.string.report_reason_harassment
        ReportReason.Scam -> R.string.report_reason_scam
        ReportReason.Other -> R.string.report_reason_other
    }

private val PreviewTarget = ReportTarget(
    gameId = "forbidden-world",
    title = "Arranged Marriage Simulator",
    authorName = "Author Name",
    coverKey = "cover-forbidden-world",
)

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ReportReasonPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        ReportContentLayout(
            target = PreviewTarget,
            state = ReportContentUiState(
                target = PreviewTarget,
                reason = ReportReason.Violent,
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ReportDetailsPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        ReportContentLayout(
            target = PreviewTarget,
            state = ReportContentUiState(
                target = PreviewTarget,
                step = ReportStep.Details,
                reason = ReportReason.Violent,
                details = "Reason",
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ReportDonePreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        ReportContentLayout(
            target = PreviewTarget,
            state = ReportContentUiState(
                target = PreviewTarget,
                step = ReportStep.Done,
            ),
            onIntent = {},
        )
    }
}
