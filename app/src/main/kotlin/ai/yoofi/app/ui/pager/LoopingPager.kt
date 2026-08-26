package ai.yoofi.app.ui.pager

import androidx.compose.foundation.pager.PagerState

/**
 * 二级 Tab / 嵌套页的无限循环滑动。
 * 虚页数为 [Int.MAX_VALUE]，真实下标 = virtualPage % itemCount。
 * 划过最左一页再左滑落到最后一页，划过最右再右滑落到第一页。
 */

fun loopingPageCount(itemCount: Int): Int =
    if (itemCount > 1) Int.MAX_VALUE else itemCount.coerceAtLeast(0)

fun loopingStartPage(itemCount: Int): Int {
    if (itemCount <= 1) return 0
    val mid = Int.MAX_VALUE / 2
    return mid - mid % itemCount
}

fun realPageIndex(virtualPage: Int, itemCount: Int): Int {
    if (itemCount <= 0) return 0
    return virtualPage.mod(itemCount)
}

/** 点击 Tab 时滚到当前虚页附近的对应真实页，避免跳回 0 打断循环。 */
suspend fun PagerState.animateToRealPage(realIndex: Int, itemCount: Int) {
    if (itemCount <= 0) return
    val target = realIndex.mod(itemCount)
    val delta = target - currentPage.mod(itemCount)
    animateScrollToPage(currentPage + delta)
}
