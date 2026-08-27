package ai.yoofi.app.ui.chat

import ai.yoofi.app.R
import ai.yoofi.app.domain.chat.ChatCastMember
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiChatMentionFrom
import ai.yoofi.app.ui.theme.YoofiChatMentionStroke
import ai.yoofi.app.ui.theme.YoofiChatMentionTo
import ai.yoofi.app.ui.theme.YoofiChatPagerFill
import ai.yoofi.app.ui.theme.YoofiDialogScrim
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MentionShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
private val PagerShape = RoundedCornerShape(120.dp)

@Composable
internal fun ChatCastOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YoofiDialogScrim)
            .clickableDismissingIme(role = null, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 卡背翻转揭示人物卡，动画细节见 ChatCastFlipCard
            ChatCastFlipCard(
                modifier = Modifier.clickableDismissingIme(role = null, onClick = {}),
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickableDismissingIme(role = Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_chat_close),
                    contentDescription = stringResource(R.string.cd_chat_close),
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Composable
internal fun ChatMentionSheet(
    members: List<ChatCastMember>,
    pageIndex: Int,
    pageCount: Int,
    onIntent: (ChatRoomIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canPrev = pageIndex > 0
    val canNext = pageIndex < pageCount - 1
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(273.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(YoofiChatMentionFrom, YoofiChatMentionTo),
                ),
                MentionShape,
            )
            .border(0.5.dp, YoofiChatMentionStroke, MentionShape),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chat_close),
            contentDescription = stringResource(R.string.cd_chat_close),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 12.dp)
                .size(16.dp)
                .clickableDismissingIme {
                    onIntent(ChatRoomIntent.DismissOverlay)
                },
            contentScale = ContentScale.Fit,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        ) {
            members.forEach { member ->
                MentionRow(
                    member = member,
                    onClick = { onIntent(ChatRoomIntent.PickMention(member.id)) },
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .width(72.dp)
                .height(24.dp)
                .background(YoofiChatPagerFill, PagerShape),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_chat_chevron),
                contentDescription = stringResource(R.string.cd_chat_mention_prev),
                modifier = Modifier
                    .size(12.dp)
                    .clickableDismissingIme(
                        enabled = canPrev,
                        onClick = { onIntent(ChatRoomIntent.MentionPrevPage) },
                    ),
                alpha = if (canPrev) 0.8f else 0.1f,
                contentScale = ContentScale.Fit,
            )
            Text(
                text = stringResource(
                    R.string.chat_mention_page,
                    pageIndex + 1,
                    pageCount.coerceAtLeast(1),
                ),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp,
                letterSpacing = 2.sp,
            )
            Image(
                painter = painterResource(R.drawable.ic_chat_chevron),
                contentDescription = stringResource(R.string.cd_chat_mention_next),
                modifier = Modifier
                    .size(12.dp)
                    .rotate(180f)
                    .clickableDismissingIme(
                        enabled = canNext,
                        onClick = { onIntent(ChatRoomIntent.MentionNextPage) },
                    ),
                alpha = if (canNext) 0.8f else 0.1f,
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun MentionRow(
    member: ChatCastMember,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableDismissingIme(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(chatAvatarRes(member.avatarKey)),
            contentDescription = null,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = member.displayName,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
        )
        Text(
            text = member.identity,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
