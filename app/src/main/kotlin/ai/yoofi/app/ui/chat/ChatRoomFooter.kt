package ai.yoofi.app.ui.chat

import ai.yoofi.app.R
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.ime.cursorAtEnd
import ai.yoofi.app.ui.ime.dismissIme
import ai.yoofi.app.ui.ime.rememberCursorAtEndField
import ai.yoofi.app.ui.theme.YoofiAuthCaretFrom
import ai.yoofi.app.ui.theme.YoofiAuthCaretTo
import ai.yoofi.app.ui.theme.YoofiAuthFieldFill
import ai.yoofi.app.ui.theme.YoofiAuthFocusStroke
import ai.yoofi.app.ui.theme.YoofiAuthIdleStroke
import ai.yoofi.app.ui.theme.YoofiAuthOtpEmpty
import ai.yoofi.app.ui.theme.YoofiChatChipCast
import ai.yoofi.app.ui.theme.YoofiChatChipItems
import ai.yoofi.app.ui.theme.YoofiChatChipMap
import ai.yoofi.app.ui.theme.YoofiChatChipRecap
import ai.yoofi.app.ui.theme.YoofiChatInputFocusStroke
import ai.yoofi.app.ui.theme.YoofiGameBg0
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
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
private val InputHeight = 46.dp

/** 编辑态右端的圆形发送键；[SendButtonInset] 是它到输入框右内壁的距离 */
private val SendButtonSize = 36.dp
private val SendButtonInset = 5.dp

private val InspirationShape = RoundedCornerShape(12.dp)
private val InspirationGap = 4.dp
private val InspirationItemHeight = 44.dp
private val InspirationQuillWidth = 28.dp
private val InspirationQuillHeight = 20.dp

/**
 * 灵感列表与输入行之间的额外间距。
 * 外层 Column 统一 8dp，设计稿这里要 12dp，差的 4dp 补在列表自己头上，不动整体节奏。
 */
private val InspirationTopExtra = 4.dp

/**
 * 刚打出的字符是「独立的 @」才算唤起：句首，或空白后面。
 * 长度必须刚好 +1，避免粘贴整段带 @ 的句子误开面板。
 */
