package ai.yoofi.app.domain.search

/**
 * 输入过程中的联想词，对应 Figma `1943:13804`「搜索-输入态」的一行。
 *
 * 只带完整标题，命中片段由 UI 按当前查询词现算，
 * 这样服务端换成任何分词策略都不用改协议。
 */
data class SearchSuggestion(
    val id: String,
    val title: String,
)

/**
 * 搜索结果作品，对应 Figma `1943:13841`「搜索-结果态」的一张卡。
 *
 * [coverKey] 是封面标识而非资源 id：mock 阶段由 UI 映射到本地图，
 * 接真实接口后直接换成图片 URL，领域模型不用动。
 */
data class SearchStory(
    val id: String,
    val title: String,
    val coverKey: String,
)
