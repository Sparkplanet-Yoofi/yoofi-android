package ai.yoofi.app.domain.profile

/**
 * 资料表单提交体。创建与编辑共用字段，接口路径由各自 UseCase 决定。
 *
 * @param genderKey 未选性别为 null；取值与 UI 的 GenderOption.name 对齐，
 *   接真实接口时再在数据源里映射成服务端枚举。
 */
data class ProfileDraft(
    val displayName: String,
    val genderKey: String?,
)
