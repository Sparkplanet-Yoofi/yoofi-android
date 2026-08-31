package ai.yoofi.app.ui.settings.feedback

import ai.yoofi.app.R
import ai.yoofi.app.domain.feedback.FeedbackType
import ai.yoofi.app.ui.auth.AuthBackground
import ai.yoofi.app.ui.auth.AuthSignUpHeader
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
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val PagePad = 20.dp
private val CardShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(100.dp)
private val SubmitBrush = Brush.verticalGradient(
    0f to YoofiStartGameFrom,
    1.5741f to YoofiStartGameTo,
)
private val SuccessRingBrush = Brush.horizontalGradient(
    listOf(YoofiAuthFocusStroke, YoofiCameraTo),
)

/**
 * 设置反馈，对齐 Figma `2252:17719` / `2252:17770` / `2252:17821`。
 * 键盘走 [ImeOverlayBox]，不顶起 Submit。
 */
@Composable
internal fun FeedbackScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedbackViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.bind(onClose) }
    FeedbackLayout(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun FeedbackLayout(
    state: FeedbackUiState,
    onIntent: (FeedbackIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onIntent(FeedbackIntent.Back) }
    ImeOverlayBox(modifier = modifier) {
        AuthBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            AuthSignUpHeader(
                onBack = { onIntent(FeedbackIntent.Back) },
                title = stringResource(R.string.settings_feedback),
            )
            when (state.step) {
                FeedbackStep.Form -> FeedbackForm(
                    state = state,
                    onIntent = onIntent,
                    modifier = Modifier.weight(1f),
                )
                FeedbackStep.Done -> FeedbackDone(
                    onBack = { onIntent(FeedbackIntent.Back) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FeedbackForm(
    state: FeedbackUiState,
    onIntent: (FeedbackIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PagePad)
                .padding(top = 16.dp, bottom = 24.dp),
        ) {
            SectionLabel(stringResource(R.string.settings_feedback_type))
            Spacer(Modifier.height(12.dp))
            TypeGrid(
                selected = state.type,
                onSelect = { onIntent(FeedbackIntent.SelectType(it)) },
            )
            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.settings_feedback_description))
            Spacer(Modifier.height(12.dp))
            DescriptionField(
                details = state.details,
                onDetailsChange = { onIntent(FeedbackIntent.DetailsChanged(it)) },
            )
            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.settings_feedback_contact))
            Spacer(Modifier.height(12.dp))
            ContactField(
                contact = state.contact,
                onContactChange = { onIntent(FeedbackIntent.ContactChanged(it)) },
            )
        }
        FeedbackPrimaryButton(
            label = stringResource(R.string.settings_feedback_submit),
            enabled = state.canSubmit,
            onClick = { onIntent(FeedbackIntent.Submit) },
            modifier = Modifier
                .padding(horizontal = PagePad)
                .padding(bottom = 20.dp),
        )
    }
}

@Composable
private fun FeedbackDone(
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
                text = stringResource(R.string.settings_feedback_submitted),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.settings_feedback_submitted_body),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )
        }
        Box(
            modifier = Modifier
                .padding(horizontal = PagePad)
                .padding(bottom = 20.dp)
                .fillMaxWidth()
                .height(46.dp)
                .clip(PillShape)
                .background(YoofiDialogButton)
                .clickableDismissingIme(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.report_back),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun TypeGrid(
    selected: FeedbackType?,
    onSelect: (FeedbackType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeChip(
                type = FeedbackType.Suggestion,
                selected = selected == FeedbackType.Suggestion,
                onSelect = onSelect,
                modifier = Modifier.weight(1f),
            )
            TypeChip(
                type = FeedbackType.Bug,
                selected = selected == FeedbackType.Bug,
                onSelect = onSelect,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeChip(
                type = FeedbackType.Content,
                selected = selected == FeedbackType.Content,
                onSelect = onSelect,
                modifier = Modifier.weight(1f),
            )
            TypeChip(
                type = FeedbackType.Other,
                selected = selected == FeedbackType.Other,
                onSelect = onSelect,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TypeChip(
    type: FeedbackType,
    selected: Boolean,
    onSelect: (FeedbackType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stroke = if (selected) YoofiAuthFocusStroke else YoofiAuthIdleStroke
    val textColor = if (selected) YoofiGenderSelected else YoofiChipText
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(CardShape)
            .background(YoofiAuthFieldFill)
            .border(1.dp, stroke, CardShape)
            .clickableDismissingIme(role = Role.RadioButton, onClick = { onSelect(type) }),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(type.labelRes),
            color = textColor,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun DescriptionField(
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
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
                .cursorAtEnd(field),
            decorationBox = { inner ->
                if (details.isEmpty()) {
                    Text(
                        text = stringResource(R.string.settings_feedback_description_hint),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                    )
                }
                inner()
            },
        )
        Text(
            text = stringResource(R.string.settings_feedback_count, details.length),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun ContactField(
    contact: String,
    onContactChange: (String) -> Unit,
) {
    val field = rememberCursorAtEndField(contact, onContactChange)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(CardShape)
            .background(YoofiAuthFieldFill)
            .border(1.dp, YoofiAuthIdleStroke, CardShape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = field.value,
            onValueChange = field.onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            cursorBrush = SolidColor(Color.White),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .cursorAtEnd(field),
            decorationBox = { inner ->
                if (contact.isEmpty()) {
                    Text(
                        text = stringResource(R.string.settings_feedback_contact_hint),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                    )
                }
                inner()
            },
        )
    }
}

@Composable
private fun FeedbackPrimaryButton(
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
            .background(SubmitBrush)
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

@get:StringRes
private val FeedbackType.labelRes: Int
    get() = when (this) {
        FeedbackType.Suggestion -> R.string.settings_feedback_type_suggestion
        FeedbackType.Bug -> R.string.settings_feedback_type_bug
        FeedbackType.Content -> R.string.settings_feedback_type_content
        FeedbackType.Other -> R.string.settings_feedback_type_other
    }

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FeedbackEmptyPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        FeedbackLayout(state = FeedbackUiState(), onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FeedbackFilledPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        FeedbackLayout(
            state = FeedbackUiState(
                type = FeedbackType.Bug,
                details = "12345679",
                contact = "123456@gmail.com",
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FeedbackDonePreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        FeedbackLayout(
            state = FeedbackUiState(step = FeedbackStep.Done),
            onIntent = {},
        )
    }
}
