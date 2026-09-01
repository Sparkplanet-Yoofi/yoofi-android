package ai.yoofi.app.ui.gamedetail.map

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameMap
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/**
 * 游戏详情地图页，对齐 Figma `2453:27236`。
 * 聊天室 Map 芯片跳这里；返回 / 关闭都回到聊天室。
 */
@Composable
internal fun GameMapScreen(
    onBack: () -> Unit,
    onClose: () -> Unit,
    onGoToLocation: (GameMapGoResult) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameMapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // 每次组合都清掉 Go。不能用 viewModel 当 key：同一 Activity VM 再进不会重跑。
    LaunchedEffect(Unit) {
        viewModel.onIntent(GameMapIntent.ShowMap)
    }
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is GameMapSideEffect.GoToChat -> onGoToLocation(
                    GameMapGoResult(effect.text, effect.backgroundKey),
                )
            }
        }
    }
    GameMapLayout(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onClose = onClose,
        modifier = modifier,
    )
}

@Composable
internal fun GameMapLayout(
    state: GameMapUiState,
    onIntent: (GameMapIntent) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        when {
            state.loading -> onIntent(GameMapIntent.CancelLoading)
            state.selectedLocationId.isNotEmpty() -> onIntent(GameMapIntent.DismissGo)
            state.listOpen -> onIntent(GameMapIntent.DismissList)
            else -> onBack()
        }
    }
    val current = state.currentMap
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBg),
    ) {
        if (current != null) {
            GameMapCanvas(
                map = current,
                selectedLocationId = state.selectedLocationId,
                onSelectLocation = { onIntent(GameMapIntent.SelectLocation(it)) },
                onDismissGo = { onIntent(GameMapIntent.DismissGo) },
                onConfirmGo = { onIntent(GameMapIntent.ConfirmGo) },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            GameMapTopBar(onBack = onBack, onClose = onClose)
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.listOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { onIntent(GameMapIntent.DismissList) },
                            ),
                    )
                }
                if (current != null) {
                    GameMapSwitcher(
                        maps = state.maps,
                        currentMap = current,
                        listOpen = state.listOpen,
                        onToggle = { onIntent(GameMapIntent.ToggleList) },
                        onSelect = { onIntent(GameMapIntent.SelectMap(it)) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 16.dp),
                    )
                }
            }
        }
        if (state.loading) {
            GameMapLoadingDialog(
                progress = state.loadingProgress,
                onCancel = { onIntent(GameMapIntent.CancelLoading) },
            )
        }
    }
}

@Composable
private fun GameMapTopBar(
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
            text = stringResource(R.string.chat_map),
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

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF1C1528)
@Composable
private fun GameMapPreview() {
    val maps = listOf(
        GameMap(
            id = "map-01",
            title = "Map 01",
            imageKey = "demo-world",
            startOffsetX = -242f / 916f,
            startOffsetY = 12f / 916f,
            locations = emptyList(),
        ),
    )
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        GameMapLayout(
            state = GameMapUiState(maps = maps, currentMapId = "map-01"),
            onIntent = {},
            onBack = {},
            onClose = {},
        )
    }
}
