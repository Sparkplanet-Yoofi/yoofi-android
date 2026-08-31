package ai.yoofi.app.ui.gamedetail

import ai.yoofi.app.R
import ai.yoofi.app.domain.gamedetail.GameAuthor
import ai.yoofi.app.domain.gamedetail.GameComment
import ai.yoofi.app.domain.gamedetail.GameDetail
import ai.yoofi.app.ui.ime.ImeOverlayBox
import ai.yoofi.app.ui.gamedetail.report.ReportContentScreen
import ai.yoofi.app.ui.gamedetail.report.ReportResetSheet
import ai.yoofi.app.ui.gamedetail.report.toReportTarget
import ai.yoofi.app.ui.profile.GuestProfileTarget
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiSnackbarContainer
import ai.yoofi.app.ui.theme.YoofiSnackbarContent
import ai.yoofi.app.ui.theme.YoofiDetailActionFrom
import ai.yoofi.app.ui.theme.YoofiDetailActionTo
import ai.yoofi.app.ui.theme.YoofiDetailBackground
import ai.yoofi.app.ui.theme.YoofiDetailFieldFill
import ai.yoofi.app.ui.theme.YoofiDetailSheet
import ai.yoofi.app.ui.theme.YoofiDisplaySerif
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Figma `1943:13411`：头图 390×585，跟随内容一起滚，不做吸顶。 */
private val HeroHeight = 585.dp

/** Figma `1943:13416`：内容卡顶边 323、上圆角 30、底色半透明并对背景做 17.5 模糊。 */
private val SheetTop = 323.dp
private val SheetShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)

/**
 * 设计稿写的是 CSS `backdrop-blur(17.5px)`，那个数是高斯标准差；
 * [Modifier.blur] 收的是模糊半径，按 `sigma ≈ 0.577 * radius + 0.5` 折算回去约 29.4。
 * 直接照抄 17.5 会明显比设计稿清晰。
 */
private val SheetBlurRadius = 29.dp

/**
 * Figma `1943:13414`：遮罩整屏 844 高，51.896% 处开始由透明转黑、66.327% 处全黑。
 * 这里把两个断点折算成头图自身高度上的比例，遮罩就能跟着头图走而不依赖屏幕高度。
 */
private const val ScrimTransparentStop = 438f / 585f
private const val ScrimOpaqueStop = 560f / 585f

/** Figma `662:2144`：返回行 60 高，左右各 20。 */
private val TopBarHeight = 60.dp

/** Figma `1943:13457` 等：正文左右安全边距。 */
internal val DetailPagePadding = 20.dp

/** Figma `1943:13432`：标题顶边 359，减去内容卡顶边 323。 */
private val TitleTop = 36.dp

/**
 * Figma `1943:13432`：设计稿把「forbidden world」拆成两行居中。
 * 标题由服务端下发，不能在客户端按空格硬断行，于是限定一个展示宽度让它自然折行；
 * 更长的标题会多折几行，版式不会塌。
 */
private val TitleMaxWidth = 240.dp
private val TitleFontSize = 36.sp

/** Figma `1943:13433`：作者行顶边 447，标题底边 431。 */
private val AuthorRowTop = 16.dp

/** Figma `1943:13471`：简介顶边 499，作者行底边 471。 */
private val SynopsisTop = 28.dp

/** Figma `1943:13457`：Cast 顶边 609，简介底边 581。 */
private val CastTop = 28.dp

/** Figma `1943:13542`：互动区标题顶边 896，Cast 底边 856。 */
private val InteractionTop = 40.dp

/** Figma `1943:13441`：Continue Game 高 46、全圆角；吸底栏左右 16、上下 8。 */
private val ActionButtonHeight = 46.dp
private val ActionButtonShape = RoundedCornerShape(100.dp)
private val BottomBarPaddingH = 16.dp
private val BottomBarPaddingV = 8.dp

/** Figma `1943:13448`：收藏键 46×46、圆角 12。 */
private val SaveButtonSize = 46.dp
private val SaveButtonShape = RoundedCornerShape(12.dp)

/** 主行动渐变：`#6C32ED` 到 `#381D7D`，终点落在 157.41% 处（超出容器）。 */
internal val DetailActionBrush = Brush.verticalGradient(
    0f to YoofiDetailActionFrom,
    1.5741f to YoofiDetailActionTo,
)

