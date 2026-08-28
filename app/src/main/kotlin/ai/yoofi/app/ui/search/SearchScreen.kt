package ai.yoofi.app.ui.search

import ai.yoofi.app.R
import ai.yoofi.app.domain.search.SearchStory
import ai.yoofi.app.domain.search.SearchSuggestion
import ai.yoofi.app.ui.ime.ImeOverlayBox
import ai.yoofi.app.ui.ime.clickableDismissingIme
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import ai.yoofi.app.ui.theme.YoofiSearchCaretFrom
import ai.yoofi.app.ui.theme.YoofiSearchCaretTo
import ai.yoofi.app.ui.theme.YoofiSearchCardScrimEnd
import ai.yoofi.app.ui.theme.YoofiSearchCardScrimMid
import ai.yoofi.app.ui.theme.YoofiSearchFieldFill
import ai.yoofi.app.ui.theme.YoofiSearchFieldStroke
import ai.yoofi.app.ui.theme.YoofiSearchHighlight
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Figma `1943:13795`：状态栏下方那条 60 高的搜索行。 */
private val HeaderBarHeight = 60.dp

/** Figma `1943:13799`：搜索框 299×40，此处宽度由 weight 撑（390 屏正好 299）。 */
private val SearchFieldHeight = 40.dp
private val SearchFieldShape = RoundedCornerShape(100.dp)

/** Figma `1943:13801`：占位符 x=37、光标 x=32，两者差 5。 */
private val SearchHintCaretGap = 5.dp

/** Figma `1943:13814`：首条联想词顶边 116，减去 108 的 Header 高。 */
private val SuggestionListTop = 8.dp

/** Figma `1943:13850`：结果网格顶边 132，减去 108 的 Header 高。 */
private val ResultGridTop = 24.dp

/** Figma `1943:13850`：网格 x=20..372，右边距比左边窄 2。 */
private val ResultGridStart = 20.dp
private val ResultGridEnd = 18.dp
private val ResultGridGap = 8.dp
private const val ResultColumns = 3

/** Figma `1943:13851`：结果卡 112×150、圆角 8。 */
private val ResultCardHeight = 150.dp
private val ResultCardShape = RoundedCornerShape(8.dp)

/** Figma `1943:13903`：空状态文案顶边 156，减去 108 的 Header 高。 */
private val EmptyHintTop = 48.dp

/**
 * 作品搜索页，对齐 Figma `1943:13781`（初始）/ `1943:13804`（输入）/
 * `1943:13841`（结果）/ `1943:13894`（空）四态。
 *
 * 键盘走 [ImeOverlayBox]，不画 iOS 状态栏 / Home Indicator。
 */
@Composable
internal fun SearchScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onStoryClick: (SearchStory) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchLayout(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onStoryClick = onStoryClick,
        modifier = modifier,
    )
}

/** 无状态骨架：四态可以直接喂进来，便于 Preview 与截图核对。 */
@Composable
internal fun SearchLayout(
    state: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    onBack: () -> Unit,
    onStoryClick: (SearchStory) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    ImeOverlayBox(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.img_search_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            // 纯渐变底图，拉伸不可见，比裁切更能保证顶部光晕位置
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            SearchHeader(
                query = state.query,
                onQueryChange = { onIntent(SearchIntent.QueryChanged(it)) },
                onSubmit = { onIntent(SearchIntent.Submit) },
                onClear = { onIntent(SearchIntent.ClearQuery) },
                onCancel = onBack,
            )
            when (val content = state.content) {
                SearchContent.Idle -> Unit
                is SearchContent.Suggestions -> SuggestionList(
                    items = content.items,
                    query = state.query,
                    onPick = { onIntent(SearchIntent.PickSuggestion(it)) },
                )
                is SearchContent.Results -> ResultGrid(
                    items = content.items,
                    onStoryClick = onStoryClick,
                )
                SearchContent.Empty -> EmptyHint()
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderBarHeight)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            onSubmit = onSubmit,
            onClear = onClear,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.search_cancel),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.clickableDismissingIme(onClick = onCancel),
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 四个设计稿都带光标，进页即聚焦拉起键盘
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        ),
        cursorBrush = Brush.verticalGradient(
            colors = listOf(YoofiSearchCaretFrom, YoofiSearchCaretTo),
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        modifier = modifier
            .height(SearchFieldHeight)
            .focusRequester(focusRequester),
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(YoofiSearchFieldFill, SearchFieldShape)
                    .border(1.dp, YoofiSearchFieldStroke, SearchFieldShape)
                    .padding(start = 12.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = SearchHintCaretGap),
                        )
                    }
                    inner()
                }
                if (query.isNotEmpty()) {
                    Image(
                        painter = painterResource(R.drawable.ic_search_clear),
                        contentDescription = stringResource(R.string.cd_search_clear),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(16.dp)
                            .clickableDismissingIme(role = Role.Button, onClick = onClear),
                    )
                }
            }
        },
    )
}

