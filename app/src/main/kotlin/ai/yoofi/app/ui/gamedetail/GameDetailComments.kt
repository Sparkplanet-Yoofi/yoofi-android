package ai.yoofi.app.ui.gamedetail

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameComment
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiDetailAuthorBadgeFrom
import ai.yoofi.app.ui.theme.YoofiDetailAuthorBadgeTo
import ai.yoofi.app.ui.theme.YoofiDetailCommentBody
import ai.yoofi.app.ui.theme.YoofiDetailFieldFill
import ai.yoofi.app.ui.theme.YoofiDetailFieldStroke
import ai.yoofi.app.ui.theme.YoofiDetailHoursBadgeFill
import ai.yoofi.app.ui.theme.YoofiDetailHoursBadgeText
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Figma `1943:13618`：输入框 350×80、圆角 12、整体 60% 不透明。 */
private val InputBoxHeight = 80.dp
private val InputBoxShape = RoundedCornerShape(12.dp)
private const val InputBoxAlpha = 0.6f

/** Figma `1943:13623` / `1943:13620`：占位文案内缩 16，底排图标内缩 10。 */
private val InputTextInset = 16.dp
private val InputIconInset = 10.dp

/** Figma `1943:13548`：评论列表顶边 1032，输入框底边 1012。 */
private val CommentsTop = 20.dp
private val CommentGap = 24.dp

/** Figma `1889:12959` / `1943:13553`：主楼头像 30，楼中楼 22。 */
private val TopLevelAvatarSize = 30.dp
private val ReplyAvatarSize = 22.dp

/** Figma `1943:13551`：楼中楼整体缩进 38。 */
private val ReplyIndent = 38.dp

/** 操作行整体 80% 不透明，与设计稿 `opacity-80` 对齐。 */
private const val CommentActionAlpha = 0.8f

/**
 * 互动区：标题 + 发言框 + 评论树，对应 Figma `1943:13542` 起的整段。
 */
@Composable
internal fun DetailInteractionSection(
    state: GameDetailUiState,
    onIntent: (GameDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = DetailPagePadding)) {
        Text(
            text = stringResource(R.string.detail_interaction_area),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(16.dp))
        CommentComposer(
            draft = state.draft,
            onDraftChange = { onIntent(GameDetailIntent.DraftChanged(it)) },
            onSubmit = { onIntent(GameDetailIntent.SubmitComment) },
        )
        Spacer(Modifier.height(CommentsTop))
        state.comments.forEachIndexed { index, comment ->
            if (index > 0) Spacer(Modifier.height(CommentGap))
            CommentThread(
                comment = comment,
                repliesCollapsed = comment.id in state.collapsedReplyIds,
                onIntent = onIntent,
            )
        }
    }
}

/** Figma `1943:13617`：占位文案在左上，底排左边配图、右边发送。 */
@Composable
private fun CommentComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val canSend = draft.isNotBlank()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(InputBoxHeight)
            .alpha(InputBoxAlpha)
            .clip(InputBoxShape)
            .background(YoofiDetailFieldFill)
            .border(1.dp, YoofiDetailFieldStroke, InputBoxShape),
    ) {
        BasicTextField(
            value = draft,
            onValueChange = onDraftChange,
            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
            cursorBrush = DetailActionBrush,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = InputTextInset, top = InputTextInset, end = InputTextInset),
            decorationBox = { inner ->
                if (draft.isEmpty()) {
                    Text(
                        text = stringResource(R.string.detail_comment_hint),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                    )
                }
                inner()
            },
        )
        Image(
            painter = painterResource(R.drawable.ic_detail_image),
            contentDescription = stringResource(R.string.cd_detail_attach_image),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = InputIconInset, bottom = InputIconInset)
                .size(16.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = InputIconInset, bottom = InputIconInset)
                .size(30.dp)
                .clip(CircleShape)
                .background(DetailActionBrush)
                // 设计稿的发送键是 50% 不透明的待发状态，有内容才点亮
                .alpha(if (canSend) 1f else 0.5f)
                .clickableDismissingIme(enabled = canSend, onClick = onSubmit),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_detail_send),
                contentDescription = stringResource(R.string.cd_detail_send),
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

