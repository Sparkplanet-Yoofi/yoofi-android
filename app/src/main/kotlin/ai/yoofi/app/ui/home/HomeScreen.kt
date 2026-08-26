package ai.yoofi.app.ui.home

import ai.yoofi.app.R
import ai.yoofi.app.ui.game.GameSectionHeader
import ai.yoofi.app.ui.theme.YoofiChipText
import ai.yoofi.app.ui.theme.YoofiDisplaySerif
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import ai.yoofi.app.ui.theme.YoofiTitleGradientEnd
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ListedCardSize = 138.dp to 182.dp
/** Figma `982:14637`：两卡间距 4dp、高度 154，宽度平分剩余空间。 */
private val LibraryCardHeight = 154.dp
/** Figma `982:14594`：Hero 背景图高度，只画在底层，不把 Listed Works 往下推。 */
private val HeroHeight = 518.dp
/** Figma 画布高度；全屏渐变色标按此计算，不能压成 518 否则中段会提前变黑。 */
private val FigmaCanvasHeight = 844.dp
/** Figma `982:14596`：顶部压暗，让状态栏文字可读。 */
private val HeroTopVignetteHeight = 332.dp
/** Figma：Start Game 底约 308，Listed Works 顶 354。 */
private val HeroToListedGap = 46.dp

/** Hero 轮播页。循环用虚页数，真实下标取模。 */
private data class HeroBanner(
    val id: String,
    @param:DrawableRes val coverRes: Int,
)

private val DemoHeroBanners = listOf(
    HeroBanner("hero-1", R.drawable.img_home_hero),
    HeroBanner("hero-2", R.drawable.img_home_listed_1),
    HeroBanner("hero-3", R.drawable.img_game_cover_e),
    HeroBanner("hero-4", R.drawable.img_home_library_1),
)

/**
 * 首页/探索，对齐 Figma `982:14591`。
 */
@Composable
fun HomeExploreScreen(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onStartGame: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp),
        ) {
            HomeHeroWithFeed(
                banners = DemoHeroBanners,
                onSearchClick = onSearchClick,
                onStartGame = onStartGame,
            )
        }
    }
}

/**
 * Hero 背景 518dp 叠在底层；文案 / 指示点按内容撑开，Listed Works 从按钮下方
 * 46dp 处叠上（Figma `982:14603` top=354），避免 518 把列表整块顶下去。
 * 轮播虚页数为 [Int.MAX_VALUE]，真实页 = page % size。
 */
@Composable
private fun HomeHeroWithFeed(
    banners: List<HeroBanner>,
    onSearchClick: () -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cycle = banners.size.coerceAtLeast(1)
    val looping = banners.size > 1
    val startPage = remember(cycle, looping) {
        if (!looping) {
            0
        } else {
            val mid = Int.MAX_VALUE / 2
            mid - mid % cycle
        }
    }
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { if (looping) Int.MAX_VALUE else cycle },
    )
    val realIndex = if (banners.isEmpty()) 0 else pagerState.currentPage.mod(cycle)

    Box(modifier = modifier.fillMaxWidth()) {
        HeroBackdrop(
            banners = banners,
            pagerState = pagerState,
            cycle = cycle,
            modifier = Modifier
                .fillMaxWidth()
                .height(HeroHeight)
                .align(Alignment.TopCenter),
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.statusBarsPadding()) {
                HomeTopBar(onSearchClick = onSearchClick)
                Spacer(Modifier.height(90.dp))
                // Figma：Start Game 与 4 个指示点同一行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 31.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    HeroCopy(onStartGame = onStartGame)
                    HeroDots(
                        count = banners.size,
                        selected = realIndex,
                    )
                }
            }
            Spacer(Modifier.height(HeroToListedGap))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                ListedWorksRow()
                GameLibraryBlock()
            }
        }
    }
}