@Composable
private fun SuggestionList(
    items: List<SearchSuggestion>,
    query: String,
    onPick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = SuggestionListTop),
    ) {
        items(items = items, key = { it.id }) { item ->
            SuggestionRow(
                title = item.title,
                query = query,
                onClick = { onPick(item.title) },
            )
        }
    }
}

/** Figma `1943:13814`：行高 44 = 上下各 12 的留白 + 20 的文字行高。 */
@Composable
private fun SuggestionRow(
    title: String,
    query: String,
    onClick: () -> Unit,
) {
    Text(
        text = highlightQuery(title, query),
        color = Color.White,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clickableDismissingIme(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun ResultGrid(
    items: List<SearchStory>,
    onStoryClick: (SearchStory) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(ResultColumns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ResultGridStart,
            end = ResultGridEnd,
            top = ResultGridTop,
            bottom = ResultGridTop,
        ),
        horizontalArrangement = Arrangement.spacedBy(ResultGridGap),
        verticalArrangement = Arrangement.spacedBy(ResultGridGap),
    ) {
        items(items = items, key = { it.id }) { story ->
            ResultCard(story = story, onClick = { onStoryClick(story) })
        }
    }
}

@Composable
private fun ResultCard(
    story: SearchStory,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ResultCardHeight)
            .clip(ResultCardShape)
            .clickableDismissingIme(onClick = onClick),
    ) {
        Image(
            painter = painterResource(searchCoverRes(story.coverKey)),
            contentDescription = story.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.57105f to Color.Transparent,
                        0.72478f to YoofiSearchCardScrimMid,
                        0.96459f to YoofiSearchCardScrimEnd,
                    ),
                ),
        )
        Text(
            text = story.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        )
    }
}

@Composable
private fun EmptyHint() {
    Text(
        text = stringResource(R.string.search_empty),
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = EmptyHintTop, start = 20.dp, end = 20.dp),
    )
}

/**
 * 把标题里命中关键词的片段染成高亮色，大小写不敏感、逐段扫完整串。
 * 高亮只在展示层算，服务端不必回分词位置。
 */
internal fun highlightQuery(
    title: String,
    query: String,
    highlight: Color = YoofiSearchHighlight,
): AnnotatedString {
    val keyword = query.trim()
    if (keyword.isEmpty()) return AnnotatedString(title)
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < title.length) {
            val hit = title.indexOf(keyword, cursor, ignoreCase = true)
            if (hit < 0) break
            append(title.substring(cursor, hit))
            withStyle(SpanStyle(color = highlight)) {
                append(title.substring(hit, hit + keyword.length))
            }
            cursor = hit + keyword.length
        }
        append(title.substring(cursor))
    }
}

/** mock 封面标识到本地图的映射；接真实接口后这里换成图片加载。 */
@DrawableRes
internal fun searchCoverRes(coverKey: String): Int = when (coverKey) {
    "cover-1" -> R.drawable.img_search_cover_1
    "cover-2" -> R.drawable.img_search_cover_2
    else -> R.drawable.img_search_cover_3
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SearchScreenIdlePreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        SearchLayout(
            state = SearchUiState(),
            onIntent = {},
            onBack = {},
            onStoryClick = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SearchScreenSuggestionsPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        SearchLayout(
            state = SearchUiState(
                query = "Marriage",
                content = SearchContent.Suggestions(
                    listOf(
                        SearchSuggestion("1", "Marriage Avenger"),
                        SearchSuggestion("2", "Abandon a terrible Marriage"),
                        SearchSuggestion("3", "Marriage Stealer"),
                        SearchSuggestion("4", "Marriage Wedding Dress"),
                        SearchSuggestion("5", "A Marriage with Princess"),
                    ),
                ),
            ),
            onIntent = {},
            onBack = {},
            onStoryClick = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SearchScreenResultsPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        SearchLayout(
            state = SearchUiState(
                query = "Marriage",
                content = SearchContent.Results(
                    List(7) { index ->
                        SearchStory(
                            id = "story-$index",
                            title = "Result ${index + 1}",
                            coverKey = "cover-${index % 3 + 1}",
                        )
                    },
                ),
            ),
            onIntent = {},
            onBack = {},
            onStoryClick = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SearchScreenEmptyPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        SearchLayout(
            state = SearchUiState(query = "Marriage", content = SearchContent.Empty),
            onIntent = {},
            onBack = {},
            onStoryClick = {},
        )
    }
}
