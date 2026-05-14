package com.nltimer.feature.settings.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.materialkolor.dynamicColorScheme
import com.nltimer.core.designsystem.theme.LocalTheme
import com.nltimer.core.designsystem.theme.toMPaletteStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 色板预览页路由：基于当前主题种子色与配色方案，
 * 生成亮/暗两套 MD3 ColorScheme，供开发者并排观察 token 在两种模式下的实际效果。
 */
@Composable
fun ColorPaletteRoute() {
    val theme = LocalTheme.current
    val schemes by produceState<Pair<ColorScheme, ColorScheme>?>(
        initialValue = null,
        key1 = theme.seedColor,
        key2 = theme.paletteStyle,
        key3 = theme.isAmoled,
    ) {
        value = withContext(Dispatchers.Default) {
            val style = theme.paletteStyle.toMPaletteStyle()
            val light = dynamicColorScheme(
                seedColor = theme.seedColor,
                isDark = false,
                isAmoled = theme.isAmoled,
                style = style,
            )
            val dark = dynamicColorScheme(
                seedColor = theme.seedColor,
                isDark = true,
                isAmoled = theme.isAmoled,
                style = style,
            )
            light to dark
        }
    }

    ColorPaletteScreen(
        seedColor = theme.seedColor,
        schemes = schemes,
    )
}

