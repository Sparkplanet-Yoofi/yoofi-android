package ai.yoofi.app.ui.navigation

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiAccent
import ai.yoofi.app.ui.theme.YoofiAndroidTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 含渐变遮罩的底栏总高；系统导航条另加 padding */
private val BarStackHeight = 86.dp
private val CapsuleWidth = 350.dp
private val CapsuleHeight = 64.dp
private val CapsuleRadius = 16.dp
private val TabIcon = 26.dp
/** Figma 图标槽 28；文案比槽宽，按槽中心放置、允许溢出 */
private val TabSlotWidth = 28.dp
private val TabSlotTop = 10.dp
private val TabHomeStart = 20.dp
private val TabWorldStart = 114.dp
private val TabCreateStart = 208.dp
private val TabMeStart = 302.dp

/**
 * 共享底栏。不画 iOS Home Indicator，改走 [navigationBarsPadding]。
 */
@Composable
fun YoofiBottomBar(
    selected: YoofiTab,
    onTabSelected: (YoofiTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0.0975f to Color(0x00010101),
                    0.6675f to Color(0xFF010101),
                    1f to Color.Black,
                ),
            )
            .navigationBarsPadding()
            .height(BarStackHeight),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .width(CapsuleWidth)
                .height(CapsuleHeight)
                .clip(RoundedCornerShape(CapsuleRadius))
                .background(Color.Black.copy(alpha = 0.72f)),
        ) {
            TabItem(
                selected = selected == YoofiTab.Home,
                label = stringResource(R.string.tab_home),
                onClick = { onTabSelected(YoofiTab.Home) },
                modifier = Modifier.tabSlot(TabHomeStart),
            ) {
                Image(
                    painter = painterResource(
                        if (selected == YoofiTab.Home) {
                            R.drawable.ic_nav_house_active
                        } else {
                            R.drawable.ic_nav_house
                        },
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(TabIcon),
                    contentScale = ContentScale.Fit,
                )
            }
            TabItem(
                selected = selected == YoofiTab.World,
                label = stringResource(R.string.tab_world),
                onClick = { onTabSelected(YoofiTab.World) },
                modifier = Modifier.tabSlot(TabWorldStart),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_nav_planet),
                    contentDescription = null,
                    modifier = Modifier.size(TabIcon),
                    contentScale = ContentScale.Fit,
                )
            }
            TabItem(
                selected = selected == YoofiTab.Create,
                label = stringResource(R.string.tab_create),
                onClick = { onTabSelected(YoofiTab.Create) },
                modifier = Modifier.tabSlot(TabCreateStart),
            ) {
                CreateTabIcon()
            }
            TabItem(
                selected = selected == YoofiTab.Me,
                label = stringResource(R.string.tab_me),
                onClick = { onTabSelected(YoofiTab.Me) },
                modifier = Modifier.tabSlot(TabMeStart),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_nav_me),
                    contentDescription = null,
                    modifier = Modifier.size(TabIcon),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .wrapContentWidth()
            .alpha(if (selected) 1f else 0.4f)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) YoofiAccent else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}

/**
 * 把 Tab 的水平中心对齐到 Figma 28 槽的中心，文案可向两侧溢出。
 * 测量时放开最大宽度，避免再被 28dp 槽裁成 Hom / Wor / Crea。
 */
private fun Modifier.tabSlot(slotStart: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity),
    )
    val centerX = (slotStart + TabSlotWidth / 2).roundToPx()
    val x = (centerX - placeable.width / 2).coerceAtLeast(0)
    val y = TabSlotTop.roundToPx()
    layout(width = x + placeable.width, height = y + placeable.height) {
        placeable.place(x = x, y = y)
    }
}

@Composable
private fun CreateTabIcon() {
    // Figma Create：26 框内三颗星 (2,6) 14 / (15.5,16.5) 6 / (15.5,4.5) 4
    Box(modifier = Modifier.size(TabIcon)) {
        Image(
            painter = painterResource(R.drawable.ic_nav_star_lg),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 2.dp, y = 6.dp)
                .size(14.dp),
            contentScale = ContentScale.Fit,
        )
        Image(
            painter = painterResource(R.drawable.ic_nav_star_md),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 15.5.dp, y = 16.5.dp)
                .size(6.dp),
            contentScale = ContentScale.Fit,
        )
        Image(
            painter = painterResource(R.drawable.ic_nav_star_sm),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 15.5.dp, y = 4.5.dp)
                .size(4.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(widthDp = 390, heightDp = 120, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun YoofiBottomBarPreview() {
    YoofiAndroidTheme(darkTheme = true, dynamicColor = false) {
        YoofiBottomBar(selected = YoofiTab.World, onTabSelected = {})
    }
}
