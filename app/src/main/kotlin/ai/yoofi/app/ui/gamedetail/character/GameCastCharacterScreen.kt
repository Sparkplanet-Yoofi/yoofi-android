package ai.yoofi.app.ui.gamedetail.character

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameCastCharacter
import ai.yoofi.app.domain.gamedetail.GameCastCharacterTab
import ai.yoofi.app.ui.gamedetail.DetailActionBrush
import ai.yoofi.app.ui.gamedetail.DetailSynopsis
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiChipText
import ai.yoofi.app.ui.theme.YoofiDetailFieldFill
import ai.yoofi.app.ui.theme.YoofiGenderSelected
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val PageBg = Color(0xFF1C1528)
private val HeroHeight = 585.dp
private val FollowPillShape = RoundedCornerShape(100.dp)
private val ChipShape = RoundedCornerShape(8.dp)
private val ChipFill = Color(0x1A7C5CFC)
private val ChipSelectedStroke = Color(0x4D7C5CFC)
private val PlaceholderFill = Color(0x4D2A2444)
private val ActionHeight = 46.dp
private val ActionShape = RoundedCornerShape(100.dp)
private val SaveShape = RoundedCornerShape(12.dp)

/**
 * 角色详情页，对齐 Figma `2409:27067`。
 * Cast 金卡跳这里；关闭回到人物页，Continue Game 回到聊天室。
 */
@Composable
internal fun GameCastCharacterScreen(
    characterId: String,
    onClose: () -> Unit,
    onContinueGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameCastCharacterViewModel = hiltViewModel(),
) {
    LaunchedEffect(characterId) { viewModel.load(characterId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GameCastCharacterLayout(
        state = state,
        onIntent = viewModel::onIntent,
        onClose = onClose,
        onContinueGame = onContinueGame,
        modifier = modifier,
    )
}

@Composable
internal fun GameCastCharacterLayout(
    state: GameCastCharacterUiState,
    onIntent: (GameCastCharacterIntent) -> Unit,
    onClose: () -> Unit,
    onContinueGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    val character = state.character
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBg),
    ) {
        CharacterHero(heroKey = character?.heroKey)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            CharacterTopBar(onClose = onClose)
            Spacer(Modifier.height(260.dp))
            if (character != null) {
                CharacterTitleRow(
                    title = character.title,
                    following = character.following,
                    onToggleFollow = { onIntent(GameCastCharacterIntent.ToggleFollow) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                CharacterTabRow(
                    selected = character.tab,
                    onSelect = { onIntent(GameCastCharacterIntent.SelectTab(it)) },
                    modifier = Modifier.padding(start = 20.dp, top = 8.dp),
                )
                DetailSynopsis(
                    title = character.synopsisTitle,
                    body = character.synopsis,
                    expanded = state.synopsisExpanded,
                    onToggle = { onIntent(GameCastCharacterIntent.ToggleSynopsis) },
                    modifier = Modifier.padding(top = 20.dp),
                )
                Box(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 22.dp, top = 20.dp)
                        .fillMaxWidth()
                        .height(101.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PlaceholderFill),
                )
            }
            Spacer(Modifier.weight(1f))
            CharacterBottomBar(
                favorited = character?.favorited == true,
                onContinueGame = onContinueGame,
                onToggleFavorite = { onIntent(GameCastCharacterIntent.ToggleFavorite) },
            )
        }
    }
}

@Composable
private fun CharacterHero(heroKey: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeroHeight),
    ) {
        Image(
            painter = painterResource(characterHeroRes(heroKey)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.486f to Color.Transparent,
                        0.687f to PageBg,
                        1f to PageBg,
                    ),
                ),
        )
    }
}

@Composable
private fun CharacterTopBar(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 20.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_cast_close),
            contentDescription = stringResource(R.string.cd_chat_close),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp)
                .clickableDismissingIme(onClick = onClose),
        )
    }
}

@Composable
private fun CharacterTitleRow(
    title: String,
    following: Boolean,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 36.sp,
            modifier = Modifier.weight(1f, fill = false),
        )
        Box(
            modifier = Modifier
                .clip(FollowPillShape)
                .background(DetailActionBrush)
                .clickableDismissingIme(
                    role = Role.Checkbox,
                    onClick = onToggleFollow,
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = stringResource(
                    if (following) R.string.detail_following else R.string.detail_follow,
                ),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CharacterTabRow(
    selected: GameCastCharacterTab,
    onSelect: (GameCastCharacterTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GameCastCharacterTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Text(
                text = stringResource(tab.labelRes()),
                color = if (isSelected) YoofiGenderSelected else YoofiChipText,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(ChipShape)
                    .background(ChipFill)
                    .then(
                        if (isSelected) {
                            Modifier.border(1.dp, ChipSelectedStroke, ChipShape)
                        } else {
                            Modifier
                        },
                    )
                    .clickableDismissingIme(onClick = { onSelect(tab) })
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun CharacterBottomBar(
    favorited: Boolean,
    onContinueGame: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PageBg)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(ActionHeight)
                .clip(ActionShape)
                .background(DetailActionBrush)
                .clickableDismissingIme(onClick = onContinueGame),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_detail_play),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.detail_continue_game),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(ActionHeight)
                .clip(SaveShape)
                .background(YoofiDetailFieldFill)
                .clickableDismissingIme(
                    role = Role.Checkbox,
                    onClick = onToggleFavorite,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_character_star),
                contentDescription = stringResource(R.string.cd_character_favorite),
                modifier = Modifier.size(24.dp),
                alpha = if (favorited) 1f else 0.8f,
            )
        }
    }
}

@DrawableRes
private fun characterHeroRes(heroKey: String?): Int = when (heroKey) {
    "forbidden-hero" -> R.drawable.img_character_hero
    else -> R.drawable.img_character_hero
}

@StringRes
private fun GameCastCharacterTab.labelRes(): Int = when (this) {
    GameCastCharacterTab.All -> R.string.character_tab_all
    GameCastCharacterTab.MyCreations -> R.string.character_tab_creations
    GameCastCharacterTab.Collections -> R.string.character_tab_collections
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF1C1528)
@Composable
private fun GameCastCharacterPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        GameCastCharacterLayout(
            state = GameCastCharacterUiState(
                character = GameCastCharacter(
                    id = "demo",
                    title = "Forbidden Game",
                    following = false,
                    tab = GameCastCharacterTab.MyCreations,
                    synopsisTitle = "Forbidden Game：",
                    synopsis = "The metal floor of the capsule rises beneath you.",
                    heroKey = "forbidden-hero",
                    favorited = false,
                ),
            ),
            onIntent = {},
            onClose = {},
            onContinueGame = {},
        )
    }
}
