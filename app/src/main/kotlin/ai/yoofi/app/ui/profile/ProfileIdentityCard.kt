package ai.yoofi.app.ui.profile

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiProfileStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 主态 / 客态共用的资料卡。
 * 差异走 slot：主态塞编辑、复制、Get VIP；客态塞关注加号，不往卡里塞 `isSelf`。
 */
@Composable
internal fun ProfileIdentityCard(
    identity: ProfileIdentity,
    modifier: Modifier = Modifier,
    nameAccessory: @Composable (() -> Unit)? = null,
    idTrailing: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    avatarBadge: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xCC110234), Color(0xCC261A42)),
                ),
            )
            .border(1.dp, YoofiProfileStroke, RoundedCornerShape(24.dp)),
    ) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .align(Alignment.CenterStart)
                .size(72.dp),
        ) {
            Image(
                painter = painterResource(identity.avatarRes),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            if (avatarBadge != null) {
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    avatarBadge()
                }
            }
        }
        Column(
            modifier = Modifier.padding(start = 94.dp, top = 16.dp, end = 20.dp),
        ) {
            // Get VIP 占右上时，姓名 / ID 给它留 68；客态没有 trailing 就铺满
            Column(modifier = if (trailing != null) Modifier.padding(end = 68.dp) else Modifier) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = identity.displayName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (nameAccessory != null) {
                        Spacer(Modifier.width(8.dp))
                        nameAccessory()
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_badge_fan),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFFC95D), Color(0xFFFF903A)),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "ID",
                            color = Color.White,
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = identity.publicId,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                    )
                    if (idTrailing != null) {
                        Spacer(Modifier.width(4.dp))
                        idTrailing()
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = identity.followingCount,
                    color = Color(0xFFDEDEDE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.me_following_label),
                    color = Color(0xFFB3B3B3),
                    fontSize = 12.sp,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = identity.followerCount,
                    color = Color(0xFFDEDEDE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.me_follower_label),
                    color = Color(0xFFB3B3B3),
                    fontSize = 12.sp,
                )
            }
        }
        if (trailing != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp, end = 20.dp),
            ) {
                trailing()
            }
        }
    }
}
