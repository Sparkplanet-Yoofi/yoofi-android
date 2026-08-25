package ai.yoofi.app.ui.game

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiandroidTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CoverFlowWidth = 350.dp
private val CoverFlowHeight = 182.dp
private val CardRadius = 20.dp

private val OverlayBrush = Brush.verticalGradient(
    0.57105f to Color.Transparent,
    0.72478f to Color(0x7A110F3A),
    0.96459f to Color(0x8A1F003F),
)

/**
 * Played 层叠封面，像素对齐 Figma `982:14810`。
 */
@Composable
fun PlayedCoverFlow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(CoverFlowWidth)
            .height(CoverFlowHeight),
    ) {
        PlayedCard(
            coverRes = R.drawable.img_game_cover_a,
            width = 106.154.dp,
            height = 140.dp,
            titleSize = 9.23.sp,
            modifier = Modifier.offset(x = 0.dp, y = 21.dp),
        )
        PlayedCard(
            coverRes = R.drawable.img_game_cover_c,
            width = 106.154.dp,
            height = 140.dp,
            titleSize = 9.23.sp,
            modifier = Modifier.offset(x = 244.dp, y = 21.dp),
        )
        PlayedCard(
            coverRes = R.drawable.img_game_cover_a,
            width = 121.319.dp,
            height = 160.dp,
            titleSize = 10.55.sp,
            modifier = Modifier.offset(x = 50.dp, y = 11.dp),
        )
        PlayedCard(
            coverRes = R.drawable.img_game_cover_d,
            width = 121.319.dp,
            height = 160.dp,
            titleSize = 10.55.sp,
            modifier = Modifier.offset(x = 179.dp, y = 11.dp),
        )
        PlayedCard(
            coverRes = R.drawable.img_game_cover_e,
            width = 138.dp,
            height = 182.dp,
            titleSize = 12.sp,
            elevated = true,
            modifier = Modifier.offset(x = 106.dp, y = 0.dp),
        )
    }
}

@Composable
private fun PlayedCard(
    coverRes: Int,
    width: Dp,
    height: Dp,
    titleSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
) {
    val shape = RoundedCornerShape(CardRadius)
    Box(
        modifier = modifier
            .then(
                if (elevated) {
                    Modifier.shadow(4.dp, shape, ambientColor = Color.Black.copy(0.25f))
                } else {
                    Modifier
                },
            )
            .size(width, height)
            .clip(shape),
    ) {
        Image(
            painter = painterResource(coverRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OverlayBrush),
        )
        Text(
            text = stringResource(R.string.card_arranged_marriage),
            color = Color.White,
            fontSize = titleSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomStart)
                .offset(x = 10.dp, y = (-12).dp)
                .width(width - 20.dp),
        )
    }
}

@Preview(widthDp = 351, heightDp = 182, showBackground = true, backgroundColor = 0xFF131126)
@Composable
private fun PlayedCoverFlowPreview() {
    YoofiandroidTheme(darkTheme = true, dynamicColor = false) {
        PlayedCoverFlow()
    }
}
