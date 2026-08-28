package ai.yoofi.app.domain.search

/**
 * 提交搜索。空关键词直接短路，不打接口
 */
class SearchStoriesUseCase(
    private val repository: SearchRepository,
) {
    suspend operator fun invoke(query: String): List<SearchStory> {
        val keyword = query.trim()
        return if (keyword.isEmpty()) emptyList() else repository.search(keyword)
    }
}
