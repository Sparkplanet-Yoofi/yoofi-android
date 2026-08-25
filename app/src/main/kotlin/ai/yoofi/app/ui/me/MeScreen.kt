package ai.yoofi.app.ui.me

import ai.yoofi.app.R
import ai.yoofi.app.ui.theme.YoofiProfileStroke
import ai.yoofi.app.ui.theme.YoofiVipText
import ai.yoofi.app.ui.theme.YoofiandroidTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 我的-主态，对齐 Figma `982:13174`。
 */
@Composable
fun MeScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onPreviewProfile: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Image(
            painter = painterResource(R.drawable.img_me_header_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.2f,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 120.dp),
        ) {
            MeTopBar(
                onSettingsClick = onSettingsClick,
                onPreviewProfile = onPreviewProfile,
            )
            ProfileCard(modifier = Modifier.padding(horizontal = 20.dp, vertical = 1.dp))
            Spacer(Modifier.height(16.dp))
            LorebookTabs(modifier = Modifier.padding(horizontal = 20.dp))
        }
    }
}

@Composable
private fun MeTopBar(
    onSettingsClick: () -> Unit,
    onPreviewProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_settings_hex),
            contentDescription = stringResource(R.string.cd_settings),
            modifier = Modifier
                .size(24.dp)
                .clickable(role = Role.Button, onClick = onSettingsClick),
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                .clickable(role = Role.Button, onClick = onPreviewProfile)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_preview_card),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.me_preview_profile),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ProfileCard(modifier: Modifier = Modifier) {
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
        Image(
            painter = painterResource(R.drawable.img_me_avatar),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 12.dp)
                .align(Alignment.CenterStart)
                .size(72.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .padding(start = 94.dp, top = 16.dp, end = 88.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.me_display_name),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                Image(
                    painter = painterResource(R.drawable.ic_edit_pencil),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
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
                    text = stringResource(R.string.me_user_id),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                )
                Spacer(Modifier.width(4.dp))
                Image(
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = stringResource(R.string.cd_copy_id),
                    modifier = Modifier.size(12.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.me_following_count),
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
                    text = stringResource(R.string.me_follower_count),
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
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFA4B5FF),
                            Color(0xFFD9F3FF),
                            Color(0xFFE5DDFF),
                            Color(0xFFB951DE),
                        ),
                    ),
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.me_get_vip),
                color = YoofiVipText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
            )
            Spacer(Modifier.width(4.dp))
            Image(
                painter = painterResource(R.drawable.ic_vip_caret),
                contentDescription = null,
                modifier = Modifier
                    .size(7.dp, 6.dp)
                    .rotate(90f),
            )
        }
    }
}

@Composable
private fun LorebookTabs(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.me_tab_lorebook),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.me_tab_creations),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                )
                Spacer(Modifier.width(2.dp))
                Image(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(start = 24.dp, top = 22.dp)
                .size(16.dp, 4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MeScreenPreview() {
    YoofiandroidTheme(darkTheme = true, dynamicColor = false) {
        MeScreen()
    }
}