@Composable
private fun HeroBackdrop(
    banners: List<HeroBanner>,
    pagerState: PagerState,
    cycle: Int,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    // 色标按 844 画布绝对高度，Last pick（约 198dp）仍落在透明段，图能铺满。
    val canvasEndY = with(density) { FigmaCanvasHeight.toPx() }
    val vignetteEndY = with(density) { HeroTopVignetteHeight.toPx() }

    Box(modifier = modifier) {
        if (banners.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { page -> page },
            ) { page ->
                val banner = banners[page.mod(cycle)]
                Image(
                    painter = painterResource(banner.coverRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    // Figma 图顶对齐并放大裁切，避免居中裁切露出上下黑边
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.049763f to Color.Black.copy(alpha = 0.5f),
                            0.12085f to Color.Transparent,
                            0.21209f to Color.Transparent,
                            0.36019f to Color(0xFF150B33),
                            0.52488f to Color(0xFF070514),
                            1f to Color(0xFF070514),
                        ),
                        startY = 0f,
                        endY = canvasEndY,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeroTopVignetteHeight)
                .alpha(0.8f)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Black,
                            0.31928f to Color.Transparent,
                        ),
                        startY = 0f,
                        endY = vignetteEndY,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeroTopVignetteHeight)
                .alpha(0.8f)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.20482f to Color(0xFF151025),
                            0.41566f to Color.Transparent,
                        ),
                        startY = 0f,
                        endY = vignetteEndY,
                    ),
                ),
        )
    }
}

@Composable
private fun HomeTopBar(onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.img_home_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .padding(start = 20.dp, top = 18.dp)
                .size(90.dp, 27.dp),
            contentScale = ContentScale.Fit,
        )
        Image(
            painter = painterResource(R.drawable.ic_game_search),
            contentDescription = stringResource(R.string.cd_game_search),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onSearchClick),
        )
    }
}

@Composable
private fun HeroCopy(onStartGame: () -> Unit) {
    Column(
        modifier = Modifier.padding(start = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.home_last_pick),
            color = Color.White,
            fontSize = 38.sp,
            fontFamily = YoofiDisplaySerif,
            fontWeight = FontWeight.Normal,
        )
        Text(
            text = stringResource(R.string.home_last_pick_sub),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 20.sp,
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(91.dp))
                .background(
                    Brush.verticalGradient(listOf(YoofiStartGameFrom, YoofiStartGameTo)),
                )
                .clickable(role = Role.Button, onClick = onStartGame)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_media_play),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.home_start_game),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun HeroDots(
    count: Int,
    selected: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(10.dp, 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        Color(0xFFD9D9D9).copy(alpha = if (index == selected) 1f else 0.3f),
                    ),
            )
        }
    }
}

@Composable
private fun ListedWorksRow() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GameSectionHeader(
            title = stringResource(R.string.section_listed_works),
            onMoreClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ListedCard(
                coverRes = R.drawable.img_home_listed_1,
                rank = "1",
                rankBrush = Brush.verticalGradient(
                    listOf(Color(0xFFEFB38D), Color(0xFFB77052)),
                ),
                title = stringResource(R.string.card_arranged_marriage),
            )
            ListedCard(
                coverRes = R.drawable.img_home_listed_2,
                rank = "2",
                rankBrush = Brush.verticalGradient(
                    listOf(Color(0xFFBFCCD0), Color(0xFF6B859B)),
                ),
                title = stringResource(R.string.card_arranged_marriage),
            )
            ListedCard(
                coverRes = R.drawable.img_home_listed_3,
                rank = "3",
                rankBrush = Brush.verticalGradient(
                    listOf(Color(0xFFF2BBBB), Color(0xFFC56A6A)),
                ),
                title = stringResource(R.string.card_arranged_short),
            )
        }
    }
}

@Composable
private fun ListedCard(
    coverRes: Int,
    rank: String,
    rankBrush: Brush,
    title: String,
) {
    val (w, h) = ListedCardSize
    Box(
        modifier = Modifier
            .size(w, h)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        Image(
            painter = painterResource(coverRes),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.57105f to Color.Transparent,
                        0.72478f to Color(0x7A110F3A),
                        0.96459f to Color(0x8A1F003F),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp))
                .background(rankBrush),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rank,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            )
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 12.dp, end = 10.dp),
        )
    }
}

@Composable
private fun GameLibraryBlock() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GameSectionHeader(
            title = stringResource(R.string.section_game_library),
            onMoreClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(YoofiTitleGradientEnd.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.chip_game),
                        color = YoofiChipText,
                        fontSize = 14.sp,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.img_home_library_1),
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .height(LibraryCardHeight)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Image(
                painter = painterResource(R.drawable.img_home_library_2),
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .height(LibraryCardHeight)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun HomeExploreScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        HomeExploreScreen()
    }
}
