package com.nltimer.feature.behavior_management.export

import org.json.JSONArray
import org.json.JSONObject

object JsonExporter {

    fun export(
        data: BehaviorExportData,
    ): String {
        val root = JSONObject().apply {
            put("version", data.version)
            put("exportedAt", data.exportedAt)

            data.timeRange?.let { tr ->
                put("timeRange", JSONObject().apply {
                    put("start", tr.start)
                    put("end", tr.end)
                    put("label", tr.label)
                })
            }

            data.filters?.let { f ->
                put("filters", JSONObject().apply {
                    f.activityGroup?.let { put("activityGroup", it) }
                    f.tagCategory?.let { put("tagCategory", it) }
                    f.status?.let { put("status", it) }
                })
            }

            val behaviorsArray = JSONArray()
            data.behaviors.forEach { item ->
                behaviorsArray.put(behaviorItemToJson(item))
            }
            put("behaviors", behaviorsArray)
        }

        return root.toString(2)
    }

    private fun behaviorItemToJson(item: BehaviorExportItem): JSONObject {
        return JSONObject().apply {
            put("startTime", item.startTime)
            item.endTime?.let { put("endTime", it) }
            put("status", item.status)
            item.note?.let { put("note", it) }
            put("pomodoroCount", item.pomodoroCount)
            put("sequence", item.sequence)
            item.estimatedDuration?.let { put("estimatedDuration", it) }
            item.actualDuration?.let { put("actualDuration", it) }
            item.achievementLevel?.let { put("achievementLevel", it) }
            put("wasPlanned", item.wasPlanned)
            put("activity", JSONObject().apply {
                put("name", item.activity.name)
                item.activity.iconKey?.let { put("iconKey", it) }
                item.activity.color?.let { put("color", it) }
            })
            val tagsArray = JSONArray()
            item.tags.forEach { tag ->
                tagsArray.put(JSONObject().apply {
                    put("name", tag.name)
                    tag.color?.let { put("color", it) }
                })
            }
            put("tags", tagsArray)
        }
    }

    fun fromBehaviors(
        behaviors: List<com.nltimer.core.data.model.BehaviorWithDetails>,
        timeRangeLabel: String,
        startTime: Long?,
        endTime: Long?,
        activityGroup: String?,
        tagCategory: String?,
        status: String?,
    ): BehaviorExportData {
        return BehaviorExportData(
            exportedAt = System.currentTimeMillis(),
            timeRange = if (startTime != null && endTime != null) {
                TimeRangeInfo(startTime.toString(), endTime.toString(), timeRangeLabel)
            } else null,
            filters = FilterInfo(activityGroup, tagCategory, status),
            behaviors = behaviors.map { bwd ->
                BehaviorExportItem(
                    startTime = bwd.behavior.startTime,
                    endTime = bwd.behavior.endTime,
                    status = bwd.behavior.status.key,
                    note = bwd.behavior.note,
                    pomodoroCount = bwd.behavior.pomodoroCount,
                    sequence = bwd.behavior.sequence,
                    estimatedDuration = bwd.behavior.estimatedDuration,
                    actualDuration = bwd.behavior.actualDuration,
                    achievementLevel = bwd.behavior.achievementLevel,
                    wasPlanned = bwd.behavior.wasPlanned,
                    activity = ActivityExportItem(
                        name = bwd.activity.name,
                        iconKey = bwd.activity.iconKey,
                        color = bwd.activity.color,
                    ),
                    tags = bwd.tags.map { TagExportItem(name = it.name, color = it.color) },
                )
            },
        )
    }
}
