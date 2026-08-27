package ai.yoofi.app.ui.chat

import ai.yoofi.app.R
import ai.yoofi.app.domain.chat.ChatEvent
import ai.yoofi.app.domain.chat.ChatEventKind
import ai.yoofi.app.domain.chat.ChatItem
import ai.yoofi.app.domain.chat.ChatSceneCharacter
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAuthIdleButton
import ai.yoofi.app.ui.theme.YoofiChatEventItemBg
import ai.yoofi.app.ui.theme.YoofiChatEventItemText
import ai.yoofi.app.ui.theme.YoofiChatEventLocationBg
import ai.yoofi.app.ui.theme.YoofiChatEventLocationText
import ai.yoofi.app.ui.theme.YoofiChatPlayerBubble
import ai.yoofi.app.ui.theme.YoofiChatPlayerText
import ai.yoofi.app.ui.theme.YoofiChatSceneAccent
import ai.yoofi.app.ui.theme.YoofiChatSceneCardFrom
import ai.yoofi.app.ui.theme.YoofiChatSceneCardTo
import ai.yoofi.app.ui.theme.YoofiChatSceneHint
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SpeechBubbleShape = RoundedCornerShape(
    topStart = 4.dp,
    topEnd = 12.dp,
    bottomEnd = 12.dp,
    bottomStart = 12.dp,
)

private val PlayerBubbleShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 30.dp,
    bottomEnd = 30.dp,
    bottomStart = 4.dp,
)

private val SceneCardShape = RoundedCornerShape(8.dp)
private val EventItemShape = RoundedCornerShape(100.dp)
private val EventLocationShape = RoundedCornerShape(12.dp)

/** 旁白书本图标与正文的间距，Figma 里正文左边缘落在 38dp（20 图标 + 18 间隔）。 */
private val NarrativeIconGap = 18.dp

/** 场景角色卡整体不透明度，头像与文案不受影响。 */
private const val SceneCardBackdropAlpha = 0.3f

/** 事件文案在 Figma 里是 70% 不透明。 */
private const val EventTextAlpha = 0.7f

@Composable
internal fun ChatMessageList(
    items: List<ChatItem>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onSceneCharacterClick: (ChatSceneCharacter) -> Unit = {},
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 16.dp,
        ),
    ) {
        itemsIndexed(items = items, key = { _, item -> item.id }) { index, item ->
            // 间距按前后两条的类型决定，故不用 verticalArrangement 统一撑开
            val topSpacing = if (index == 0) 0.dp else chatItemSpacing(items[index - 1], item)
            Column(modifier = Modifier.padding(top = topSpacing)) {
                when (item) {
                    is ChatItem.Narrative -> NarrativeRow(item, onSceneCharacterClick)
                    is ChatItem.Speech -> SpeechRow(item)
                    is ChatItem.Player -> PlayerRow(item)
                    is ChatItem.Events -> EventsRow(item)
                }
            }
        }
    }
}

/**
 * Figma `1826:9610` 中任务事件提示与相邻内容留 12dp，其余相邻内容留 16dp。
 */
internal fun chatItemSpacing(previous: ChatItem, current: ChatItem): Dp =
    if (previous is ChatItem.Events || current is ChatItem.Events) 12.dp else 16.dp

@Composable
private fun NarrativeRow(
    item: ChatItem.Narrative,
    onSceneCharacterClick: (ChatSceneCharacter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NarrativeIconGap),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chat_book_open),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            contentScale = ContentScale.Fit,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.body,
                color = Color.White,
                fontSize = 14.sp,
            )
            if (item.sceneCharacters.isNotEmpty()) {
                SceneCharacterBlock(
                    characters = item.sceneCharacters,
                    onCharacterClick = onSceneCharacterClick,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/** 旁白下方的「Scene Characters」分组，标题 + 若干角色卡。 */
@Composable
private fun SceneCharacterBlock(
    characters: List<ChatSceneCharacter>,
    onCharacterClick: (ChatSceneCharacter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.chat_scene_characters),
            color = YoofiChatSceneAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        characters.forEach { character ->
            SceneCharacterCard(
                character = character,
                onClick = { onCharacterClick(character) },
            )
        }
    }
}

@Composable
private fun SceneCharacterCard(character: ChatSceneCharacter, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(178.dp)
            .height(46.dp)
            .clip(SceneCardShape)
            .clickableDismissingIme(role = Role.Button, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = SceneCardBackdropAlpha }
                .background(
                    Brush.horizontalGradient(
                        0.0582f to YoofiChatSceneCardFrom,
                        0.9865f to YoofiChatSceneCardTo,
                    ),
                    SceneCardShape,
                )
                .border(1.dp, YoofiChatSceneAccent, SceneCardShape),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(chatAvatarRes(character.avatarKey)),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
            )
            // Figma 里文案区固定 129dp，超长时省略以免撑破 46dp 卡高
            Column(modifier = Modifier.width(129.dp)) {
                Text(
                    text = character.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.chat_scene_character_hint),
                    color = YoofiChatSceneHint,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** 一组连续的任务事件提示，居中排列，组内间距 4dp。 */
@Composable
private fun EventsRow(item: ChatItem.Events) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item.events.forEach { event -> EventPill(event) }
    }
}

@Composable
private fun EventPill(event: ChatEvent) {
    val isItem = event.kind == ChatEventKind.ItemAcquired
    val text = when (event.kind) {
        ChatEventKind.ItemAcquired ->
            stringResource(R.string.chat_event_item_acquired, event.subject)
        ChatEventKind.LocationUnlocked ->
            stringResource(R.string.chat_event_location_unlocked, event.subject)
    }
    Row(
        modifier = Modifier
            .background(
                color = if (isItem) YoofiChatEventItemBg else YoofiChatEventLocationBg,
                shape = if (isItem) EventItemShape else EventLocationShape,
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(
                if (isItem) {
                    R.drawable.img_chat_icon_items
                } else {
                    R.drawable.img_chat_icon_map
                },
            ),
            contentDescription = null,
            modifier = if (isItem) {
                Modifier.size(14.dp)
            } else {
                Modifier.size(width = 16.dp, height = 15.dp)
            },
            contentScale = ContentScale.Fit,
        )
        Text(
            text = text,
            color = (if (isItem) YoofiChatEventItemText else YoofiChatEventLocationText)
                .copy(alpha = EventTextAlpha),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun SpeechRow(item: ChatItem.Speech) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(chatAvatarRes(item.avatarKey)),
            contentDescription = item.speakerName,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.speakerName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                AudioPill(seconds = item.audioSeconds)
            }
            Text(
                text = item.body,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .background(YoofiAuthIdleButton, SpeechBubbleShape)
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun PlayerRow(item: ChatItem.Player) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Text(
            text = item.body,
            color = YoofiChatPlayerText,
            fontSize = 14.sp,
            modifier = Modifier
                .widthIn(max = 276.dp)
                .background(YoofiChatPlayerBubble, PlayerBubbleShape)
                .padding(12.dp),
        )
    }
}

@Composable
private fun AudioPill(seconds: Int) {
    Row(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    0.11818f to YoofiStartGameFrom,
                    1f to YoofiStartGameTo,
                ),
                RoundedCornerShape(100.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chat_volume_min),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = stringResource(R.string.chat_audio_seconds, seconds),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@DrawableRes
internal fun chatAvatarRes(avatarKey: String): Int = when (avatarKey) {
    "tomy" -> R.drawable.img_chat_avatar_tomy
    else -> R.drawable.img_chat_avatar_anmi
}
