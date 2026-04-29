package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.nltimer.core.designsystem.R

/**
 * 颜色选择器对话框，提供 HSV 色相环、亮度滑块和 Alpha 预览
 * @param initialColor 弹窗打开时的初始颜色
 * @param onSelect 用户确认选择颜色后的回调
 * @param onDismiss 关闭弹窗的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onSelect: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // 初始化颜色选择器控制器，管理 HSV/亮度/Alpha 状态
    val controller = rememberColorPickerController()

    // 使用 Material3 基础弹窗容器
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.wrapContentSize()
        ) {
            // 垂直排列所有控件，间距 16dp
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 弹窗标题
                Text(
                    text = "选择主题色",
                    style = MaterialTheme.typography.headlineSmall
                )

                // HSV 色相-饱和度选择面板
                HsvColorPicker(
                    modifier = Modifier
                        .size(250.dp),
                    initialColor = initialColor,
                    controller = controller,
                )

                // 亮度调节滑块
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    initialColor = initialColor,
                    controller = controller,
                )

                // Alpha 透明度预览瓦片
                AlphaTile(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    controller = controller,
                )

                // 底部操作按钮行：取消 / 确认
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            // 读取控制器当前选中的颜色并回调
                            onSelect(controller.selectedColor.value)
                            onDismiss()
                        }
                    ) {
                        Text(text = stringResource(R.string.done))
                    }
                }
            }
        }
    }
}
