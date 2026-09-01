package ai.yoofi.app.ui.gamedetail.item

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameItem
import ai.yoofi.app.domain.gamedetail.GameItemTarget
import ai.yoofi.app.ui.chat.chatAvatarRes
import ai.yoofi.app.ui.gamedetail.DetailActionBrush
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiStartGameTo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SelectAllFill = Color(0x3352466C)
private val SelectAllStroke = Color(0x4D746396)
private val SelectAllText = Color(0xFFA5A4B8)
private val IdentityFill = Color(0xFF3A3745)
private val NameColor = Color(0xFFE1E1E1)
private val DisabledHint = Color(0xFF646464)
private val RemainingBrush = Brush.horizontalGradient(
    listOf(Color(0xFF5257FF), Color(0xFF906AEF)),
)
private val PillShape = RoundedCornerShape(100.dp)

/**
 * 多人道具选人页。
 * 未选 `2304:24760`，部分选 `2304:24649`，全选 `2304:24871`。
 */
@Composable
internal fun GameItemTargetLayout(
    item: GameItem,
    targets: List<GameItemTarget>,
    selectedIds: Set<String>,
    allSelected: Boolean,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onUse: () -> Unit,
) {
    val hasSelection = selectedIds.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ItemPageBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        GameItemTargetTopBar(
            allSelected = allSelected,
            onBack = onBack,
            onSelectAll = onSelectAll,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.12f)),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
        ) {
            items(targets, key = { it.id }) { target ->
                GameItemTargetRow(
                    target = target,
                    selected = target.id in selectedIds,
                    onToggle = { onToggle(target.id) },
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val useLabel = if (allSelected) {
                stringResource(R.string.item_use_item)
            } else {
                stringResource(R.string.item_use)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .alpha(if (hasSelection) 1f else 0.5f)
                    .clip(PillShape)
                    .background(DetailActionBrush)
                    .then(
                        if (hasSelection) {
                            Modifier.clickable(role = Role.Button, onClick = onUse)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = useLabel,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            val remaining = if (hasSelection) {
                stringResource(
                    R.string.item_remaining,
                    item.remainingCards,
                    item.remainingUses,
                )
            } else {
                stringResource(R.string.item_remaining, 0, 0)
            }
            Text(
                text = remaining,
                style = if (hasSelection) {
                    TextStyle(
                        brush = RemainingBrush,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    TextStyle(
                        color = DisabledHint,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun GameItemTargetTopBar(
    allSelected: Boolean,
    onBack: () -> Unit,
    onSelectAll: () -> Unit,
) {
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
            text = stringResource(R.string.item_select_character),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center),
        )
        val selectAllBg = if (allSelected) {
            Modifier.background(
                Brush.verticalGradient(listOf(YoofiStartGameFrom, YoofiStartGameTo)),
                PillShape,
            )
        } else {
            Modifier.background(SelectAllFill, PillShape)
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 85.dp, height = 36.dp)
                .then(selectAllBg)
                .border(1.dp, SelectAllStroke, PillShape)
                .clickable(role = Role.Button, onClick = onSelectAll),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.item_select_all),
                color = if (allSelected) Color.White else SelectAllText,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun GameItemTargetRow(
    target: GameItemTarget,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(chatAvatarRes(target.avatarKey)),
                contentDescription = target.displayName,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = target.displayName,
                    color = NameColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (target.identity.isNotBlank()) {
                    Text(
                        text = target.identity,
                        color = NameColor,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(PillShape)
                            .background(IdentityFill)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(1.5.dp, SelectAllStroke, CircleShape)
                    .background(SelectAllFill, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(YoofiAccent, CircleShape),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.08f)),
        )
    }
}
