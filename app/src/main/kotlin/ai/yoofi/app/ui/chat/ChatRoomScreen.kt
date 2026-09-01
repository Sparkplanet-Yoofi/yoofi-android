package ai.yoofi.app.ui.chat

import ai.yoofi.app.R
import ai.yoofi.app.ui.ime.ImeOverlayBox
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.ime.imeAvoidingPadding
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiChatHeaderTop
import ai.yoofi.app.ui.theme.YoofiChatRadialMid
import ai.yoofi.app.ui.theme.YoofiGameBg0
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.max

/**
 * Footer 顶部那条「全透明 → 不透明」的渐变带高度。
 * Figma `1826:9178`：Footer 顶边 654、芯片行顶边 708，中间 54dp 是渐变带，
 * 消息容器 `1826:9183` 一直延伸到 700，即消息本就该钻到这条带子下面被淡出。
 */
private val FooterFadeHeight = 54.dp

/**
 * 回到底部箭头嵌入 Footer 顶部的深度。
 * Figma `1826:9178`：Footer 顶边 654、箭头底边 696，正好压进 42dp（距芯片行 12dp）。
 */
private val JumpArrowFooterOverlap = 42.dp

/** 箭头中心比屏幕中线偏左 7dp（Figma 箭头 x=170、宽 36，中心 188 对 195）。 */
private val JumpArrowCenterOffset = 7.dp

/**
 * 多人聊天室，对齐 Figma `1826:9178`（Cast `1826:9211`、@ `1826:11556`、灵感 `1826:9937`）。
 * Cast / Map 芯片分别跳独立游戏详情页，不进本页 overlay。
 * 键盘弹起对齐 `1826:10061`：顶栏 [ChatRoomHeader] 留在顶部，列表与 Footer 整体抬到键盘上沿；
 * 背景仍铺满。点空白收键盘。不画 iOS 状态栏 / Home Indicator。
 */
@Composable
internal fun ChatRoomScreen(
    onBack: () -> Unit,
    onOpenCast: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChatRoomViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    // 点 Continue 的瞬间用户是否停在底部。必须在列表刷新前取样，
    // 否则等副作用到达时新消息已入列，位置判定必然为「不在底部」。
    var followLatest by remember { mutableStateOf(true) }

    BackHandler {
        if (state.overlay != ChatRoomOverlay.None) {
            viewModel.onIntent(ChatRoomIntent.DismissOverlay)
        } else {
            onBack()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ChatRoomSideEffect.ScrollToBottom -> {
                    // 用户正在回看历史时不抢滚动，交给回到底部箭头提示新消息
                    if (!effect.force && !followLatest) return@collect
                    val last = viewModel.uiState.value.items.lastIndex
                    if (last >= 0) {
                        listState.animateScrollToItem(last)
                    }
                }
            }
        }
    }

    ChatRoomLayout(
        state = state,
        listState = listState,
        onIntent = { intent ->
            if (intent is ChatRoomIntent.OpenCast) {
                onOpenCast()
                return@ChatRoomLayout
            }
            if (intent is ChatRoomIntent.OpenMap) {
                onOpenMap()
                return@ChatRoomLayout
            }
            if (intent is ChatRoomIntent.ContinueStory) {
                followLatest = listState.isScrolledToEnd()
            }
            viewModel.onIntent(intent)
        },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun ChatRoomLayout(
    state: ChatRoomUiState,
    listState: LazyListState,
    onIntent: (ChatRoomIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 回到底部的箭头要压在 Footer 顶部渐变带上，位置随 Footer 实时高度走
    var footerHeightPx by remember { mutableIntStateOf(0) }

    ImeOverlayBox(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.img_chat_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        ChatRoomScrim(modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .background(
                    Brush.verticalGradient(
                        0f to YoofiChatHeaderTop,
                        0.05f to YoofiChatHeaderTop,
                        1f to Color.Transparent,
                    ),
                ),
        )
        // 只垫这一层：背景/遮罩仍铺满屏幕，键盘从底下盖上来。
        // inset = IME ∪ 导航栏，键盘收起后回落到导航栏，Footer 不再自己垫导航栏。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imeAvoidingPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ChatRoomHeader(
                    title = state.chapterTitle,
                    objective = state.chapterObjective,
                    volumeMuted = state.volumeMuted,
                    onExit = onBack,
                    onToggleVolume = { onIntent(ChatRoomIntent.ToggleVolume) },
                    modifier = Modifier.fillMaxWidth(),
                )
                ChatMessageList(
                    items = state.items,
                    listState = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onSceneCharacterClick = { character ->
                        onIntent(ChatRoomIntent.OpenSceneCharacter(character.id))
                    },
                )
                ChatRoomFooter(
                    state = state,
                    onIntent = onIntent,
                    modifier = Modifier
                        .fillMaxWidth()
                        // 渐变带不占列布局高度，改为向上盖住列表底部：
                        // 列表因此多拿 54dp 空间并在芯片行处才裁剪，滚到底的内容被渐变淡出而非硬切
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            val fade = FooterFadeHeight.roundToPx()
                            layout(placeable.width, placeable.height - fade) {
                                placeable.place(0, -fade)
                            }
                        }
                        .onSizeChanged { footerHeightPx = it.height },
                )
            }
            val showJumpArrow = rememberJumpArrowVisible(
                itemCount = state.items.size,
                listState = listState,
            )
            // 画在 Footer 之后，才不会被 Footer 顶部的半透明渐变洗淡
            if (showJumpArrow && footerHeightPx > 0) {
                Image(
                    painter = painterResource(R.drawable.ic_chat_jump_down),
                    contentDescription = stringResource(R.string.cd_chat_jump_latest),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset {
                            IntOffset(
                                x = -JumpArrowCenterOffset.roundToPx(),
                                y = JumpArrowFooterOverlap.roundToPx() - footerHeightPx,
                            )
                        }
                        .size(36.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.15f),
                            spotColor = Color.Black.copy(alpha = 0.15f),
                        )
                        .clickableDismissingIme {
                            onIntent(ChatRoomIntent.JumpToLatest)
                        },
                )
            }
        }
        if (state.overlay == ChatRoomOverlay.Cast) {
            ChatCastOverlay(
                onDismiss = { onIntent(ChatRoomIntent.DismissOverlay) },
            )
        }
    }
}

