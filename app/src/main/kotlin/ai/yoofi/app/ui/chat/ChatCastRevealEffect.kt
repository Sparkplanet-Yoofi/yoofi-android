package ai.yoofi.app.ui.chat

import kotlin.random.Random

/**
 * 人物卡揭示特效，与 iOS `ChatRelationshipShaders.metal` 中 `relationshipReveal` 的 `effectValue` 一一对应。
 *
 * [shaderId] 必须与 metal / AGSL 里的分支编号保持一致，新增效果时两侧要同步改。
 */
internal enum class ChatCastRevealEffect(val shaderId: Int) {
    /** 全息扫描：一道发光扫描线推进并消解 RGB 色散 */
    HolographicScan(0),

    /** 数据重组：横向条带各自错位后归位 */
    DataReassembly(1),

    /** 液态揭示：有机的径向边缘一边扩张一边折射画面 */
    LiquidReveal(2),

    /** 空间传送：像素从景深处向内汇聚，带径向色散拖影 */
    SpatialTransport(3),

    /** 能量裂隙：中央亮口横向撕开露出卡面 */
    EnergyRift(4),

    /** 数字故障：高频跳变色块、跳帧与强烈 RGB 分离 */
    DigitalGlitch(5),

    /** 卡牌召唤：径向光环叠加流动的镭射高光 */
    CardSummon(6),

    /** 金色召唤：有机溶解配克制的暖色描边，对应 metal 的默认分支 */
    GoldenSummon(7),
    ;

    companion object {
        /** 当前按需求随机挑一种；以后要固定效果，调用方直接传入指定枚举即可 */
        fun random(random: Random = Random.Default): ChatCastRevealEffect = entries.random(random)

        /** 按 shader 分支编号取效果，越界时与 metal 一致地回落到金色召唤 */
        fun fromShaderId(shaderId: Int): ChatCastRevealEffect =
            entries.firstOrNull { it.shaderId == shaderId } ?: GoldenSummon
    }
}
