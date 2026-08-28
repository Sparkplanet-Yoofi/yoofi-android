package ai.yoofi.app.domain.search

/**
 * 作品搜索契约。挂起函数即远端调用的接缝，
 * 换真实接口时只替换实现（照 `RemoteDataSource` + `ApiCaller` 那套接），调用方无感。
 */
interface SearchRepository {

    /**
     * 输入过程中的联想词。[query] 由 UseCase 保证已去首尾空白且非空
     */
    suspend fun suggest(query: String): List<SearchSuggestion>

    /**
     * 提交搜索后的作品列表。返回空列表即「空状态」，不要用异常表达无结果
     */
    suspend fun search(query: String): List<SearchStory>
}
