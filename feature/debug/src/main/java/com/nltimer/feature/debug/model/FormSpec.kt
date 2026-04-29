package com.nltimer.feature.debug.model

data class FormSpec(
    val title: String,
    val submitLabel: String,
    val sections: List<FormSection>,
)

data class FormSection(
    val rows: List<FormRow>,
)

sealed class FormRow {
    data class TextInput(
        val key: String,
        val label: String,
        val placeholder: String,
        val initialValue: String = "",
    ) : FormRow()

    data class IconColor(
        val iconKey: String,
        val colorKey: String,
        val initialEmoji: String = "📖",
    ) : FormRow()

    data class LabelAction(
        val key: String,
        val label: String,
        val actionText: String,
        val showHelp: Boolean = false,
    ) : FormRow()
}
