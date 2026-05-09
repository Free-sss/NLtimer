package com.nltimer.core.designsystem.component

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.R
import kotlinx.coroutines.launch

private data class IntroPage(
    val backgroundColor: Color,
    val icon: String,
    val title: String,
    val subtitle: String,
)

private val introPages = listOf(
    IntroPage(
        backgroundColor = Color(0xFF093A8F),
        icon = "👋",
        title = "欢迎",
        subtitle = "开始你的行为追踪之旅",
    ),
    IntroPage(
        backgroundColor = Color(0xFF1565C0),
        icon = "📋",
        title = "创建活动",
        subtitle = "定义你的日常行为类型",
    ),
    IntroPage(
        backgroundColor = Color(0xFF00695C),
        icon = "⏱",
        title = "开始计时",
        subtitle = "记录每一段行为时间",
    ),
    IntroPage(
        backgroundColor = Color(0xFF4A148C),
        icon = "📊",
        title = "查看统计",
        subtitle = "洞察你的时间分配",
    ),
    IntroPage(
        backgroundColor = Color(0xFFE65100),
        icon = "🎨",
        title = "个性化主题",
        subtitle = "种子色、风格、表达力",
    ),
    IntroPage(
        backgroundColor = Color(0xFF1B5E20),
        icon = "🚀",
        title = "准备就绪",
        subtitle = "开始记录你的第一天",
    ),
)

@Composable
fun AppIntroScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { introPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == introPages.size - 1

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            IntroPageContent(introPage = introPages[page])
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PageIndicator(
                pageCount = introPages.size,
                currentPage = pagerState.currentPage,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onFinish) {
                    Text("跳过")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                ) {
                    Text(if (isLastPage) "开始" else "下一步")
                }
            }
        }
    }
}

@Composable
private fun IntroPageContent(introPage: IntroPage) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(introPage.backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = introPage.icon,
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = introPage.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily(Font(R.font.dm_serif_text)),
                ),
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = introPage.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentPage) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) Color.White
                        else Color.White.copy(alpha = 0.4f),
                    ),
            )
        }
    }
}
