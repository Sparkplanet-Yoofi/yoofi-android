package ai.yoofi.app.ui.game

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RowWidth = 350.dp
private val RowHeight = 200.dp
private val CoverW = 101.dp
private val CoverH = 134.dp
private val CoverGap = 10.dp
private val CoverStart = 14.dp
private val FrostH = 46.dp
private val FrostTop = 99.dp

private val FrostBrush = Brush.verticalGradient(
    0f to Color(0x80A0A0A0),
    0.56731f to Color(0x4D959398),
    1f to Color(0x006E5B84),
)

/**
 * Stories 一行三列收藏卡，对齐 `982:14821`。
 */
@Composable
fun StoryCollectionRow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(RowWidth)
            .height(RowHeight),
    ) {
        Row(
            modifier = Modifier.padding(start = CoverStart),
        ) {
            repeat(3) { index ->
                if (index > 0) Spacer(Modifier.width(CoverGap))
                StoryCollectionItem()
            }
        }
        Box(
            modifier = Modifier
                .offset(y = FrostTop)
                .size(RowWidth, FrostH)
                .clip(RoundedCornerShape(6.dp))
                .background(FrostBrush)
                .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
        ) {
            CornerDot(Modifier.offset(x = 6.dp, y = 6.dp))
            CornerDot(Modifier.offset(x = 6.dp, y = 30.dp))
            CornerDot(Modifier.offset(x = 338.dp, y = 6.dp))
            CornerDot(Modifier.offset(x = 338.dp, y = 30.dp))
        }
    }
}

/** 封面与文案绑成一列，避免文案脱离卡片被兄弟列盖住。 */
@Composable
private fun StoryCollectionItem() {
    Column(
        modifier = Modifier.width(CoverW),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.img_game_cover_d),
            contentDescription = null,
            modifier = Modifier
                .size(CoverW, CoverH)
                .shadow(4.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.collection_name),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.collection_items),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CornerDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(listOf(Color.White, Color(0xFFD9D9D9))),
            ),
    )
}

@Preview(widthDp = 350, heightDp = 184, showBackground = true, backgroundColor = 0xFF131126)
@Composable
private fun StoryCollectionRowPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        StoryCollectionRow()
    }
}