private fun isMentionTrigger(previous: String, next: String): Boolean {
    if (next.length != previous.length + 1) return false
    if (!next.endsWith('@')) return false
    val before = next.dropLast(1)
    return before.isEmpty() || before.last().isWhitespace()
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
            onClick = { onIntent(ChatRoomIntent.OpenMap) },
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
internal fun ChatRoomFooter(
    state: ChatRoomUiState,
    onIntent: (ChatRoomIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val mentionOpen = state.overlay == ChatRoomOverlay.Mention
    val inspireOpen = state.overlay == ChatRoomOverlay.Inspiration
    // 「选中成员」「选中灵感」这两刻才置位。面板刚打开时不能抢焦点：
    // BasicTextField 一拿到焦点就会开输入会话弹键盘，把面板挡掉
    var requestInputFocus by remember { mutableStateOf(false) }
    var inputFocused by remember { mutableStateOf(false) }
    // 有焦点或有草稿都算「玩家正在组织这句话」，输入框吃满整行并亮出发送键。
    // 不只看草稿是否为空，否则敲下第一个字的瞬间整行会跳一次宽度
    val editing = inputFocused || state.draft.isNotEmpty()

    fun dispatchSend(intent: ChatRoomIntent) {
        onIntent(intent)
        // ViewModel 已把草稿清空；焦点若还在，editing 仍为 true，会停在编辑态。
        // Continue / 圆形发送 / IME Send / 点灵感本体 都走这里，回到空闲态。
        inputFocused = false
        dismissIme(focusManager, keyboard)
    }

    LaunchedEffect(requestInputFocus) {
        if (requestInputFocus) {
            focusRequester.requestFocus()
            requestInputFocus = false
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
                // 底部安全区由聊天室根上的 imeAvoidingPadding 统一吃掉：
                // 键盘弹起贴 IME，收回只剩导航栏，避免这里再垫一次叠出空隙
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
                        onIntent = { intent ->
                            // 选完成员要接着补后半句，这时才该把焦点和键盘给输入框；
                            // 翻页等其它意图不碰焦点，免得翻一页弹一次键盘
                            if (intent is ChatRoomIntent.PickMention) {
                                requestInputFocus = true
                            }
                            onIntent(intent)
                        },
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
            InputRow(
                draft = state.draft,
                editing = editing,
                mentionActive = mentionOpen,
                inspireActive = inspireOpen,
                // 原来 @ 面板一开就抢焦点，输入框有光标才藏提示语；现在不抢焦点了，
                // 再藏提示语会得到一个既无光标又无文案的空框，所以只看草稿是否为空
                showPlaceholder = state.draft.isEmpty(),
                onDraftChange = { next ->
                    val typedAt = !mentionOpen && isMentionTrigger(state.draft, next)
                    onIntent(ChatRoomIntent.DraftChanged(next))
                    // 句首或空白后新打的 @ 才开列表；已打开时不再派发，避免 OpenMention 把面板 toggle 掉
                    if (typedAt) {
                        onIntent(ChatRoomIntent.OpenMention)
                    }
                },
                onAtClick = { onIntent(ChatRoomIntent.OpenMention) },
                onWandClick = { onIntent(ChatRoomIntent.OpenInspiration) },
                onContinue = { dispatchSend(ChatRoomIntent.ContinueStory) },
                onFocusChanged = { inputFocused = it },
                focusRequester = focusRequester,
            )
            if (inspireOpen) {
                InspirationList(
                    lines = state.inspirations,
                    // 羽毛：填进输入框继续改。顺带把焦点要过来，玩家可以直接接着敲
                    onEdit = {
                        onIntent(ChatRoomIntent.PickInspiration(it))
                        requestInputFocus = true
                    },
                    onSend = { dispatchSend(ChatRoomIntent.SendInspiration(it)) },
                    modifier = Modifier.padding(top = InspirationTopExtra),
                )
            }
        }
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

/**
 * 输入行有两副面孔，由 [editing] 切换：
 *
 * - 空闲态：输入框让出右侧给 Continue 按钮，框内左 @ 右魔法棒，2dp 浅描边。
 * - 编辑态：输入框吃满整行，@ 与魔法棒收起，右端嵌 36dp 圆形发送键，1dp 亮描边。
 *
 * 两态的宽度差不是写死的：Continue 在编辑态整个不渲染，输入框的 `weight(1f)` 自然铺满，
 * 所以设计稿的 238 / 350 两个宽度是算出来的，不用各摆一份常量。
 *
 * 圆形发送键与 Continue 绑的是同一个 [onContinue]：编辑态下它就是 Continue 的另一副皮，
 * 行为必须一致，否则同一个动作会分裂成两套规则。
 */
@Composable
private fun InputRow(
    draft: String,
    editing: Boolean,
    mentionActive: Boolean,
    inspireActive: Boolean,
    showPlaceholder: Boolean,
    onDraftChange: (String) -> Unit,
    onAtClick: () -> Unit,
    onWandClick: () -> Unit,
    onContinue: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    focusRequester: FocusRequester,
) {
    val field = rememberCursorAtEndField(draft, onDraftChange)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BasicTextField(
            value = field.value,
            onValueChange = field.onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
            ),
            cursorBrush = Brush.verticalGradient(
                colors = listOf(YoofiAuthCaretFrom, YoofiAuthCaretTo),
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onContinue() }),
            modifier = Modifier
                .weight(1f)
                .height(InputHeight)
                .focusRequester(focusRequester)
                .cursorAtEnd(field)
                .onFocusChanged { onFocusChanged(it.isFocused) },
            decorationBox = { inner ->
                if (editing) {
                    EditingFieldDecoration(
                        showPlaceholder = showPlaceholder,
                        onSend = onContinue,
                        inner = inner,
                    )
                } else {
                    IdleFieldDecoration(
                        mentionActive = mentionActive,
                        inspireActive = inspireActive,
                        showPlaceholder = showPlaceholder,
                        onAtClick = onAtClick,
                        onWandClick = onWandClick,
                        inner = inner,
                    )
                }
            },
        )
        if (!editing) {
            ContinueButton(onContinue = onContinue)
        }
    }
}

/** 空闲态输入框：左 @、中提示语、右魔法棒。 */
@Composable
private fun IdleFieldDecoration(
    mentionActive: Boolean,
    inspireActive: Boolean,
    showPlaceholder: Boolean,
    onAtClick: () -> Unit,
    onWandClick: () -> Unit,
    inner: @Composable () -> Unit,
) {
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
            // 资源本体是灰底路径，用 SrcIn 盖成设计稿两态：默认白、点开后 #5257FF
            colorFilter = ColorFilter.tint(
                if (mentionActive) YoofiAuthFocusStroke else Color.White,
            ),
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
            InputPlaceholder(visible = showPlaceholder)
            inner()
        }
        Image(
            // 默认棒、列表打开用选中棒，两套位图不要用 tint 混成一态
            painter = painterResource(
                if (inspireActive) R.drawable.img_chat_wand_select else R.drawable.img_chat_wand,
            ),
            contentDescription = stringResource(R.string.cd_chat_inspire),
            modifier = Modifier
                .size(20.dp)
                .clickableDismissingIme(onClick = onWandClick),
            contentScale = ContentScale.Fit,
        )
    }
}

