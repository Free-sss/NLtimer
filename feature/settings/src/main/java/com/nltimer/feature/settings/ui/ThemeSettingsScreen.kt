package com.nltimer.feature.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.materialkolor.rememberDynamicColorScheme
import com.nltimer.core.designsystem.component.ColorPickerDialog
import com.nltimer.core.designsystem.theme.AppTheme
import com.nltimer.core.designsystem.theme.Fonts
import com.nltimer.core.designsystem.theme.PaletteStyle
import com.nltimer.core.designsystem.theme.Theme
import com.nltimer.core.designsystem.theme.endItemShape
import com.nltimer.core.designsystem.theme.leadingItemShape
import com.nltimer.core.designsystem.theme.listItemColors
import com.nltimer.core.designsystem.theme.middleItemShape
import com.nltimer.core.designsystem.theme.toDisplayString
import com.nltimer.core.designsystem.theme.toFontRes
import com.nltimer.core.designsystem.theme.toMPaletteStyle

/**
 * 主题设置页路由入口，负责初始化ViewModel并将状态与回调绑定到UI层。
 * @param viewModel 主题设置ViewModel，通过Hilt自动注入
 * @param onNavigateBack 返回上一级页面的导航回调
 */
@Composable
fun ThemeSettingsRoute(
    viewModel: ThemeSettingsViewModel = hiltViewModel(),
) {
    // 从ViewModel中收集主题状态，重组时自动订阅
    val theme by viewModel.theme.collectAsState()
    // 将ViewModel的方法指针传入UI层，分离逻辑与视图
    ThemeSettingsScreen(
        theme = theme,
        onSeedColorChange = viewModel::onSeedColorChange,
        onThemeSwitch = viewModel::onThemeSwitch,
        onAmoledSwitch = viewModel::onAmoledSwitch,
        onPaletteChange = viewModel::onPaletteChange,
        onMaterialYouToggle = viewModel::onMaterialYouToggle,
        onFontChange = viewModel::onFontChange,
        onShowBordersToggle = viewModel::onShowBordersToggle,
    )
}

