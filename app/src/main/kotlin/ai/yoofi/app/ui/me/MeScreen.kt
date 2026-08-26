package ai.yoofi.app.ui.me

import ai.yoofi.app.R
import ai.yoofi.app.ui.pager.animateToRealPage
import ai.yoofi.app.ui.pager.loopingPageCount
import ai.yoofi.app.ui.pager.loopingStartPage
import ai.yoofi.app.ui.pager.realPageIndex
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiProfileStroke
import ai.yoofi.app.ui.theme.YoofiSnackbarContainer
import ai.yoofi.app.ui.theme.YoofiSnackbarContent
import ai.yoofi.app.ui.theme.YoofiStartGameFrom
import ai.yoofi.app.ui.theme.YoofiVipText
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private enum class MePrimaryTab { Lorebook, Creations }

private enum class MeWorkKind { StoryGame, Story }

private data class MeCreation(
    val id: String,
    val kind: MeWorkKind,
    @param:DrawableRes val coverRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val viewsRes: Int,
)

private val DemoCreations = listOf(
    MeCreation(
        id = "sg-a",
        kind = MeWorkKind.StoryGame,
        coverRes = R.drawable.img_game_cover_e,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    MeCreation(
        id = "sg-b",
        kind = MeWorkKind.StoryGame,
        coverRes = R.drawable.img_game_cover_d,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    MeCreation(
        id = "sg-c",
        kind = MeWorkKind.StoryGame,
        coverRes = R.drawable.img_game_cover_a,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    MeCreation(
        id = "sg-d",
        kind = MeWorkKind.StoryGame,
        coverRes = R.drawable.img_game_cover_c,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    MeCreation(
        id = "st-a",
        kind = MeWorkKind.Story,
        coverRes = R.drawable.img_home_listed_1,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    MeCreation(
        id = "st-b",
        kind = MeWorkKind.Story,
        coverRes = R.drawable.img_home_listed_2,
        titleRes = R.string.card_arranged_short,
        viewsRes = R.string.me_views_sample,
    ),
    MeCreation(
        id = "st-c",
        kind = MeWorkKind.Story,
        coverRes = R.drawable.img_home_listed_3,
        titleRes = R.string.card_arranged_marriage,
        viewsRes = R.string.me_views_sample,
    ),
    MeCreation(
        id = "st-d",
        kind = MeWorkKind.Story,
        coverRes = R.drawable.img_game_cover_b,
        titleRes = R.string.card_arranged_short,
        viewsRes = R.string.me_views_sample,
    ),
)

private val CardOverlayBrush = Brush.verticalGradient(
    0.57105f to Color.Transparent,
    0.72478f to Color(0x7A110F3A),
    0.96459f to Color(0x8A1F003F),
)

/**
 * 我的页。主态对齐 `982:13174`，创建态对齐 `982:12845`。
 * Lorebook / Creations 为无限循环 HorizontalPager；Story Game / Story 仍在本地切换。
 */
@Composable
fun MeScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onPreviewProfile: () -> Unit = {},
) {
    val primaryTabs = MePrimaryTab.entries
    val cycle = primaryTabs.size
    val pagerState = rememberPagerState(
        initialPage = loopingStartPage(cycle),
        pageCount = { loopingPageCount(cycle) },
    )
    val primaryTab = primaryTabs[realPageIndex(pagerState.currentPage, cycle)]
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val userId = stringResource(R.string.me_user_id)
    val copyLabel = stringResource(R.string.cd_copy_id)
    val copiedMessage = stringResource(R.string.me_id_copied)
    var workKind by remember { mutableStateOf(MeWorkKind.StoryGame) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Image(
            painter = painterResource(R.drawable.img_me_header_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.2f,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 120.dp),
        ) {
            MeTopBar(
                onSettingsClick = onSettingsClick,
                onPreviewProfile = onPreviewProfile,
            )
            ProfileCard(
                onCopyId = {
                    copyUserIdToClipboard(context, label = copyLabel, userId = userId)
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = copiedMessage,
                            duration = SnackbarDuration.Short,
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 1.dp),
            )
            Spacer(Modifier.height(16.dp))
            MePrimaryTabs(
                selected = primaryTab,
                onSelected = { tab ->
                    scope.launch {
                        pagerState.animateToRealPage(primaryTabs.indexOf(tab), cycle)
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                beyondViewportPageCount = 1,
                key = { page -> page },
            ) { page ->
                when (primaryTabs[realPageIndex(page, cycle)]) {
                    MePrimaryTab.Lorebook -> LorebookEmptyPane(Modifier.fillMaxSize())
                    MePrimaryTab.Creations -> CreationsPane(
                        workKind = workKind,
                        onWorkKindChange = { workKind = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 128.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = YoofiSnackbarContainer,
                contentColor = YoofiSnackbarContent,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
private fun MeTopBar(
    onSettingsClick: () -> Unit,
    onPreviewProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_settings_hex),
            contentDescription = stringResource(R.string.cd_settings),
            modifier = Modifier
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onSettingsClick),
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                .clickable(role = Role.Button, onClick = onPreviewProfile)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_preview_card),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.me_preview_profile),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ProfileCard(
    onCopyId: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xCC110234), Color(0xCC261A42)),
                ),
            )
            .border(1.dp, YoofiProfileStroke, RoundedCornerShape(24.dp)),
    ) {
        Image(
            painter = painterResource(R.drawable.img_me_avatar),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 12.dp)
                .align(Alignment.CenterStart)
                .size(72.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .padding(start = 94.dp, top = 16.dp, end = 20.dp),
        ) {
            // 姓名 / ID 给右侧 Get VIP 留位；关注数据在按钮下方，不共用 88dp 右边距
            Column(modifier = Modifier.padding(end = 68.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.me_display_name),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painter = painterResource(R.drawable.ic_edit_pencil),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_badge_fan),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFFC95D), Color(0xFFFF903A)),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "ID",
                            color = Color.White,
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.me_user_id),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                    )
                    Spacer(Modifier.width(4.dp))
                    Image(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = stringResource(R.string.cd_copy_id),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(role = Role.Button, onClick = onCopyId)
                            .padding(6.dp),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.me_following_count),
                    color = Color(0xFFDEDEDE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.me_following_label),
                    color = Color(0xFFB3B3B3),
                    fontSize = 12.sp,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.me_follower_count),
                    color = Color(0xFFDEDEDE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.me_follower_label),
                    color = Color(0xFFB3B3B3),
                    fontSize = 12.sp,
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFA4B5FF),
                            Color(0xFFD9F3FF),
                            Color(0xFFE5DDFF),
                            Color(0xFFB951DE),
                        ),
                    ),
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.me_get_vip),
                color = YoofiVipText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
            )
            Spacer(Modifier.width(4.dp))
            Image(
                painter = painterResource(R.drawable.ic_vip_caret),
                contentDescription = null,
                modifier = Modifier
                    .size(7.dp, 6.dp)
                    .rotate(90f),
            )
        }
    }
}

