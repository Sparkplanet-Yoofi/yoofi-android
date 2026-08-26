package ai.yoofi.app.ui.game

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiGameBg0
import ai.yoofi.app.ui.theme.YoofiGameBg1
import ai.yoofi.app.ui.theme.YoofiGameBg2
import ai.yoofi.app.ui.theme.YoofiGameBg3
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GameBgBrush = Brush.verticalGradient(
    0f to YoofiGameBg0,
    0.27168f to YoofiGameBg1,
    0.99038f to YoofiGameBg2,
    1f to YoofiGameBg3,
)

/**
 * Game / World 首页，对齐 Figma `982:14757`。
 * 底栏由外层 [ai.yoofi.app.ui.navigation.YoofiApp] 绘制。
 */
@Composable
fun GameHomeScreen(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onPlayedMore: () -> Unit = {},
    onStoriesMore: () -> Unit = {},
    onPlayedItemClick: (PlayedCover) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBgBrush),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x806579BB), Color.Transparent),
                        radius = 420f,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            GameTopBar(
                onSearchClick = onSearchClick,
                onNotificationClick = onNotificationClick,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, top = 12.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                GameSectionHeader(
                    title = stringResource(R.string.section_played),
                    onMoreClick = onPlayedMore,
                    modifier = Modifier.width(350.dp),
                )
                PlayedCoverFlow(onItemClick = onPlayedItemClick)
                GameSectionHeader(
                    title = stringResource(R.string.section_stories),
                    onMoreClick = onStoriesMore,
                    modifier = Modifier.width(350.dp),
                )
                StoryCollectionRow()
                StoryCollectionRow()
            }
        }
    }
}

@Composable
internal fun GameSectionHeader(
    title: String,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.ic_chevron_up),
            contentDescription = stringResource(R.string.cd_section_more),
            modifier = Modifier
                .size(20.dp)
                .rotate(90f)
                .clickable(role = Role.Button, onClick = onMoreClick),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.width(10.dp))
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF131126)
@Composable
private fun GameHomeScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        GameHomeScreen()
    }
}
