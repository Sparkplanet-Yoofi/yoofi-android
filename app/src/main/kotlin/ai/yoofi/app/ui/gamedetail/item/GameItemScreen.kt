package ai.yoofi.app.ui.gamedetail.item

import ai.yoofi.app.R
import ai.yoofi.app.core.item.preview.ItemPreviewContent
import ai.yoofi.app.core.item.preview.ItemPreviewHostRenderer
import ai.yoofi.app.data.item.preview.itemArtRes
import ai.yoofi.app.di.itemPreviewHostRenderer
import ai.yoofi.app.domain.gamedetail.GameItem
import ai.yoofi.app.domain.gamedetail.GameItemKind
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiAuthError
import ai.yoofi.app.ui.theme.YoofiDisplaySerif
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

internal val ItemPageBg = Color(0xFF23212B)
private val CardShape = RoundedCornerShape(16.dp)
private val SelectedShape = RoundedCornerShape(12.dp)
private val CardHeight = 240.dp
private val CardDesc = Color(0xFF4E4217)

/**
 * 游戏详情道具页，对齐 Figma `2304:24267`。
 * 聊天室 Items 芯片跳这里；弹层 / 预览 / 选人都不进 ChatRoomViewModel。
 */
@Composable
internal fun GameItemScreen(
    onBack: () -> Unit,
    onClose: () -> Unit,
    onUseItem: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameItemViewModel = hiltViewModel(),
    previewRenderer: ItemPreviewHostRenderer = itemPreviewHostRenderer(LocalContext.current),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // 每次组合都回列表。不能用 viewModel 当 key：同一 Activity VM 再进不会重跑。
    LaunchedEffect(Unit) {
        viewModel.onIntent(GameItemIntent.ShowList)
    }
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is GameItemSideEffect.SendToChat -> onUseItem(effect.text)
            }
        }
    }
    GameItemLayout(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onClose = onClose,
        previewRenderer = previewRenderer,
        modifier = modifier,
    )
}

@Composable
internal fun GameItemLayout(
    state: GameItemUiState,
    onIntent: (GameItemIntent) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    previewRenderer: ItemPreviewHostRenderer,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        when {
            state.previewOpen -> onIntent(GameItemIntent.ClosePreview)
            state.targetOpen -> onIntent(GameItemIntent.CloseTargets)
            state.sheetOpen -> onIntent(GameItemIntent.DismissSheet)
            else -> onBack()
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ItemPageBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            GameItemTopBar(onBack = onBack, onClose = onClose)
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = if (state.sheetOpen) 320.dp else 24.dp,
                ),
            ) {
                items(state.items, key = { it.id }) { item ->
                    GameItemCard(
                        item = item,
                        selected = state.sheetOpen && item.id == state.selectedItemId,
                        onClick = { onIntent(GameItemIntent.OpenItem(item.id)) },
                    )
                }
            }
        }
        val selected = state.selectedItem
        if (state.sheetOpen && selected != null && !state.targetOpen) {
            GameItemUseSheet(
                item = selected,
                onPreview = { onIntent(GameItemIntent.OpenPreview) },
                onPrimary = { onIntent(GameItemIntent.OpenTargets) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        if (state.previewOpen && selected != null) {
            GameItemPreviewOverlay(
                content = ItemPreviewContent(
                    imageKey = selected.imageKey,
                    name = selected.name,
                    description = selected.cardDescription,
                ),
                renderer = previewRenderer,
                onClose = { onIntent(GameItemIntent.ClosePreview) },
            )
        }
        if (state.targetOpen && selected != null) {
            GameItemTargetLayout(
                item = selected,
                targets = state.targets,
                selectedIds = state.selectedTargetIds,
                allSelected = state.allTargetsSelected,
                onBack = { onIntent(GameItemIntent.CloseTargets) },
                onToggle = { onIntent(GameItemIntent.ToggleTarget(it)) },
                onSelectAll = { onIntent(GameItemIntent.ToggleSelectAll) },
                onUse = { onIntent(GameItemIntent.ConfirmUse) },
            )
        }
    }
}

@Composable
private fun GameItemTopBar(
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
            text = stringResource(R.string.chat_items),
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
private fun GameItemCard(
    item: GameItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CardHeight)
            .then(
                if (selected) {
                    Modifier.border(2.dp, YoofiAccent, SelectedShape)
                } else {
                    Modifier
                },
            )
            .padding(if (selected) 4.dp else 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CardShape)
                .background(Color.White)
                .clickable(role = Role.Button, onClick = onClick),
        ) {
            Image(
                painter = painterResource(itemArtRes(item.imageKey)),
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(178.dp)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Crop,
            )
            Image(
                painter = painterResource(R.drawable.img_item_card_frame),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 9.dp, vertical = 9.dp),
                contentScale = ContentScale.FillBounds,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 14.15.sp,
                    fontFamily = YoofiDisplaySerif,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = item.cardDescription,
                    color = CardDesc,
                    fontSize = 8.26.sp,
                    lineHeight = 9.43.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                )
            }
        }
        if (item.quantity > 0) {
            Text(
                text = stringResource(R.string.item_qty, item.quantity),
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = YoofiDisplaySerif,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-6).dp)
                    .clip(RoundedCornerShape(68.dp))
                    .background(YoofiAuthError)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF23212B)
@Composable
private fun GameItemListPreview() {
    val renderer = remember {
        object : ItemPreviewHostRenderer {
            @Composable
            override fun Render(content: ItemPreviewContent, modifier: Modifier) = Unit
        }
    }
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        GameItemLayout(
            state = GameItemUiState(
                items = listOf(
                    GameItem(
                        id = "a",
                        name = "Name",
                        cardDescription = "Add a description",
                        description = "",
                        imageKey = "knife",
                        quantity = 99,
                        remainingCards = 8,
                        remainingUses = 39,
                        usageScope = "",
                        usageRules = "",
                        kind = GameItemKind.General,
                    ),
                ),
            ),
            onIntent = {},
            onBack = {},
            onClose = {},
            previewRenderer = renderer,
        )
    }
}
