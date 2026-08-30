package ai.yoofi.app.ui.gamedetail

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameAuthor
import ai.yoofi.app.domain.gamedetail.GameCastMember
import ai.yoofi.app.ui.ime.clickableDismissingIme
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Figma `1943:13437`：Follow 药丸左右 8、上下 4、全圆角。 */
private val FollowPillShape = RoundedCornerShape(100.dp)

/** Figma `1943:13474`：正文 14/18，收起时露 3 行（54 高）。 */
private val BodyFontSize = 14.sp
private val BodyLineHeight = 18.sp
private const val SynopsisCollapsedLines = 3

/** Figma `1943:13463`：Cast 格 68 宽，头像 68 见方、圆角 12，格间距 8。 */
private val CastItemWidth = 68.dp
private val CastPortraitSize = 68.dp
private val CastPortraitShape = RoundedCornerShape(12.dp)
private val CastItemGap = 8.dp

/** Figma `1943:13433`：作者头像 24，与名字、Follow 之间都是 4。 */
@Composable
internal fun DetailAuthorRow(
    author: GameAuthor,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(detailAvatarRes(author.avatarKey)),
            contentDescription = author.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickableDismissingIme(role = Role.Button, onClick = onAvatarClick),
        )
        Text(
            text = author.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .clip(FollowPillShape)
                .background(DetailActionBrush)
                .clickableDismissingIme(role = Role.Checkbox, onClick = onToggleFollow)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = stringResource(
                    if (author.following) R.string.detail_following else R.string.detail_follow,
                ),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Figma `1943:13471`：小标题 + 正文，右下角一枚展开/收起圆钮。
 *
 * 圆钮压在正文最后一行右端，这是设计稿的画法（导出里就是重叠布局），不是失误。
 */
@Composable
internal fun DetailSynopsis(
    title: String,
    body: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = DetailPagePadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = body,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = BodyFontSize,
                lineHeight = BodyLineHeight,
                textAlign = TextAlign.Justify,
                maxLines = if (expanded) Int.MAX_VALUE else SynopsisCollapsedLines,
                overflow = TextOverflow.Ellipsis,
            )
            Image(
                painter = painterResource(R.drawable.ic_detail_expand),
                contentDescription = stringResource(
                    if (expanded) {
                        R.string.cd_detail_collapse_synopsis
                    } else {
                        R.string.cd_detail_expand_synopsis
                    },
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .clickableDismissingIme(onClick = onToggle)
                    // 展开后箭头朝上，翻转复用同一份资源
                    .rotate(if (expanded) 180f else 0f),
            )
        }
    }
}

/** Figma `1943:13457`：标题行 + 一排可横滑的角色头像，最后一格被屏幕右缘裁掉以暗示可滑。 */
@Composable
internal fun DetailCastSection(
    cast: List<GameCastMember>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DetailPagePadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.detail_cast),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.detail_cast_see_all),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(CastItemGap),
        ) {
            Spacer(Modifier.width(DetailPagePadding))
            cast.forEach { member -> CastItem(member) }
            Spacer(Modifier.width(DetailPagePadding))
        }
    }
}

@Composable
private fun CastItem(member: GameCastMember) {
    Column(
        modifier = Modifier.width(CastItemWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(detailCastRes(member.portraitKey)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(CastPortraitSize)
                .clip(CastPortraitShape),
        )
        Text(
            text = member.name,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * 标识到本地图的映射。Demo 阶段封面 / 头像都是标识串，接真实接口后这里换成 Coil 加载 URL，
 * 领域模型与调用方都不用动。未知标识回落到演示图，避免联调期缺图直接崩。
 */
@DrawableRes
internal fun detailCoverRes(key: String): Int = when (key) {
    "cover-forbidden-world" -> R.drawable.img_detail_hero
    else -> R.drawable.img_detail_hero
}

@DrawableRes
internal fun detailCastRes(key: String): Int = when (key) {
    "cast-1" -> R.drawable.img_detail_cast_1
    "cast-2" -> R.drawable.img_detail_cast_2
    "cast-3" -> R.drawable.img_detail_cast_3
    "cast-4" -> R.drawable.img_detail_cast_4
    else -> R.drawable.img_detail_cast_1
}

@DrawableRes
internal fun detailAvatarRes(key: String): Int = when (key) {
    "avatar-author" -> R.drawable.img_detail_avatar_author
    "avatar-1" -> R.drawable.img_detail_avatar_1
    "avatar-2" -> R.drawable.img_detail_avatar_2
    "avatar-3" -> R.drawable.img_detail_avatar_3
    else -> R.drawable.img_detail_avatar_1
}
