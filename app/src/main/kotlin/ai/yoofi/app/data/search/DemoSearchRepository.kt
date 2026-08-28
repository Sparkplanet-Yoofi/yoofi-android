package ai.yoofi.app.data.search

import ai.yoofi.app.domain.search.SearchRepository
import ai.yoofi.app.domain.search.SearchStory
import ai.yoofi.app.domain.search.SearchSuggestion
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

/**
 * 对齐 Figma `1943:13804`（输入态）/ `1943:13841`（结果态）/ `1943:13894`（空状态）的英文演示稿。
 *
 * 设计稿里联想词与结果卡本就是两份互不相干的占位数据，这里如实照搬：
 * 命中演示词库就回结果卡，未命中就回空列表以驱动空状态。
 * 接真实接口后整个文件换成远端 DataSource 实现即可，[SearchRepository] 契约不动。
 */
@Singleton
class DemoSearchRepository @Inject constructor() : SearchRepository {

    override suspend fun suggest(query: String): List<SearchSuggestion> {
        delay(DemoLatencyMillis)
        return DemoTitles.withIndex()
            .filter { (_, title) -> title.contains(query, ignoreCase = true) }
            .map { (index, title) ->
                SearchSuggestion(id = "suggest-${index + 1}", title = title)
            }
    }

    override suspend fun search(query: String): List<SearchStory> {
        delay(DemoLatencyMillis)
        val hit = DemoTitles.any { it.contains(query, ignoreCase = true) }
        return if (hit) DemoStories else emptyList()
    }
}

/** 演示接口耗时，用来验证防抖与状态切换，不代表真实网络。 */
private const val DemoLatencyMillis = 180L

/** Figma `1943:13804` 的 5 条联想词，顺序与设计稿自上而下一致。 */
private val DemoTitles = listOf(
    "Marriage Avenger",
    "Abandon a terrible Marriage",
    "Marriage Stealer",
    "Marriage Wedding Dress",
    "A Marriage with Princess",
)

private val DemoCoverKeys = listOf("cover-1", "cover-2", "cover-3")

/** Figma `1943:13841` 的 7 张结果卡，封面按三张演示图循环。 */
private val DemoStories = List(7) { index ->
    SearchStory(
        id = "story-${index + 1}",
        title = "Result ${index + 1}",
        coverKey = DemoCoverKeys[index % DemoCoverKeys.size],
    )
}
