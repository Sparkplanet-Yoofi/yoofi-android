package ai.yoofi.app.domain.gamedetail

/**
 * 作品作者，对应 Figma `1943:13433` 标题下那一行。
 *
 * [avatarKey] 与 [GameDetail.coverKey] 同理：mock 阶段是本地图标识，
 * 接真实接口后直接换成 URL，领域模型不动。
 */
data class GameAuthor(
    val id: String,
    val name: String,
    val avatarKey: String,
    val following: Boolean,
)

/**
 * Cast 里的一个角色，对应 Figma `1943:13465` 的 68×68 头像格。
 */
data class GameCastMember(
    val id: String,
    val name: String,
    val portraitKey: String,
)

/**
 * 一条评论，对应 Figma `1889:13079`（主楼）与 `1943:13551`（楼中楼）。
 *
 * 主楼与回复是同一个类型，靠 [replies] 是否为空区分层级。设计稿只画到二层，
 * 但服务端大概率返回任意深度，所以这里不做层数限制，由 UI 决定渲染几层。
 *
 * @property playedBadge 头衔右侧的时长徽章文案，如 `2.5 h`；服务端给什么显示什么，
 *   客户端不做时长换算，免得和服务端口径打架
 * @property createdAtLabel 已格式化的时间文案，如 `08/27 22:21`。
 *   同样交给服务端定口径，客户端不碰时区
 * @property replyToName 楼中楼里「A reply B」的 B；主楼与直接回复主楼时为 null
 * @property deletable 是否显示删除按钮。设计稿只在自己的评论上画了垃圾桶
 */
data class GameComment(
    val id: String,
    val authorName: String,
    val avatarKey: String,
    val body: String,
    val likeCount: Int,
    val liked: Boolean,
    val replyCount: Int,
    val isAuthor: Boolean,
    val playedBadge: String,
    val createdAtLabel: String,
    val replyToName: String? = null,
    val deletable: Boolean = false,
    val replies: List<GameComment> = emptyList(),
)

/**
 * 游戏详情页主体，对应 Figma `1943:13409`（简介收起）/ `1943:13476`（简介展开 + 评论）。
 *
 * 评论**不在这里**：它要翻页、要在发布后局部刷新，与详情主体的生命周期不同，
 * 所以走 [GameDetailRepository.comments] 单独取。
 *
 * @property synopsisTitle 简介小标题，设计稿里是「forbidden world：」，
 *   与 [title] 分开存是因为服务端可能给不同措辞，不要在客户端拼
 */
data class GameDetail(
    val id: String,
    val title: String,
    val coverKey: String,
    val author: GameAuthor,
    val synopsisTitle: String,
    val synopsis: String,
    val cast: List<GameCastMember>,
    val saved: Boolean,
)
