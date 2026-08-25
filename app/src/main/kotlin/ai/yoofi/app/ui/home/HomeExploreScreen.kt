package ai.yoofi.app.ui.home

import ai.yoofi.app.R
import ai.yoofi.app.ui.game.GameSectionHeader
import ai.yoofi.app.ui.theme.YoofiChipText
import ai.yoofi.app.ui.theme.YoofiDisplaySerif
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import ai.yoofi.app.ui.theme.YoofiTitleGradientEnd
import ai.yoofi.app.ui.theme.YoofiandroidTheme
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val ListedCardSize = 138.dp to 182.dp
private val LibraryCardSize = 173.dp to 154.dp

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
        Image(
            painter = painterResource(R.drawable.img_home_hero),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(518.dp),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.049763f to Color.Black.copy(alpha = 0.5f),
                        0.12085f to Color.Transparent,
                        0.21209f to Color.Transparent,
                        0.36019f to Color(0xFF150B33),
                        0.52488f to Color(0xFF070514),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp),
        ) {
            HomeTopBar(onSearchClick = onSearchClick)
            Spacer(Modifier.height(90.dp))
            HeroCopy(onStartGame = onStartGame)
            Spacer(Modifier.height(16.dp))
            HeroDots(modifier = Modifier.align(Alignment.End).padding(end = 31.dp))
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.padding(start = 20.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                ListedWorksRow()
                GameLibraryBlock()
            }
        }
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
            painter = painterResource(R.drawable.img_home_logo_glow),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 2.dp, y = 0.5.dp)
                .size(125.dp, 58.dp),
            contentScale = ContentScale.FillBounds,
        )
        Image(
            painter = painterResource(R.drawable.img_yoofi_logo),
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
private fun HeroDots(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .size(10.dp, 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFD9D9D9).copy(alpha = if (index == 0) 1f else 0.3f)),
            )
        }
    }
}

@Composable
private fun ListedWorksRow() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GameSectionHeader(
            title = stringResource(R.string.section_listed_works),
            onMoreClick = {},
            modifier = Modifier.width(350.dp),
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GameSectionHeader(
            title = stringResource(R.string.section_game_library),
            onMoreClick = {},
            modifier = Modifier.width(350.dp),
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
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Image(
                painter = painterResource(R.drawable.img_home_library_1),
                contentDescription = null,
                modifier = Modifier
                    .size(LibraryCardSize.first, LibraryCardSize.second)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Image(
                painter = painterResource(R.drawable.img_home_library_2),
                contentDescription = null,
                modifier = Modifier
                    .size(LibraryCardSize.first, LibraryCardSize.second)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun HomeExploreScreenPreview() {
    YoofiandroidTheme(darkTheme = true, dynamicColor = false) {
        HomeExploreScreen()
    }
}