@Composable
fun ColorPaletteScreen(
    seedColor: Color,
    schemes: Pair<ColorScheme, ColorScheme>?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val onCopy: (String) -> Unit = remember(clipboard, context) {
        { hex ->
            clipboard.setText(AnnotatedString(hex))
            Toast.makeText(context, "已复制 $hex", Toast.LENGTH_SHORT).show()
        }
    }

    SettingsSubpageContainer(modifier = modifier) {
        item(key = "header-seed") {
            Text(
                text = "基于当前种子色 ${seedColor.toHex()}（在主题配置中修改）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        if (schemes != null) {
            paletteContent(
                lightScheme = schemes.first,
                darkScheme = schemes.second,
                onCopy = onCopy,
            )
        }
    }
}

private fun LazyListScope.paletteContent(
    lightScheme: ColorScheme,
    darkScheme: ColorScheme,
    onCopy: (String) -> Unit,
) {
    PALETTE_GROUPS.forEach { group ->
        item(key = "section-${group.title}") {
            val titleStyle = MaterialTheme.typography.titleMedium
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = group.title,
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = group.description,
                    style = titleStyle.copy(fontSize = titleStyle.fontSize * 0.8f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        items(group.tokens, key = { "token-${it.name}" }) { token ->
            TokenCard(
                token = token,
                lightScheme = lightScheme,
                darkScheme = darkScheme,
                onCopy = onCopy,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun TokenCard(
    token: PaletteToken,
    lightScheme: ColorScheme,
    darkScheme: ColorScheme,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lightContainer = token.container(lightScheme)
    val darkContainer = token.container(darkScheme)
    val lightText = token.onContainer?.invoke(lightScheme) ?: lightScheme.onSurface
    val darkText = token.onContainer?.invoke(darkScheme) ?: darkScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = token.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ColorSwatch(
                label = "Light",
                container = lightContainer,
                textColor = lightText,
                onCopy = onCopy,
                modifier = Modifier.weight(1f),
            )
            ColorSwatch(
                label = "Dark",
                container = darkContainer,
                textColor = darkText,
                onCopy = onCopy,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColorSwatch(
    label: String,
    container: Color,
    textColor: Color,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hex = remember(container) { container.toHex() }
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(container)
            .combinedClickable(
                onClick = {},
                onLongClick = { onCopy(hex) },
            )
            .padding(10.dp),
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f),
            )
            Text(
                text = hex,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
        }
    }
}

private data class PaletteToken(
    val name: String,
    val container: (ColorScheme) -> Color,
    val onContainer: ((ColorScheme) -> Color)? = null,
)

private data class PaletteGroup(
    val title: String,
    val description: String,
    val tokens: List<PaletteToken>,
)

private val PALETTE_GROUPS: List<PaletteGroup> = listOf(
    PaletteGroup(
        title = "Primary",
        description = "主品牌色 · 主要按钮、选中状态、强调元素",
        tokens = listOf(
            PaletteToken("primary", { it.primary }, { it.onPrimary }),
            PaletteToken("primaryContainer", { it.primaryContainer }, { it.onPrimaryContainer }),
            PaletteToken("inversePrimary", { it.inversePrimary }),
        ),
    ),
    PaletteGroup(
        title = "Secondary",
        description = "次要品牌色 · 浮动按钮、辅助强调",
        tokens = listOf(
            PaletteToken("secondary", { it.secondary }, { it.onSecondary }),
            PaletteToken("secondaryContainer", { it.secondaryContainer }, { it.onSecondaryContainer }),
        ),
    ),
    PaletteGroup(
        title = "Tertiary",
        description = "三级品牌色 · 平衡色彩、装饰性强调",
        tokens = listOf(
            PaletteToken("tertiary", { it.tertiary }, { it.onTertiary }),
            PaletteToken("tertiaryContainer", { it.tertiaryContainer }, { it.onTertiaryContainer }),
        ),
    ),
    PaletteGroup(
        title = "Fixed Colors",
        description = "亮/暗模式下保持一致 · 地图、徽章等不随主题切换的场景",
        tokens = listOf(
            PaletteToken("primaryFixed", { it.primaryFixed }, { it.onPrimaryFixed }),
            PaletteToken("primaryFixedDim", { it.primaryFixedDim }, { it.onPrimaryFixedVariant }),
            PaletteToken("secondaryFixed", { it.secondaryFixed }, { it.onSecondaryFixed }),
            PaletteToken("secondaryFixedDim", { it.secondaryFixedDim }, { it.onSecondaryFixedVariant }),
            PaletteToken("tertiaryFixed", { it.tertiaryFixed }, { it.onTertiaryFixed }),
            PaletteToken("tertiaryFixedDim", { it.tertiaryFixedDim }, { it.onTertiaryFixedVariant }),
        ),
    ),
    PaletteGroup(
        title = "Background & Surface",
        description = "页面与表面背景 · 卡片、面板、列表底色",
        tokens = listOf(
            PaletteToken("background", { it.background }, { it.onBackground }),
            PaletteToken("surface", { it.surface }, { it.onSurface }),
            PaletteToken("surfaceVariant", { it.surfaceVariant }, { it.onSurfaceVariant }),
            PaletteToken("surfaceTint", { it.surfaceTint }),
            PaletteToken("inverseSurface", { it.inverseSurface }, { it.inverseOnSurface }),
            PaletteToken("surfaceContainerLowest", { it.surfaceContainerLowest }),
            PaletteToken("surfaceContainerLow", { it.surfaceContainerLow }),
            PaletteToken("surfaceContainer", { it.surfaceContainer }),
            PaletteToken("surfaceContainerHigh", { it.surfaceContainerHigh }),
            PaletteToken("surfaceContainerHighest", { it.surfaceContainerHighest }),
            PaletteToken("surfaceBright", { it.surfaceBright }),
            PaletteToken("surfaceDim", { it.surfaceDim }),
        ),
    ),
    PaletteGroup(
        title = "Error",
        description = "错误与警示状态 · 错误提示、危险操作",
        tokens = listOf(
            PaletteToken("error", { it.error }, { it.onError }),
            PaletteToken("errorContainer", { it.errorContainer }, { it.onErrorContainer }),
        ),
    ),
    PaletteGroup(
        title = "Other",
        description = "装饰与辅助 · 边框、分隔线、遮罩层",
        tokens = listOf(
            PaletteToken("outline", { it.outline }),
            PaletteToken("outlineVariant", { it.outlineVariant }),
            PaletteToken("scrim", { it.scrim }),
        ),
    ),
)

private fun Color.toHex(): String {
    val argb = this.toArgb()
    return if (this.alpha < 1f) {
        "#%08X".format(argb)
    } else {
        "#%06X".format(argb and 0xFFFFFF)
    }
}