/**
 * 游戏详情页，对齐 Figma `1943:13409`（简介收起）与 `1943:13476`（简介展开 + 评论）。
 *
 * 工程尚未引入 Navigation，[gameId] 由调用方直接传入并在此触发加载。
 * 键盘走 [ImeOverlayBox]；不画 iOS 状态栏与 Home Indicator，改用系统 insets。
 */
@Composable
internal fun GameDetailScreen(
    gameId: String,
    onBack: () -> Unit,
    onContinueGame: () -> Unit,
    onOpenGuestProfile: (GuestProfileTarget) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(gameId) { viewModel.load(gameId) }
    BackHandler {
        when {
            state.reportOpen -> viewModel.onIntent(GameDetailIntent.CloseReport)
            state.overlay != GameDetailOverlay.None ->
                viewModel.onIntent(GameDetailIntent.DismissOverlay)
            else -> onBack()
        }
    }
    GameDetailLayout(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onContinueGame = onContinueGame,
        onOpenGuestProfile = onOpenGuestProfile,
        modifier = modifier,
    )
}

@Composable
internal fun GameDetailLayout(
    state: GameDetailUiState,
    onIntent: (GameDetailIntent) -> Unit,
    onBack: () -> Unit,
    onContinueGame: () -> Unit,
    onOpenGuestProfile: (GuestProfileTarget) -> Unit = {},
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    val detail = state.detail
    val snackbarHostState = remember { SnackbarHostState() }
    val resetMessage = stringResource(R.string.detail_start_new_story_snackbar)
    LaunchedEffect(state.snackbar) {
        val kind = state.snackbar ?: return@LaunchedEffect
        val message = when (kind) {
            GameDetailSnackbar.StartNewStory -> resetMessage
        }
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        onIntent(GameDetailIntent.ConsumeSnackbar)
    }
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        ImeOverlayBox(modifier = Modifier.fillMaxSize()) {
            if (detail != null) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        DetailHero(coverKey = detail.coverKey)
                        DetailSheet(
                            state = state,
                            detail = detail,
                            onIntent = onIntent,
                            onOpenGuestProfile = onOpenGuestProfile,
                        )
                    }
                    // 给吸底栏让位，否则最后一条评论会被压住
                    Spacer(
                        Modifier
                            .navigationBarsPadding()
                            .height(ActionButtonHeight + BottomBarPaddingV * 2),
                    )
                }
            }
            DetailTopBar(
                onBack = onBack,
                onMore = { onIntent(GameDetailIntent.OpenMenu) },
                modifier = Modifier.align(Alignment.TopCenter),
            )
            DetailBottomBar(
                saved = detail?.saved == true,
                onContinueGame = onContinueGame,
                onToggleSaved = { onIntent(GameDetailIntent.ToggleSaved) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = YoofiSnackbarContainer,
                    contentColor = YoofiSnackbarContent,
                    shape = RoundedCornerShape(12.dp),
                )
            }
            if (state.overlay == GameDetailOverlay.Menu) {
                ReportResetSheet(
                    onReset = { onIntent(GameDetailIntent.ResetStory) },
                    onReport = { onIntent(GameDetailIntent.OpenReport) },
                    onDismiss = { onIntent(GameDetailIntent.DismissOverlay) },
                )
            }
        }
        if (state.reportOpen && detail != null) {
            ReportContentScreen(
                target = detail.toReportTarget(),
                onClose = { onIntent(GameDetailIntent.CloseReport) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** 头图与它下缘的黑色渐变。渐变让画面自然过渡到页面底色，而不是硬切一条边。 */
@Composable
private fun DetailHero(coverKey: String) {
    Image(
        painter = painterResource(detailCoverRes(coverKey)),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(HeroHeight)
            .heroScrim(),
    )
}

/**
 * 头图下缘的压暗渐变。内容卡下面那层模糊头图也要用它——
 * 设计稿里 `backdrop-blur` 模糊的是「已经压暗过」的画面，
 * 少了这一层，卡片顶部会比设计稿亮一大截。
 */
private fun Modifier.heroScrim(): Modifier = drawWithContent {
    drawContent()
    drawRect(
        Brush.verticalGradient(
            ScrimTransparentStop to Color.Transparent,
            ScrimOpaqueStop to Color.Black,
        ),
    )
}

/**
 * 半透明内容卡。顶部 262dp 仍压在头图上，所以先垫一层模糊头图再盖半透明紫，
 * 复刻设计稿的 `backdrop-blur`。[Modifier.blur] 在 Android 12 以下自动降级为不模糊，
 * 那时只剩半透明底色，观感仍然成立。
 */
@Composable
private fun DetailSheet(
    state: GameDetailUiState,
    detail: GameDetail,
    onIntent: (GameDetailIntent) -> Unit,
    onOpenGuestProfile: (GuestProfileTarget) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(top = SheetTop)
            .fillMaxWidth()
            .clip(SheetShape)
            .background(YoofiDetailSheet),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeroHeight - SheetTop)
                    .clipToBounds(),
            ) {
                Image(
                    painter = painterResource(detailCoverRes(detail.coverKey)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HeroHeight)
                        .offset(y = -SheetTop)
                        .heroScrim()
                        .blur(SheetBlurRadius),
                )
                Box(Modifier.fillMaxSize().background(YoofiDetailSheet))
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(TitleTop))
                Text(
                    text = detail.title.uppercase(),
                    color = Color.White,
                    fontSize = TitleFontSize,
                    lineHeight = TitleFontSize,
                    fontFamily = YoofiDisplaySerif,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(TitleMaxWidth),
                )
                Spacer(Modifier.height(AuthorRowTop))
                DetailAuthorRow(
                    author = detail.author,
                    onToggleFollow = { onIntent(GameDetailIntent.ToggleFollow) },
                    onAvatarClick = { onOpenGuestProfile(detail.author.toGuestProfileTarget()) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(SynopsisTop))
                DetailSynopsis(
                    title = detail.synopsisTitle,
                    body = detail.synopsis,
                    expanded = state.synopsisExpanded,
                    onToggle = { onIntent(GameDetailIntent.ToggleSynopsis) },
                )
                Spacer(Modifier.height(CastTop))
                DetailCastSection(cast = detail.cast)
                Spacer(Modifier.height(InteractionTop))
                DetailInteractionSection(
                    state = state,
                    onIntent = onIntent,
                    onOpenGuestProfile = onOpenGuestProfile,
                )
            }
        }
    }
}

