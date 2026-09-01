package ai.yoofi.app.ui.gamedetail.cast

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameCastCard
import ai.yoofi.app.domain.gamedetail.GameCastRole
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiDisplaySerif
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val PageBg = Color(0xFF23212B)
private val CardGoldFrom = Color.White
private val CardGoldTo = Color(0xFFD6C9AD)
private val RolePillFill = Color(0x8031221B)
private val CardShape = RoundedCornerShape(16.dp)
private val CardHeight = 240.dp

/**
 * 游戏详情人物页，对齐 Figma `2304:23753`。
 * 聊天室 Cast 芯片跳这里；金卡再进角色详情 `2409:27067`。
 * 返回 / 关闭都回到聊天室，不改翻牌 overlay。
 */
@Composable
internal fun GameCastScreen(
    onBack: () -> Unit,
    onClose: () -> Unit,
    onOpenCharacter: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GameCastViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GameCastLayout(
        cards = state.cards,
        onBack = onBack,
        onClose = onClose,
        onOpenCharacter = onOpenCharacter,
        modifier = modifier,
    )
}

@Composable
internal fun GameCastLayout(
    cards: List<GameCastCard>,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onOpenCharacter: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        GameCastTopBar(onBack = onBack, onClose = onClose)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.12f)),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        ) {
            items(cards, key = { it.id }) { card ->
                GameCastCardItem(
                    card = card,
                    onClick = { onOpenCharacter(card.id) },
                )
            }
        }
    }
}

@Composable
private fun GameCastTopBar(
    onBack: () -> Unit,
    onClose: () -> Unit,
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
            text = stringResource(R.string.chat_cast),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center),
        )
        Image(
            painter = painterResource(R.drawable.ic_cast_close),
            contentDescription = stringResource(R.string.cd_chat_close),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onClose),
        )
    }
}

@Composable
private fun GameCastCardItem(
    card: GameCastCard,
    onClick: () -> Unit,
) {
    val portraitKey = card.portraitKey
    val name = card.name
    val role = card.role
    if (portraitKey == null || name == null || role == null) {
        Image(
            painter = painterResource(R.drawable.img_cast_slot_empty),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(CardHeight)
                .clip(CardShape),
            contentScale = ContentScale.Crop,
        )
        return
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CardHeight)
            .clip(CardShape)
            .background(
                Brush.linearGradient(listOf(CardGoldFrom, CardGoldTo)),
            )
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Image(
            painter = painterResource(castPortraitRes(portraitKey)),
            contentDescription = name,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 7.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )
        Image(
            painter = painterResource(R.drawable.img_cast_card_frame),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 8.dp),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 29.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = name.uppercase(),
                color = Color.White,
                fontSize = 21.sp,
                fontFamily = YoofiDisplaySerif,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(castRoleLabelRes(role)),
                color = Color.White,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(RolePillFill)
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
    }
}

@DrawableRes
private fun castPortraitRes(portraitKey: String): Int = when (portraitKey) {
    "sunnme-a" -> R.drawable.img_cast_portrait_sunnme_a
    "tomy-a" -> R.drawable.img_cast_portrait_tomy_a
    "sunnme-b" -> R.drawable.img_cast_portrait_sunnme_b
    "tomy-b" -> R.drawable.img_cast_portrait_tomy_b
    else -> R.drawable.img_cast_portrait_sunnme_a
}

@StringRes
private fun castRoleLabelRes(role: GameCastRole): Int = when (role) {
    GameCastRole.Me -> R.string.cast_role_me
    GameCastRole.PlayerRole -> R.string.cast_role_player
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF1C1528)
@Composable
private fun GameCastPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        GameCastLayout(
            cards = listOf(
                GameCastCard("a", "sunnme", GameCastRole.Me, "sunnme-a"),
                GameCastCard("b", "TOMY", GameCastRole.PlayerRole, "tomy-a"),
                GameCastCard("c", "sunnme", GameCastRole.PlayerRole, "sunnme-b"),
                GameCastCard("d", "TOMY", GameCastRole.PlayerRole, "tomy-b"),
                GameCastCard("e", null, null, null),
                GameCastCard("f", null, null, null),
            ),
            onBack = {},
            onClose = {},
        )
    }
}
