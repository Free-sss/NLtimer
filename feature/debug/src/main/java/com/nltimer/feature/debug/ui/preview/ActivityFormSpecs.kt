package com.nltimer.feature.debug.ui.preview

import com.nltimer.feature.debug.model.FormRow
import com.nltimer.feature.debug.model.FormSection
import com.nltimer.feature.debug.model.FormSpec

object ActivityFormSpecs {
    val create = FormSpec(
        title = "增加活动",
        submitLabel = "增加活动",
        sections = listOf(
            FormSection(
                rows = listOf(
                    FormRow.IconColor(iconKey = "emoji", colorKey = "color", initialEmoji = "📖"),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.TextInput(key = "name", label = "名称", placeholder = "请输入"),
                    FormRow.TextInput(key = "note", label = "备注", placeholder = "请输入"),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.LabelAction(key = "tags", label = "关联标签", actionText = "+ 增加", showHelp = true),
                    FormRow.LabelAction(key = "keywords", label = "关键词", actionText = "+ 增加", showHelp = true),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.LabelAction(key = "category", label = "所属分类", actionText = "未分类"),
                ),
            ),
        ),
    )

    val createTag = FormSpec(
        title = "新增标签",
        submitLabel = "新增标签",
        sections = listOf(
            FormSection(
                rows = listOf(
                    FormRow.IconColor(iconKey = "emoji", colorKey = "color", initialEmoji = "🏷️"),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.TextInput(key = "name", label = "名称", placeholder = "请输入标签名"),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.LabelAction(key = "category", label = "所属分类", actionText = "未分类"),
                ),
            ),
        ),
    )

    fun editActivity() = FormSpec(
        title = "编辑活动",
        submitLabel = "保存",
        sections = create.sections,
    )

    fun editTag() = FormSpec(
        title = "编辑标签",
        submitLabel = "保存",
        sections = createTag.sections,
    )
}