/** 编辑态输入框：文字铺到底，右端嵌圆形发送键。 */
@Composable
private fun EditingFieldDecoration(
    showPlaceholder: Boolean,
    onSend: () -> Unit,
    inner: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YoofiAuthFieldFill, InputShape)
            .border(1.dp, YoofiChatInputFocusStroke, InputShape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    // 让开发送键，再留 8dp 余量，光标走到末尾不会顶到按钮
                    end = SendButtonSize + SendButtonInset + 8.dp,
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            InputPlaceholder(visible = showPlaceholder)
            inner()
        }
        Image(
            painter = painterResource(R.drawable.ic_chat_send),
            contentDescription = stringResource(R.string.cd_chat_send),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = SendButtonInset)
                .size(SendButtonSize)
                // 这里不收键盘：发完往往还要接着说下一句
                .clickable(role = Role.Button, onClick = onSend),
            contentScale = ContentScale.Fit,
        )
    }
}

/** 两态共用的提示语，字号固定 12，比输入文字的 14 小一号，与设计一致。 */
@Composable
private fun InputPlaceholder(visible: Boolean) {
    if (!visible) return
    Text(
        text = stringResource(R.string.chat_input_hint),
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 12.sp,
    )
}

@Composable
private fun ContinueButton(onContinue: () -> Unit) {
    Row(
        modifier = Modifier
            .height(InputHeight)
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

/**
 * 灵感列表。一条条目上有两个互不相同的动作：
 * 左侧羽毛 = 填进输入框接着改（[onEdit]），条目本体 = 这句就发（[onSend]）。
 *
 * 羽毛的点击挂在内层，Compose 会让内层先吃掉手势，不会再冒泡到外层的整条点击，
 * 所以点羽毛不会误触发送。
 */
@Composable
private fun InspirationList(
    lines: List<String>,
    onEdit: (String) -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(InspirationGap),
    ) {
        lines.forEach { line ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(InspirationItemHeight)
                    .background(YoofiAuthOtpEmpty, InspirationShape)
                    .clickableDismissingIme(onClick = { onSend(line) })
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 图形只有 28×20，但点击区撑满行高，免得 20dp 的热区太难点
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(InspirationQuillWidth)
                        .clickableDismissingIme(onClick = { onEdit(line) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_chat_inspiration_quill),
                        contentDescription = stringResource(R.string.cd_chat_inspire_edit),
                        modifier = Modifier.size(
                            width = InspirationQuillWidth,
                            height = InspirationQuillHeight,
                        ),
                        contentScale = ContentScale.Fit,
                    )
                }
                Text(
                    text = line,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