@Composable
private fun MePrimaryTabs(
    selected: MePrimaryTab,
    onSelected: (MePrimaryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        MePrimaryTabItem(
            label = stringResource(R.string.me_tab_lorebook),
            selected = selected == MePrimaryTab.Lorebook,
            onClick = { onSelected(MePrimaryTab.Lorebook) },
        )
        MePrimaryTabItem(
            label = stringResource(R.string.me_tab_creations),
            selected = selected == MePrimaryTab.Creations,
            onClick = { onSelected(MePrimaryTab.Creations) },
        )
    }
}

@Composable
private fun MePrimaryTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(16.dp, 4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(if (selected) Color.White else Color.Transparent),
        )
    }
}

@Composable
private fun LorebookEmptyPane(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.me_lorebook_empty),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
        )
    }
}

@Composable
private fun CreationsPane(
    workKind: MeWorkKind,
    onWorkKindChange: (MeWorkKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember(workKind) { DemoCreations.filter { it.kind == workKind } }
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkKindChip(
                label = stringResource(R.string.me_filter_story_game),
                selected = workKind == MeWorkKind.StoryGame,
                onClick = { onWorkKindChange(MeWorkKind.StoryGame) },
            )
            WorkKindChip(
                label = stringResource(R.string.me_filter_story),
                selected = workKind == MeWorkKind.Story,
                onClick = { onWorkKindChange(MeWorkKind.Story) },
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(items, key = { it.id }) { item ->
                CreationCard(item)
            }
        }
    }
}

@Composable
private fun WorkKindChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(100.dp)
    Text(
        text = label,
        color = if (selected) YoofiAccent else Color.White.copy(alpha = 0.45f),
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .background(Color.Black)
                        .border(1.dp, YoofiAccent, shape)
                } else {
                    Modifier.background(Color.White.copy(alpha = 0.1f))
                },
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun CreationCard(item: MeCreation) {
    val title = stringResource(item.titleRes)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(226.dp)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        Image(
            painter = painterResource(item.coverRes),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardOverlayBrush),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_me_drafts),
                contentDescription = null,
                modifier = Modifier.size(12.dp, 9.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.me_badge_drafts),
                color = YoofiStartGameFrom,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(item.viewsRes),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
            )
        }
    }
}

/**
 * 将用户 ID 写入系统剪贴板。label 供系统粘贴面板展示，不进入业务逻辑。
 */
private fun copyUserIdToClipboard(context: Context, label: String, userId: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, userId))
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MeScreenPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        MeScreen()
    }
}
