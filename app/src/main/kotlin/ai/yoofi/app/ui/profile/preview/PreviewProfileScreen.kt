package ai.yoofi.app.ui.profile.preview

import ai.yoofi.app.R
import ai.yoofi.app.domain.profile.PreviewPlayedGenre
import ai.yoofi.app.domain.profile.PreviewPlayedWork
import ai.yoofi.app.ui.pager.animateToRealPage
import ai.yoofi.app.ui.pager.loopingPageCount
import ai.yoofi.app.ui.pager.loopingStartPage
import ai.yoofi.app.ui.pager.realPageIndex
import ai.yoofi.app.ui.profile.ProfileIdentity
import ai.yoofi.app.ui.profile.ProfileIdentityCard
import ai.yoofi.app.ui.profile.ProfileLorebookEmptyPane
import ai.yoofi.app.ui.profile.ProfilePageBackground
import ai.yoofi.app.ui.profile.ProfileStat
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiAuthFocusStroke
import ai.yoofi.app.ui.theme.YoofiCameraTo
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/** 预览页一级 Tab，对齐 Figma `2252:19531`：Played / Lorebook / Props。 */
internal enum class PreviewProfileTab {
    Played,
    Lorebook,
    Props,
}

/**
 * 我的个人页预览，对齐 Figma `2252:19446`。
 * 别人眼里的公开资料：无设置 / 铅笔 / 复制 / VIP / 三点拉黑。
 */
@Composable
internal fun PreviewProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreviewProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PreviewProfileLayout(
        works = state.works,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun PreviewProfileLayout(
    works: List<PreviewPlayedWork>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = PreviewProfileTab.entries
    val cycle = tabs.size
    val pagerState = rememberPagerState(
        initialPage = loopingStartPage(cycle),
        pageCount = { loopingPageCount(cycle) },
    )
    val selected = tabs[realPageIndex(pagerState.currentPage, cycle)]
    val scope = rememberCoroutineScope()
    val identity = ProfileIdentity(
        displayName = stringResource(R.string.me_display_name),
        publicId = stringResource(R.string.me_user_id),
        avatarRes = R.drawable.img_me_avatar,
        stats = listOf(
            ProfileStat(
                count = stringResource(R.string.me_following_count),
                label = stringResource(R.string.me_following_label),
            ),
            ProfileStat(
                count = stringResource(R.string.me_follower_count),
                label = stringResource(R.string.me_follower_label),
            ),
        ),
    )
    BackHandler(onBack = onBack)
    Box(modifier = modifier.fillMaxSize()) {
        ProfilePageBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            PreviewTopBar(onBack = onBack)
            ProfileIdentityCard(
                identity = identity,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 1.dp),
                avatarBadge = { PreviewFollowBadge() },
            )
            Spacer(Modifier.height(16.dp))
            PreviewProfileTabs(
                selected = selected,
                onSelected = { tab ->
                    scope.launch {
                        pagerState.animateToRealPage(tabs.indexOf(tab), cycle)
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                beyondViewportPageCount = 1,
                key = { page -> page },
            ) { page ->
                when (tabs[realPageIndex(page, cycle)]) {
                    PreviewProfileTab.Played -> PreviewPlayedPane(
                        works = works,
                        modifier = Modifier.fillMaxSize(),
                    )
                    PreviewProfileTab.Lorebook -> {
                        ProfileLorebookEmptyPane(Modifier.fillMaxSize())
                    }
                    PreviewProfileTab.Props -> {
                        ProfileLorebookEmptyPane(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 20.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_auth_back),
            contentDescription = stringResource(R.string.cd_detail_back),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onBack),
        )
        Text(
            text = stringResource(R.string.me_preview_profile),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun PreviewProfileTabs(
    selected: PreviewProfileTab,
    onSelected: (PreviewProfileTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(30.dp),
    ) {
        PreviewTabItem(
            label = stringResource(R.string.me_tab_played),
            selected = selected == PreviewProfileTab.Played,
            onClick = { onSelected(PreviewProfileTab.Played) },
        )
        PreviewTabItem(
            label = stringResource(R.string.me_tab_lorebook),
            selected = selected == PreviewProfileTab.Lorebook,
            onClick = { onSelected(PreviewProfileTab.Lorebook) },
        )
        PreviewTabItem(
            label = stringResource(R.string.me_tab_props),
            selected = selected == PreviewProfileTab.Props,
            onClick = { onSelected(PreviewProfileTab.Props) },
        )
    }
}

@Composable
private fun PreviewTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(8.dp, 4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(if (selected) Color.White else Color.Transparent),
        )
    }
}

@Composable
private fun PreviewPlayedPane(
    works: List<PreviewPlayedWork>,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
    ) {
        items(works, key = { it.id }) { item ->
            PreviewPlayedCard(item = item)
        }
    }
}

private val CardOverlayBrush = Brush.verticalGradient(
    0.57105f to Color.Transparent,
    0.72478f to Color(0x7A110F3A),
    0.96459f to Color(0x8A1F003F),
)

@Composable
private fun PreviewPlayedCard(item: PreviewPlayedWork) {
    val title = stringResource(R.string.card_arranged_marriage)
    val genre = stringResource(previewGenreLabelRes(item.genre))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        Image(
            painter = painterResource(previewCoverRes(item.coverKey)),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardOverlayBrush),
        )
        Text(
            text = genre,
            color = previewGenreTagColor(item.genre),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x80070707))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 15.dp, bottom = 10.dp)
                .fillMaxWidth(),
        )
    }
}

/** 客态关注钮的公开态：别人未关注时看到的 +，预览里不可点。 */
@Composable
private fun PreviewFollowBadge() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    listOf(YoofiAuthFocusStroke, YoofiCameraTo),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_follow_plus),
            contentDescription = null,
            modifier = Modifier.size(13.dp),
        )
    }
}

@StringRes
private fun previewGenreLabelRes(genre: PreviewPlayedGenre): Int = when (genre) {
    PreviewPlayedGenre.IndieGames -> R.string.preview_genre_indie
    PreviewPlayedGenre.MurderMystery -> R.string.preview_genre_mystery
}

private fun previewGenreTagColor(genre: PreviewPlayedGenre): Color = when (genre) {
    PreviewPlayedGenre.IndieGames -> Color(0xFFB1B1B1)
    PreviewPlayedGenre.MurderMystery -> Color.White.copy(alpha = 0.7f)
}

@DrawableRes
private fun previewCoverRes(coverKey: String): Int = when (coverKey) {
    "cover-e" -> R.drawable.img_game_cover_e
    "cover-d" -> R.drawable.img_game_cover_d
    "cover-c" -> R.drawable.img_game_cover_c
    else -> R.drawable.img_game_cover_a
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewProfilePlayedPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        PreviewProfileLayout(
            works = remember {
                listOf(
                    PreviewPlayedWork("a", "cover-e", PreviewPlayedGenre.IndieGames),
                    PreviewPlayedWork("b", "cover-d", PreviewPlayedGenre.MurderMystery),
                    PreviewPlayedWork("c", "cover-a", PreviewPlayedGenre.IndieGames),
                    PreviewPlayedWork("d", "cover-c", PreviewPlayedGenre.MurderMystery),
                )
            },
            onBack = {},
        )
    }
}
