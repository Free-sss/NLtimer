package com.nltimer.core.designsystem.form

data class FormSpec(
    val title: String,
    val submitLabel: String,
    val sections: List<FormSection>,
) {
    fun defaultValues(): Map<String, String> = buildMap {
        sections.forEach { section ->
            section.rows.forEach { row ->
                when (row) {
                    is FormRow.TextInput -> put(row.key, row.initialValue)
                    is FormRow.IconColor -> {
                        put(row.iconKey, row.initialEmoji)
                        put(row.colorKey, "")
                    }
                    is FormRow.LabelAction -> {}
                    is FormRow.Switch -> put(row.key, row.initialChecked.toString())
                    is FormRow.NumberInput -> put(row.key, row.initialValue.toString())
                }
            }
        }
    }
}

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
        val onClick: (() -> Unit)? = null,
    ) : FormRow()

    data class Switch(
        val key: String,
        val label: String,
        val initialChecked: Boolean = false,
    ) : FormRow()

    data class NumberInput(
        val key: String,
        val label: String,
        val initialValue: Int = 0,
        val range: IntRange = 0..99,
    ) : FormRow()
}

fun parseColorHex(colorHex: String?): Long? = colorHex?.let {
    try { it.toULong(16).toLong() } catch (_: Exception) { null }
}

fun FormSpec.withUpdatedLabelAction(key: String, actionText: String): FormSpec =
    copy(
        sections = sections.map { section ->
            section.copy(
                rows = section.rows.map { row ->
                    if (row is FormRow.LabelAction && row.key == key) {
                        row.copy(actionText = actionText)
                    } else {
                        row
                    }
                }
            )
        }
    )