/**
 * 回到底部箭头的显示时机：列表追加了新消息、且用户当时没停在底部才亮起；
 * 一旦滚到底部立即复位，避免箭头常驻。首次进入不算「新消息」。
 */
@Composable
private fun rememberJumpArrowVisible(
    itemCount: Int,
    listState: LazyListState,
): Boolean {
    val atBottom by remember(listState) {
        derivedStateOf { listState.isScrolledToEnd() }
    }
    var seenCount by remember { mutableIntStateOf(itemCount) }
    var pendingNew by remember { mutableStateOf(false) }

    LaunchedEffect(itemCount) {
        if (itemCount > seenCount && !atBottom) {
            pendingNew = true
        }
        seenCount = itemCount
    }
    LaunchedEffect(atBottom) {
        if (atBottom) {
            pendingNew = false
        }
    }
    return pendingNew && !atBottom
}

/** 最后一条完全落在视口内（含底部 contentPadding）即视为已在底部。 */
private fun LazyListState.isScrolledToEnd(): Boolean {
    val info = layoutInfo
    val last = info.visibleItemsInfo.lastOrNull() ?: return true
    return last.index == info.totalItemsCount - 1 &&
        last.offset + last.size <= info.viewportEndOffset - info.afterContentPadding
}

@Composable
private fun ChatRoomScrim(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val radius = max(constraints.maxWidth, constraints.maxHeight).toFloat()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.8f }
                .background(
                    Brush.radialGradient(
                        0f to Color(0x1A000000),
                        0.5f to YoofiChatRadialMid,
                        1f to YoofiGameBg0,
                        center = Offset(
                            constraints.maxWidth / 2f,
                            constraints.maxHeight / 2f,
                        ),
                        radius = radius,
                    ),
                ),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF131126)
@Composable
private fun ChatRoomLayoutPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        ChatRoomLayout(
            state = ChatRoomUiState(
                chapterTitle = "Chapter Title",
                chapterObjective = "Chapter Objective",
            ),
            listState = rememberLazyListState(),
            onIntent = {},
            onBack = {},
        )
    }
}