/** Figma `662:2144`：返回与更多。状态栏高度交给系统 insets，不照抄设计稿的 iOS 状态栏。 */
@Composable
private fun DetailTopBar(
    onBack: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(TopBarHeight)
            .padding(horizontal = DetailPagePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_auth_back),
            contentDescription = stringResource(R.string.cd_detail_back),
            modifier = Modifier
                .size(24.dp)
                .clickableDismissingIme(onClick = onBack),
        )
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.ic_detail_more),
            contentDescription = stringResource(R.string.cd_detail_more),
            modifier = Modifier
                .size(24.dp)
                .clickableDismissingIme(onClick = onMore),
        )
    }
}

/** Figma `1943:13439`：Continue Game 占满剩余宽度，右侧是 46 见方的收藏键。 */
@Composable
private fun DetailBottomBar(
    saved: Boolean,
    onContinueGame: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(YoofiDetailBackground)
            .navigationBarsPadding()
            .padding(horizontal = BottomBarPaddingH, vertical = BottomBarPaddingV),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(ActionButtonHeight)
                .clip(ActionButtonShape)
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
                .size(SaveButtonSize)
                .clip(SaveButtonShape)
                .background(YoofiDetailFieldFill)
                .clickableDismissingIme(role = Role.Checkbox, onClick = onToggleSaved),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_detail_save),
                contentDescription = stringResource(R.string.cd_detail_save),
                alpha = if (saved) 1f else 0.8f,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GameDetailCollapsedPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        GameDetailLayout(
            state = previewDetailState(synopsisExpanded = false),
            onIntent = {},
            onBack = {},
            onContinueGame = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun GameDetailExpandedPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        GameDetailLayout(
            state = previewDetailState(synopsisExpanded = true),
            onIntent = {},
            onBack = {},
            onContinueGame = {},
        )
    }
}

internal fun GameAuthor.toGuestProfileTarget(): GuestProfileTarget = GuestProfileTarget(
    userId = id,
    displayName = name,
    avatarKey = avatarKey,
    following = following,
)

internal fun GameComment.toGuestProfileTargetOrNull(): GuestProfileTarget? {
    if (deletable) return null
    return GuestProfileTarget(
        userId = "name:$authorName:$avatarKey",
        displayName = authorName,
        avatarKey = avatarKey,
    )
}
