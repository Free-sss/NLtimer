package com.nltimer.feature.settings.ui

import android.os.Build
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
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

@Composable
fun ThemeSettingsRoute(
    viewModel: ThemeSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val theme by viewModel.theme.collectAsState()
    ThemeSettingsScreen(
        theme = theme,
        onSeedColorChange = viewModel::onSeedColorChange,
        onThemeSwitch = viewModel::onThemeSwitch,
        onAmoledSwitch = viewModel::onAmoledSwitch,
        onPaletteChange = viewModel::onPaletteChange,
        onMaterialYouToggle = viewModel::onMaterialYouToggle,
        onFontChange = viewModel::onFontChange,
        onShowBordersToggle = viewModel::onShowBordersToggle,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showColorPicker by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = theme.seedColor,
            onSelect = onSeedColorChange,
            onDismiss = { showColorPicker = false }
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(text = "主题配置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 60.dp,
                start = padding.calculateLeftPadding(LocalLayoutDirection.current) + 16.dp,
                end = padding.calculateRightPadding(LocalLayoutDirection.current) + 16.dp,
            ),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // AppTheme picker
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
                            AppTheme.values().toList().forEach { appTheme ->
                                ToggleButton(
                                    checked = appTheme == theme.appTheme,
                                    onCheckedChange = { onThemeSwitch(appTheme) },
                                    modifier = Modifier.weight(1f),
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    ),
                                ) {
                                    Text(
                                        text = appTheme.toDisplayString(),
                                        modifier = Modifier.basicMarquee(),
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }

                    // Material You toggle (API 31+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                    }

                    // Font picker
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
                            Fonts.values().toList().forEach { font ->
                                ToggleButton(
                                    checked = theme.font == font,
                                    onCheckedChange = { onFontChange(font) },
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    ),
                                ) {
                                    Text(
                                        text = font.toDisplayString(),
                                        fontFamily = font.toFontRes()?.let { FontFamily(Font(it)) }
                                            ?: FontFamily.Default,
                                    )
                                }
                            }
                        }
                    }

                    // Amoled toggle
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

                    // Seed color picker
                    AnimatedVisibility(visible = !theme.isMaterialYou) {
                        ListItem(
                            headlineContent = { Text(text = "主题色") },
                            supportingContent = { Text(text = "选择应用的主色调") },
                            trailingContent = {
                                IconButton(
                                    onClick = { showColorPicker = true },
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
                            PaletteStyle.values().toList().forEach { style ->
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
    }
}
