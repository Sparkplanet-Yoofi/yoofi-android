package ai.yoofi.app.domain.search

/**
 * 取联想词。空关键词直接短路，不打接口
 */
class SuggestStoriesUseCase(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(query: String): List<SearchSuggestion> {
        val keyword = query.trim()
        return if (keyword.isEmpty()) emptyList() else repository.suggest(keyword)
    }
}