/**
 * 主题设置页面主界面，提供主题模式、Material You、字体、AMOLED纯黑、
 * 种子颜色、配色方案、边框显示等设置项。
 * @param theme 当前主题配置
 * @param onSeedColorChange 种子颜色变更回调
 * @param onThemeSwitch 主题模式（亮/暗/跟随系统）切换回调
 * @param onAmoledSwitch AMOLED纯黑模式开关回调
 * @param onPaletteChange 配色方案样式变更回调
 * @param onMaterialYouToggle Material You动态取色开关回调
 * @param onFontChange 字体选择变更回调
 * @param onShowBordersToggle 网格卡片边框显示开关回调
 * @param onNavigateBack 导航返回回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    theme: Theme,
    onSeedColorChange: (Color) -> Unit,
    onThemeSwitch: (AppTheme) -> Unit,
    onAmoledSwitch: (Boolean) -> Unit,
    onPaletteChange: (PaletteStyle) -> Unit,
    onMaterialYouToggle: (Boolean) -> Unit,
    onFontChange: (Fonts) -> Unit,
    onShowBordersToggle: (Boolean) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.background,
    modifier: Modifier = Modifier,
) {
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = theme.seedColor,
            onSelect = onSeedColorChange,
            onDismiss = { showColorPicker = false }
        )
    }

    SettingsSubpageContainer(
        containerColor = containerColor,
        modifier = modifier,
    ) {
        ThemeSettingsContent(
            theme = theme,
            onSeedColorChange = onSeedColorChange,
            onThemeSwitch = onThemeSwitch,
            onAmoledSwitch = onAmoledSwitch,
            onPaletteChange = onPaletteChange,
            onMaterialYouToggle = onMaterialYouToggle,
            onFontChange = onFontChange,
            onShowBordersToggle = onShowBordersToggle,
            showColorPicker = showColorPicker,
            onShowColorPicker = { showColorPicker = it },
        )
    }
}

private fun LazyListScope.ThemeSettingsContent(
    theme: Theme,
    onSeedColorChange: (Color) -> Unit,
    onThemeSwitch: (AppTheme) -> Unit,
    onAmoledSwitch: (Boolean) -> Unit,
    onPaletteChange: (PaletteStyle) -> Unit,
    onMaterialYouToggle: (Boolean) -> Unit,
    onFontChange: (Fonts) -> Unit,
    onShowBordersToggle: (Boolean) -> Unit,
    showColorPicker: Boolean,
    onShowColorPicker: (Boolean) -> Unit,
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // 主题模式切换区：使用FilterChip选择亮/暗/跟随系统
                    Column(modifier = Modifier.clip(leadingItemShape())) {
                        ListItem(
                            headlineContent = { Text(text = "主题模式") },
                            colors = listItemColors(),
                            leadingContent = {
                                Icon(
                                    imageVector = when (theme.appTheme) {
                                        AppTheme.SYSTEM -> if (isSystemInDarkTheme())
                                            Icons.Default.DarkMode
                                        else
                                            Icons.Default.LightMode
                                        AppTheme.DARK -> Icons.Default.DarkMode
                                        AppTheme.LIGHT -> Icons.Default.LightMode
                                    },
                                    contentDescription = null,
                                )
                            },
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(listItemColors().containerColor)
                                .padding(start = 52.dp, end = 16.dp, bottom = 8.dp),
                        ) {
                            AppTheme.entries.toList().forEach { appTheme ->
                                FilterChip(
                                    selected = appTheme == theme.appTheme,
                                    onClick = { onThemeSwitch(appTheme) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    ),
                                    label = {
                                        Text(
                                            text = appTheme.toDisplayString(),
                                            modifier = Modifier.basicMarquee(),
                                            maxLines = 1,
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Material You 开关：开启后使用系统壁纸动态取色（需要API 31+）
                    ListItem(
                        headlineContent = { Text(text = "Material You") },
                        supportingContent = { Text(text = "使用系统壁纸颜色") },
                        trailingContent = {
                            Switch(
                                checked = theme.isMaterialYou,
                                onCheckedChange = { onMaterialYouToggle(it) },
                            )
                        },
                        colors = listItemColors(),
                        modifier = Modifier.clip(middleItemShape()),
                    )

                    // 字体选择区：列出所有可用字体，选中项高亮，预览时应用实际字体
                    Column(modifier = Modifier.clip(middleItemShape())) {
                        ListItem(
                            headlineContent = { Text(text = "字体") },
                            colors = listItemColors(),
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.FontDownload,
                                    contentDescription = null,
                                )
                            },
                        )
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(listItemColors().containerColor)
                                .padding(start = 52.dp, end = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Fonts.entries.toList().forEach { font ->
                                FilterChip(
                                    selected = theme.font == font,
                                    onClick = { onFontChange(font) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    ),
                                    label = {
                                        Text(
                                            text = font.toDisplayString(),
                                            fontFamily = font.toFontRes()?.let { FontFamily(Font(it)) }
                                                ?: FontFamily.Default,
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // AMOLED纯黑开关：深色模式下将背景替换为纯黑以节省屏幕功耗
                    ListItem(
                        headlineContent = { Text(text = "AMOLED 纯黑") },
                        supportingContent = { Text(text = "在深色模式下使用纯黑背景") },
                        trailingContent = {
                            Switch(
                                checked = theme.isAmoled,
                                onCheckedChange = { onAmoledSwitch(it) },
                            )
                        },
                        colors = listItemColors(),
                        modifier = Modifier.clip(middleItemShape()),
                    )

                    // 种子颜色选择器：仅当Material You关闭时可见，点击弹出颜色选择对话框
                    AnimatedVisibility(visible = !theme.isMaterialYou) {
                        ListItem(
                            headlineContent = { Text(text = "主题色") },
                            supportingContent = { Text(text = "选择应用的主色调") },
                            trailingContent = {
                                IconButton(
                                    onClick = { onShowColorPicker(true) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = theme.seedColor,
                                        contentColor = contentColorFor(theme.seedColor),
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Colorize,
                                        contentDescription = "选择颜色",
                                    )
                                }
                            },
                            colors = listItemColors(),
                            modifier = Modifier.clip(middleItemShape()),
                        )
                    }

                    // Palette style picker
                    Column(modifier = Modifier.clip(middleItemShape())) {
                        ListItem(
                            headlineContent = { Text(text = "配色方案") },
                            colors = listItemColors(),
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                )
                            },
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(listItemColors().containerColor)
                                .padding(start = 52.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            PaletteStyle.entries.toList().forEach { style ->
                                val scheme = rememberDynamicColorScheme(
                                    primary = theme.seedColor,
                                    isDark = when (theme.appTheme) {
                                        AppTheme.SYSTEM -> isSystemInDarkTheme()
                                        AppTheme.DARK -> true
                                        AppTheme.LIGHT -> false
                                    },
                                    isAmoled = theme.isAmoled,
                                    style = style.toMPaletteStyle(),
                                )
                                val selected = theme.paletteStyle == style

                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .clickable { onPaletteChange(style) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Canvas(modifier = Modifier.matchParentSize()) {
                                        val colors: List<Color> = listOf(
                                            scheme.primary,
                                            scheme.primaryContainer,
                                            scheme.secondary,
                                            scheme.secondaryContainer,
                                            scheme.tertiary,
                                            scheme.tertiaryContainer,
                                        )
                                        val sweepAngle = 360f / colors.size
                                        colors.forEachIndexed { index: Int, color: Color ->
                                            drawArc(
                                                color = color,
                                                startAngle = index * sweepAngle,
                                                sweepAngle = sweepAngle,
                                                useCenter = true,
                                            )
                                        }
                                    }

                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(
                                                    scheme.primary.copy(alpha = 0.7f)
                                                )
                                        )
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = scheme.onPrimary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Border toggle
                    ListItem(
                        headlineContent = { Text(text = "显示边框") },
                        supportingContent = { Text(text = "在网格卡片上显示轮廓边框") },
                        trailingContent = {
                            Switch(
                                checked = theme.showBorders,
                                onCheckedChange = { onShowBordersToggle(it) },
                            )
                        },
                        colors = listItemColors(),
                        modifier = Modifier.clip(endItemShape()),
                    )
        }
    }
}
