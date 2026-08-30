package ai.yoofi.app.ui.profile

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class ProfileWork(
    val id: String,
    val kind: ProfileWorkKind,
    @param:DrawableRes val coverRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val viewsRes: Int,
)

internal val DemoProfileWorks = listOf(
    ProfileWork(
        id = "sg-a",
        kind = ProfileWorkKind.StoryGame,
        coverRes = R.drawable.img_game_cover_e,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    ProfileWork(
        id = "sg-b",
        kind = ProfileWorkKind.StoryGame,
        coverRes = R.drawable.img_game_cover_d,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    ProfileWork(
        id = "sg-c",
        kind = ProfileWorkKind.StoryGame,
        coverRes = R.drawable.img_game_cover_a,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    ProfileWork(
        id = "sg-d",
        kind = ProfileWorkKind.StoryGame,
        coverRes = R.drawable.img_game_cover_c,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    ProfileWork(
        id = "st-a",
        kind = ProfileWorkKind.Story,
        coverRes = R.drawable.img_home_listed_1,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    ProfileWork(
        id = "st-b",
        kind = ProfileWorkKind.Story,
        coverRes = R.drawable.img_home_listed_2,
        titleRes = R.string.card_arranged_short,
        viewsRes = R.string.me_views_sample,
    ),
    ProfileWork(
        id = "st-c",
        kind = ProfileWorkKind.Story,
        coverRes = R.drawable.img_home_listed_3,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    ProfileWork(
        id = "st-d",
        kind = ProfileWorkKind.Story,
        coverRes = R.drawable.img_game_cover_b,
        titleRes = R.string.card_arranged_short,
        viewsRes = R.string.me_views_sample,
    ),
)

private val CardOverlayBrush = Brush.verticalGradient(
    0.57105f to Color.Transparent,
    0.72478f to Color(0x7A110F3A),
    0.96459f to Color(0x8A1F003F),
)

@Composable
internal fun ProfileLorebookEmptyPane(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.me_lorebook_empty),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
        )
    }
}

/**
 * @param showDraftsBadge 主态草稿角标；客态不展示自己的草稿。
 */
@Composable
internal fun ProfileCreationsPane(
    workKind: ProfileWorkKind,
    onWorkKindChange: (ProfileWorkKind) -> Unit,
    modifier: Modifier = Modifier,
    showDraftsBadge: Boolean = true,
) {
    val items = remember(workKind) { DemoProfileWorks.filter { it.kind == workKind } }
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkKindChip(
                label = stringResource(R.string.me_filter_story_game),
                selected = workKind == ProfileWorkKind.StoryGame,
                onClick = { onWorkKindChange(ProfileWorkKind.StoryGame) },
            )
            WorkKindChip(
                label = stringResource(R.string.me_filter_story),
                selected = workKind == ProfileWorkKind.Story,
                onClick = { onWorkKindChange(ProfileWorkKind.Story) },
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(items, key = { it.id }) { item ->
                CreationCard(item = item, showDraftsBadge = showDraftsBadge)
            }
        }
    }
}

@Composable
private fun WorkKindChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(100.dp)
    Text(
        text = label,
        color = if (selected) YoofiAccent else Color.White.copy(alpha = 0.45f),
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .background(Color.Black)
                        .border(1.dp, YoofiAccent, shape)
                } else {
                    Modifier.background(Color.White.copy(alpha = 0.1f))
                },
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun CreationCard(
    item: ProfileWork,
    showDraftsBadge: Boolean,
) {
    val title = stringResource(item.titleRes)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(226.dp)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        Image(
            painter = painterResource(item.coverRes),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardOverlayBrush),
        )
        if (showDraftsBadge) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_me_drafts),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp, 9.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.me_badge_drafts),
                    color = YoofiStartGameFrom,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(item.viewsRes),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
            )
        }
    }
}
