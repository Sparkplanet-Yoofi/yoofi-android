package ai.yoofi.app.ui.auth

/**
 * 资料页的两条产品入口。创建与编辑以后走不同接口，
 * 禁止收成 `isEdit: Boolean`——入口上的差异（能否 Skip、预填、提交契约）会越长越多。
 */
internal sealed interface ProfileEditorEntry {
    /** 注册后首次完善资料，对齐 Figma `2117:18954` 起。 */
    data object Create : ProfileEditorEntry

    /** Me 页编辑已有资料，对齐 Figma `1943:14006`。 */
    data object Edit : ProfileEditorEntry
}
