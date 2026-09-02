package ai.yoofi.app.data.item.preview

import ai.yoofi.app.R
import ai.yoofi.app.core.item.preview.ItemPreviewContent
import ai.yoofi.app.ui.theme.YoofiDisplaySerif
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 预览卡面圆角，对齐 `2304:23750`。两个渲染器共用，避免 2D / 3D 切换时圆角不一致 */
internal val ItemPreviewCardShape = RoundedCornerShape(16.dp)

/**
 * 立绘占卡面高度的比例，沿用列表卡 `GameItemCard` 的 178/240，
 * 用比例而非固定 dp，卡面尺寸调整时版式不会散。
 */
private const val ArtHeightFraction = 178f / 240f

// 以下尺寸由列表卡等比放大而来（预览 450dp / 列表 240dp = 1.875），保证两处观感一致
private val FrameInset = 17.dp
private val TextSideInset = 30.dp
private val TextBottomInset = 26.dp
private val DescTopGap = 4.dp
private val NameSize = 26.sp
private val DescSize = 15.sp
private val DescLineHeight = 18.sp
private val DescColor = Color(0xFF4E4217)
private val NameStrokeColor = Color(0xFF2A2410)
private const val NameStrokeWidth = 7f

/**
 * 预览卡面：白底 + 立绘 + 装饰边框 + 信息区，与列表卡 `GameItemCard` 同一套模板。
 *
 * 卡面框架（白底、边框、文字）始终静止，只有立绘区通过 [art] 插槽交给调用方，
 * 3D 环绕就发生在那一块里——转的是道具本身，不是整张卡。
 */
@Composable
internal fun ItemPreviewCardFace(
    content: ItemPreviewContent,
    modifier: Modifier = Modifier,
    art: @Composable (Modifier) -> Unit = { artModifier ->
        StaticItemArt(imageKey = content.imageKey, contentDescription = content.name, modifier = artModifier)
    },
) {
    Box(modifier = modifier.background(Color.White)) {
        art(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(ArtHeightFraction)
                .align(Alignment.TopCenter),
        )
        Image(
            painter = painterResource(R.drawable.img_item_card_frame),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(FrameInset),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = TextSideInset, end = TextSideInset, bottom = TextBottomInset),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 白色字压在白色信息区上，只靠深色描边勾轮廓才看得见（Figma 2304:23750）。
            // Compose 的 Text 一次只能画填充或描边，所以叠两层：先描边后填充。
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = content.name,
                    color = NameStrokeColor,
                    fontSize = NameSize,
                    fontFamily = YoofiDisplaySerif,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        drawStyle = Stroke(width = NameStrokeWidth, join = StrokeJoin.Round),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = content.name,
                    color = Color.White,
                    fontSize = NameSize,
                    fontFamily = YoofiDisplaySerif,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                text = content.description,
                color = DescColor,
                fontSize = DescSize,
                lineHeight = DescLineHeight,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DescTopGap),
            )
        }
    }
}

/** 无环绕素材时的静态立绘，也是降级链的末端 */
@Composable
internal fun StaticItemArt(
    imageKey: String,
    contentDescription: String?,
    modifier: Modifier,
) {
    Image(
        painter = painterResource(itemArtRes(imageKey)),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}
