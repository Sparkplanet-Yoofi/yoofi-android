package ai.yoofi.app.ui.gamedetail.item

import ai.yoofi.app.R
import ai.yoofi.app.core.item.preview.ItemPreviewContent
import ai.yoofi.app.core.item.preview.ItemPreviewHost
import ai.yoofi.app.core.item.preview.ItemPreviewHostRenderer
import ai.yoofi.app.data.item.preview.itemArtRes
import ai.yoofi.app.domain.gamedetail.GameItem
import ai.yoofi.app.domain.gamedetail.GameItemKind
import ai.yoofi.app.ui.gamedetail.DetailActionBrush
import ai.yoofi.app.ui.theme.YoofiDialogScrim
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SheetFill = Color(0xCC23212B)
private val SheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
private val ThumbShape = RoundedCornerShape(8.dp)
private val Badge3dFill = Color(0x99484848)
private val PreviewCloseFill = Color(0x4DFFFFFF)
private val GeneralFill = Color(0x4D5C95FF)
private val GeneralText = Color(0xFF8FC0FF)
private val PillShape = RoundedCornerShape(100.dp)

/**
 * 点任意道具都走这张底栏，对齐多人稿 `2304:24509`。
 * [GameItemKind.General] 只决定 General 徽章显隐，不另开一套按钮。
 */
@Composable
internal fun GameItemUseSheet(
    item: GameItem,
    onPreview: () -> Unit,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val general = item.kind == GameItemKind.General
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SheetShape)
            .background(SheetFill)
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GameItemThumb(
                    imageKey = item.imageKey,
                    onPreview = onPreview,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = item.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (general) {
                            Text(
                                text = stringResource(R.string.item_general),
                                color = GeneralText,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeneralFill)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Text(
                        text = item.description,
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }
            GameItemUsageBlock(
                title = stringResource(R.string.item_usage_scope),
                body = item.usageScope,
            )
            GameItemUsageBlock(
                title = stringResource(R.string.item_usage_rules),
                body = item.usageRules,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ItemPageBg)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(PillShape)
                    .background(DetailActionBrush)
                    .clickable(role = Role.Button, onClick = onPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.item_select_target),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun GameItemThumb(
    imageKey: String,
    onPreview: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(ThumbShape)
            .clickable(role = Role.Button, onClick = onPreview),
    ) {
        Image(
            painter = painterResource(itemArtRes(imageKey)),
            contentDescription = stringResource(R.string.cd_item_preview),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomStart = 12.dp))
                .background(Badge3dFill),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_item_3d),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_item_play),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp),
        )
    }
}

@Composable
private fun GameItemUsageBlock(
    title: String,
    body: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = body,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            textAlign = TextAlign.Justify,
        )
    }
}

/**
 * 道具大图预览：80% 黑遮罩 + 卡面宿主，关闭钮对齐 `2304:23749`。
 * 白底 `2464:27742` 已交给渲染器自己画——3D 模式下白底要跟着卡面一起转，
 * 留在这里的话卡面转了白底不转会露馅。
 */
@Composable
internal fun GameItemPreviewOverlay(
    content: ItemPreviewContent,
    renderer: ItemPreviewHostRenderer,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YoofiDialogScrim),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 300.dp, height = 450.dp),
        ) {
            ItemPreviewHost(
                content = content,
                renderer = renderer,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // 关闭钮：半透明圆底 + Close_MD 白叉。
        // 原先直接铺 ic_item_preview_close.png，那张图导错了图层（紫底下箭头，是「收起」不是「关闭」）。
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 148.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(PreviewCloseFill)
                .clickable(role = Role.Button, onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_cast_close),
                contentDescription = stringResource(R.string.cd_item_preview_close),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
