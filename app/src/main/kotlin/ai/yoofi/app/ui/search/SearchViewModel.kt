package ai.yoofi.app.ui.search

import ai.yoofi.app.domain.search.SearchStoriesUseCase
import ai.yoofi.app.domain.search.SearchStory
import ai.yoofi.app.domain.search.SearchSuggestion
import ai.yoofi.app.domain.search.SuggestStoriesUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 输入停顿多久才打联想接口，避免逐字符请求。 */
internal const val SuggestDebounceMillis = 300L

/**
 * 搜索页的四个内容态，逐一对应 Figma 画板。
 * 顶部搜索框在四态里都在，所以不纳入这里，由 [SearchUiState.query] 单独描述。
 */
internal sealed interface SearchContent {
    /** `1943:13781` 初始态：关键词为空，正文留白。 */
    data object Idle : SearchContent

    /** `1943:13804` 输入态：边打字边出联想词。 */
    data class Suggestions(val items: List<SearchSuggestion>) : SearchContent

    /** `1943:13841` 结果态。 */
    data class Results(val items: List<SearchStory>) : SearchContent

    /** `1943:13894` 空状态：搜过了但一条都没有。 */
    data object Empty : SearchContent
}

internal data class SearchUiState(
    val query: String = "",
    val content: SearchContent = SearchContent.Idle,
)

internal sealed interface SearchIntent {
    data class QueryChanged(val value: String) : SearchIntent

    /** 点联想词：把它填回输入框并立刻搜。 */
    data class PickSuggestion(val title: String) : SearchIntent

    /** 键盘 search 键。 */
    data object Submit : SearchIntent
    data object ClearQuery : SearchIntent
}

/**
 * 搜索页 ViewModel。只依赖 UseCase，不碰任何数据源实现。
 *
 * 联想与搜索各自持有 Job：任何一方启动前先取消另一方，
 * 这样「打完字立刻按搜索」不会被迟到的联想结果把结果态盖回去。
 */
@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val suggestStories: SuggestStoriesUseCase,
    private val searchStories: SearchStoriesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var suggestJob: Job? = null
    private var searchJob: Job? = null

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> onQueryChanged(intent.value)
            is SearchIntent.PickSuggestion -> submit(intent.title)
            SearchIntent.Submit -> submit(_uiState.value.query)
            SearchIntent.ClearQuery -> onQueryChanged("")
        }
    }

    private fun onQueryChanged(value: String) {
        cancelPending()
        val keyword = value.trim()
        if (keyword.isEmpty()) {
            _uiState.value = SearchUiState(query = value, content = SearchContent.Idle)
            return
        }
        _uiState.update { it.copy(query = value) }
        suggestJob = viewModelScope.launch {
            delay(SuggestDebounceMillis)
            val items = suggestStories(keyword)
            _uiState.update { it.copy(content = SearchContent.Suggestions(items)) }
        }
    }

    private fun submit(query: String) {
        val keyword = query.trim()
        if (keyword.isEmpty()) return
        cancelPending()
        _uiState.update { it.copy(query = query) }
        searchJob = viewModelScope.launch {
            val stories = searchStories(keyword)
            _uiState.update {
                it.copy(
                    content = if (stories.isEmpty()) {
                        SearchContent.Empty
                    } else {
                        SearchContent.Results(stories)
                    },
                )
            }
        }
    }

    private fun cancelPending() {
        suggestJob?.cancel()
        suggestJob = null
        searchJob?.cancel()
        searchJob = null
    }
}