/** 一个主楼加它的回复，外带底部「Hide Replies」开关。 */
@Composable
private fun CommentThread(
    comment: GameComment,
    repliesCollapsed: Boolean,
    onIntent: (GameDetailIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CommentRow(comment = comment, avatarSize = TopLevelAvatarSize, onIntent = onIntent)
        if (comment.replies.isEmpty()) return@Column
        if (!repliesCollapsed) {
            comment.replies.forEach { reply ->
                Spacer(Modifier.height(CommentGap))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.width(ReplyIndent))
                    CommentRow(
                        comment = reply,
                        avatarSize = ReplyAvatarSize,
                        onIntent = onIntent,
                    )
                }
            }
        }
        Spacer(Modifier.height(CommentGap))
        Row(
            modifier = Modifier
                .padding(start = ReplyIndent)
                .clickableDismissingIme {
                    onIntent(GameDetailIntent.ToggleReplies(comment.id))
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_detail_arrow_up),
                contentDescription = null,
                modifier = Modifier.size(16.dp).rotateWhen(repliesCollapsed),
            )
            Text(
                text = stringResource(
                    if (repliesCollapsed) {
                        R.string.detail_show_replies
                    } else {
                        R.string.detail_hide_replies
                    },
                ),
                color = YoofiAccent,
                fontSize = 12.sp,
            )
        }
    }
}

/**
 * Figma `1889:12958`：头像在左，右侧依次是名字行、正文、操作行。
 *
 * 主楼与楼中楼结构完全一样，只有头像尺寸不同，所以共用这一个实现。
 */
@Composable
private fun CommentRow(
    comment: GameComment,
    avatarSize: Dp,
    onIntent: (GameDetailIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(detailAvatarRes(comment.avatarKey)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(avatarSize).clip(CircleShape),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CommentAuthorLine(comment)
            Text(
                text = comment.body,
                color = YoofiDetailCommentBody,
                fontSize = 14.sp,
                lineHeight = 18.sp,
            )
            CommentActionRow(comment = comment, onIntent = onIntent)
        }
    }
}

/** 名字 + Author 徽章 + 时长徽章；楼中楼则是「A reply B」。 */
@Composable
private fun CommentAuthorLine(comment: GameComment) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = comment.authorName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (comment.replyToName != null) {
            Text(
                text = stringResource(R.string.detail_comment_reply),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
            )
            Text(
                text = comment.replyToName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (comment.isAuthor) {
            CommentBadge(
                text = stringResource(R.string.detail_comment_author),
                textColor = Color.White,
                background = Brush.horizontalGradient(
                    listOf(YoofiDetailAuthorBadgeFrom, YoofiDetailAuthorBadgeTo),
                ),
            )
        }
        if (comment.playedBadge.isNotEmpty()) {
            CommentBadge(
                text = comment.playedBadge,
                textColor = YoofiDetailHoursBadgeText,
                background = SolidColor(YoofiDetailHoursBadgeFill),
            )
        }
    }
}

@Composable
private fun CommentBadge(text: String, textColor: Color, background: Brush) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** 点赞 / 回复数 / 删除靠左，时间靠右。设计稿字体是 DIN，工程未引入，退回系统字重。 */
@Composable
private fun CommentActionRow(
    comment: GameComment,
    onIntent: (GameDetailIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.alpha(CommentActionAlpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CommentAction(
                iconRes = R.drawable.ic_detail_thumbs_up,
                contentDescription = stringResource(R.string.cd_detail_like),
                count = comment.likeCount,
                highlighted = comment.liked,
                onClick = { onIntent(GameDetailIntent.ToggleLike(comment.id)) },
            )
            CommentAction(
                iconRes = R.drawable.ic_detail_comment,
                contentDescription = stringResource(R.string.cd_detail_reply),
                count = comment.replyCount,
            )
            if (comment.deletable) {
                Image(
                    painter = painterResource(R.drawable.ic_detail_trash),
                    contentDescription = stringResource(R.string.cd_detail_delete),
                    modifier = Modifier
                        .size(16.dp)
                        .clickableDismissingIme {
                            onIntent(GameDetailIntent.DeleteComment(comment.id))
                        },
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = comment.createdAtLabel,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun CommentAction(
    iconRes: Int,
    contentDescription: String,
    count: Int,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = if (onClick == null) {
            Modifier
        } else {
            Modifier.clickableDismissingIme(role = Role.Button, onClick = onClick)
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = if (highlighted) {
                ColorFilter.tint(YoofiAccent)
            } else {
                null
            },
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = count.toString(),
            color = if (highlighted) YoofiAccent else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** 折叠后箭头朝下。 */
private fun Modifier.rotateWhen(collapsed: Boolean): Modifier =
    if (collapsed) this.then(Modifier.rotate(180f)) else this
