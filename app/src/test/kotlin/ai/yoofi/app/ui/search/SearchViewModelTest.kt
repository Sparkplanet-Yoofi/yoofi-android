package ai.yoofi.app.ui.search

import ai.yoofi.app.domain.search.SearchRepository
import ai.yoofi.app.domain.search.SearchStoriesUseCase
import ai.yoofi.app.domain.search.SearchStory
import ai.yoofi.app.domain.search.SearchSuggestion
import ai.yoofi.app.domain.search.SuggestStoriesUseCase
import ai.yoofi.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `初始就是初始态`() {
        val viewModel = viewModel()
        assertEquals("", viewModel.uiState.value.query)
        assertEquals(SearchContent.Idle, viewModel.uiState.value.content)
    }

    @Test
    fun `输入未到防抖时间不打联想接口`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeSearchRepository()
        val viewModel = viewModel(repository)

        viewModel.onIntent(SearchIntent.QueryChanged("Mar"))

        assertEquals("Mar", viewModel.uiState.value.query)
        assertEquals(SearchContent.Idle, viewModel.uiState.value.content)
        assertEquals(0, repository.suggestCalls)
    }

    @Test
    fun `防抖到期后出联想词`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeSearchRepository()
        val viewModel = viewModel(repository)

        viewModel.onIntent(SearchIntent.QueryChanged("Marriage"))
        advanceUntilIdle()

        val content = viewModel.uiState.value.content
        assertTrue(content is SearchContent.Suggestions)
        assertEquals(2, (content as SearchContent.Suggestions).items.size)
        assertEquals(1, repository.suggestCalls)
    }

    @Test
    fun `连续输入只打最后一次联想`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeSearchRepository()
        val viewModel = viewModel(repository)

        viewModel.onIntent(SearchIntent.QueryChanged("M"))
        viewModel.onIntent(SearchIntent.QueryChanged("Ma"))
        viewModel.onIntent(SearchIntent.QueryChanged("Marriage"))
        advanceUntilIdle()

        assertEquals(1, repository.suggestCalls)
        assertEquals("Marriage", repository.lastSuggestQuery)
    }

    @Test
    fun `打完字立刻提交不会被迟到的联想覆盖`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeSearchRepository()
        val viewModel = viewModel(repository)

        viewModel.onIntent(SearchIntent.QueryChanged("Marriage"))
        viewModel.onIntent(SearchIntent.Submit)
        advanceUntilIdle()

        val content = viewModel.uiState.value.content
        assertTrue(content is SearchContent.Results)
        assertEquals(0, repository.suggestCalls)
    }

    @Test
    fun `搜不到结果进入空状态`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("nothing"))
        viewModel.onIntent(SearchIntent.Submit)
        advanceUntilIdle()

        assertEquals(SearchContent.Empty, viewModel.uiState.value.content)
    }

    @Test
    fun `点联想词会回填输入框并直接出结果`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(SearchIntent.PickSuggestion("Marriage Stealer"))
        advanceUntilIdle()

        assertEquals("Marriage Stealer", viewModel.uiState.value.query)
        assertTrue(viewModel.uiState.value.content is SearchContent.Results)
    }

    @Test
    fun `清空关键词回到初始态`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(SearchIntent.QueryChanged("Marriage"))
        advanceUntilIdle()
        viewModel.onIntent(SearchIntent.ClearQuery)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.query)
        assertEquals(SearchContent.Idle, viewModel.uiState.value.content)
    }

    @Test
    fun `只有空白的关键词不触发搜索`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeSearchRepository()
        val viewModel = viewModel(repository)

        viewModel.onIntent(SearchIntent.QueryChanged("   "))
        viewModel.onIntent(SearchIntent.Submit)
        advanceUntilIdle()

        assertEquals(SearchContent.Idle, viewModel.uiState.value.content)
        assertEquals(0, repository.suggestCalls)
        assertEquals(0, repository.searchCalls)
    }

    private fun viewModel(
        repository: SearchRepository = FakeSearchRepository(),
    ): SearchViewModel = SearchViewModel(
        suggestStories = SuggestStoriesUseCase(repository),
        searchStories = SearchStoriesUseCase(repository),
    )
}

/** 只认 marriage 关键词，其余一律无结果，用来驱动结果态与空状态。 */
private class FakeSearchRepository : SearchRepository {

    var suggestCalls: Int = 0
        private set
    var searchCalls: Int = 0
        private set
    var lastSuggestQuery: String? = null
        private set

    override suspend fun suggest(query: String): List<SearchSuggestion> {
        suggestCalls += 1
        lastSuggestQuery = query
        delay(FakeLatencyMillis)
        return if (query.contains("marriage", ignoreCase = true)) {
            listOf(
                SearchSuggestion("s1", "Marriage Avenger"),
                SearchSuggestion("s2", "Marriage Stealer"),
            )
        } else {
            emptyList()
        }
    }

    override suspend fun search(query: String): List<SearchStory> {
        searchCalls += 1
        delay(FakeLatencyMillis)
        return if (query.contains("marriage", ignoreCase = true)) {
            listOf(SearchStory("story-1", "Result 1", "cover-1"))
        } else {
            emptyList()
        }
    }
}

private const val FakeLatencyMillis = 50L
