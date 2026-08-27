package ai.yoofi.app.ui.chat

import ai.yoofi.app.R
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAuthCaretFrom
import ai.yoofi.app.ui.theme.YoofiAuthCaretTo
import ai.yoofi.app.ui.theme.YoofiAuthFieldFill
import ai.yoofi.app.ui.theme.YoofiAuthIdleStroke
import ai.yoofi.app.ui.theme.YoofiAuthOtpEmpty
import ai.yoofi.app.ui.theme.YoofiChatChipCast
import ai.yoofi.app.ui.theme.YoofiChatChipItems
import ai.yoofi.app.ui.theme.YoofiChatChipMap
import ai.yoofi.app.ui.theme.YoofiChatChipRecap
import ai.yoofi.app.ui.theme.YoofiGameBg0
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FooterBrush = Brush.verticalGradient(
    0f to Color.Transparent,
    0.29474f to YoofiGameBg0,
    1f to YoofiGameBg0,
)

private val InputShape = RoundedCornerShape(100.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val ContinueShape = RoundedCornerShape(100.dp)

@Composable
internal fun ChatRoomFooter(
    state: ChatRoomUiState,
    onIntent: (ChatRoomIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val mentionOpen = state.overlay == ChatRoomOverlay.Mention
    val inspireOpen = state.overlay == ChatRoomOverlay.Inspiration

    LaunchedEffect(mentionOpen) {
        if (mentionOpen) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(FooterBrush),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 54.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ChipRow(onIntent = onIntent, modifier = Modifier.fillMaxWidth())
                if (mentionOpen) {
                    ChatMentionSheet(
                        members = state.mentionPageMembers,
                        pageIndex = state.mentionPage,
                        pageCount = state.mentionPageCount,
                        onIntent = onIntent,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
            InputRow(
                draft = state.draft,
                showPlaceholder = state.draft.isEmpty() && !mentionOpen,
                onDraftChange = { onIntent(ChatRoomIntent.DraftChanged(it)) },
                onAtClick = { onIntent(ChatRoomIntent.OpenMention) },
                onWandClick = { onIntent(ChatRoomIntent.OpenInspiration) },
                onContinue = { onIntent(ChatRoomIntent.ContinueStory) },
                focusRequester = focusRequester,
            )
            if (inspireOpen) {
                InspirationList(
                    lines = state.inspirations,
                    onPick = { onIntent(ChatRoomIntent.PickInspiration(it)) },
                )
            }
        }
    }
}

@Composable
private fun ChipRow(
    onIntent: (ChatRoomIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FooterChip(
            labelRes = R.string.chat_cast,
            iconRes = R.drawable.img_chat_icon_cast,
            iconWidth = 20.dp,
            iconHeight = 22.dp,
            tint = YoofiChatChipCast,
            showDot = true,
            onClick = { onIntent(ChatRoomIntent.OpenCast) },
        )
        FooterChip(
            labelRes = R.string.chat_map,
            iconRes = R.drawable.img_chat_icon_map,
            iconWidth = 23.dp,
            iconHeight = 21.dp,
            tint = YoofiChatChipMap,
            showDot = true,
            onClick = {},
        )
        FooterChip(
            labelRes = R.string.chat_items,
            iconRes = R.drawable.img_chat_icon_items,
            iconWidth = 20.dp,
            iconHeight = 20.dp,
            tint = YoofiChatChipItems,
            showDot = true,
            onClick = {},
        )
        FooterChip(
            labelRes = R.string.chat_recap,
            iconRes = R.drawable.img_chat_icon_recap,
            iconWidth = 22.dp,
            iconHeight = 22.dp,
            tint = YoofiChatChipRecap,
            showDot = false,
            onClick = {},
        )
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.img_chat_icon_phone),
            contentDescription = null,
            modifier = Modifier
                .size(width = 36.dp, height = 41.dp)
                .rotate(9.37f)
                .graphicsLayer { blendMode = BlendMode.Lighten },
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun FooterChip(
    @StringRes labelRes: Int,
    @DrawableRes iconRes: Int,
    iconWidth: Dp,
    iconHeight: Dp,
    tint: Color,
    showDot: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(36.dp)
            .background(tint, ChipShape)
            .clickableDismissingIme(role = Role.Button, onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(width = iconWidth, height = iconHeight)
                    .graphicsLayer { blendMode = BlendMode.Lighten },
                contentScale = ContentScale.Fit,
            )
            if (showDot) {
                Image(
                    painter = painterResource(R.drawable.ic_chat_dot),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(6.dp),
                )
            }
        }
        Text(
            text = stringResource(labelRes),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun InputRow(
    draft: String,
    showPlaceholder: Boolean,
    onDraftChange: (String) -> Unit,
    onAtClick: () -> Unit,
    onWandClick: () -> Unit,
    onContinue: () -> Unit,
    focusRequester: FocusRequester,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BasicTextField(
            value = draft,
            onValueChange = onDraftChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 12.sp,
            ),
            cursorBrush = Brush.verticalGradient(
                colors = listOf(YoofiAuthCaretFrom, YoofiAuthCaretTo),
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onContinue() }),
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .focusRequester(focusRequester),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(YoofiAuthFieldFill, InputShape)
                        .border(2.dp, YoofiAuthIdleStroke, InputShape)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_chat_at),
                        contentDescription = stringResource(R.string.cd_chat_at),
                        modifier = Modifier
                            .size(20.dp)
                            .clickableDismissingIme(onClick = onAtClick),
                        contentScale = ContentScale.Fit,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (showPlaceholder) {
                            Text(
                                text = stringResource(R.string.chat_input_hint),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                            )
                        }
                        inner()
                    }
                    Image(
                        painter = painterResource(R.drawable.img_chat_wand),
                        contentDescription = stringResource(R.string.cd_chat_inspire),
                        modifier = Modifier
                            .size(20.dp)
                            .clickableDismissingIme(onClick = onWandClick),
                        contentScale = ContentScale.Fit,
                    )
                }
            },
        )
        Row(
            modifier = Modifier
                .height(46.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(YoofiStartGameFrom, YoofiStartGameTo),
                    ),
                    ContinueShape,
                )
                .clickableDismissingIme(onClick = onContinue)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_chat_play_circle),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = stringResource(R.string.chat_continue),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun InspirationList(
    lines: List<String>,
    onPick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        lines.forEach { line ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YoofiAuthOtpEmpty, RoundedCornerShape(12.dp))
                    .clickableDismissingIme(onClick = { onPick(line) })
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = line,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                Image(
                    painter = painterResource(R.drawable.img_chat_quill),
                    contentDescription = null,
                    modifier = Modifier.size(width = 28.dp, height = 20.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
