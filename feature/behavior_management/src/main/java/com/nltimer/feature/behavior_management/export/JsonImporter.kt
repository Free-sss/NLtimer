package com.nltimer.feature.behavior_management.export

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.behavior_management.model.ImportNewItem
import com.nltimer.feature.behavior_management.model.ImportPreview
import com.nltimer.feature.behavior_management.model.ImportPreviewItem
import com.nltimer.feature.behavior_management.model.NewItemType
import org.json.JSONArray
import org.json.JSONObject

object JsonImporter {

    fun parse(jsonString: String): BehaviorExportData {
        val root = JSONObject(jsonString)
        return parseExportData(root)
    }

    private fun parseExportData(root: JSONObject): BehaviorExportData {
        val version = root.optInt("version", 1)
        val exportedAt = root.optLong("exportedAt", 0L)

        val timeRange = root.optJSONObject("timeRange")?.let { tr ->
            TimeRangeInfo(
                start = tr.getString("start"),
                end = tr.getString("end"),
                label = tr.getString("label"),
            )
        }

        val filters = root.optJSONObject("filters")?.let { f ->
            FilterInfo(
                activityGroup = f.optString("activityGroup").ifEmpty { null },
                tagCategory = f.optString("tagCategory").ifEmpty { null },
                status = f.optString("status").ifEmpty { null },
            )
        }

        val behaviorsArray = root.optJSONArray("behaviors") ?: JSONArray()
        val behaviors = (0 until behaviorsArray.length()).map { i ->
            parseBehaviorItem(behaviorsArray.getJSONObject(i))
        }

        return BehaviorExportData(
            version = version,
            exportedAt = exportedAt,
            timeRange = timeRange,
            filters = filters,
            behaviors = behaviors,
        )
    }

    private fun parseBehaviorItem(json: JSONObject): BehaviorExportItem {
        val activityJson = json.getJSONObject("activity")
        val tagsArray = json.optJSONArray("tags") ?: JSONArray()

        return BehaviorExportItem(
            startTime = json.getLong("startTime"),
            endTime = json.optLong("endTime", 0L).let { if (it == 0L) null else it },
            status = json.getString("status"),
            note = json.optString("note").ifEmpty { null },
            pomodoroCount = json.optInt("pomodoroCount", 0),
            sequence = json.optInt("sequence", 0),
            estimatedDuration = json.optLong("estimatedDuration", 0L).let { if (it == 0L) null else it },
            actualDuration = json.optLong("actualDuration", 0L).let { if (it == 0L) null else it },
            achievementLevel = json.optInt("achievementLevel", -1).let { if (it == -1) null else it },
            wasPlanned = json.optBoolean("wasPlanned", false),
            activity = ActivityExportItem(
                name = activityJson.getString("name"),
                iconKey = activityJson.optString("iconKey").ifEmpty { null },
                color = activityJson.optLong("color", 0L).let { if (it == 0L) null else it },
            ),
            tags = (0 until tagsArray.length()).map { i ->
                val tagJson = tagsArray.getJSONObject(i)
                TagExportItem(
                    name = tagJson.getString("name"),
                    color = tagJson.optLong("color", 0L).let { if (it == 0L) null else it },
                )
            },
        )
    }

    fun analyzeDuplicates(
        data: BehaviorExportData,
        localActivities: List<Activity>,
        localTags: List<Tag>,
        existingBehaviors: List<Behavior>,
    ): ImportPreview {
        val activityNameMap = localActivities.associateBy { it.name }
        val tagNameMap = localTags.associateBy { it.name }

        val duplicateItems = mutableListOf<ImportPreviewItem>()
        val newItems = mutableListOf<ImportNewItem>()
        var duplicateCount = 0

        val missingActivities = mutableSetOf<String>()
        val missingTags = mutableSetOf<String>()

        for (item in data.behaviors) {
            val localActivity = activityNameMap[item.activity.name]
            if (localActivity == null) {
                missingActivities.add(item.activity.name)
                continue
            }
            val hasOverlap = existingBehaviors.any { existing ->
                existing.activityId == localActivity.id && timeOverlaps(
                    existing.startTime, existing.endTime,
                    item.startTime, item.endTime,
                )
            }
            if (hasOverlap) {
                duplicateCount++
                duplicateItems.add(ImportPreviewItem(
                    activityName = item.activity.name,
                    startTime = item.startTime,
                    endTime = item.endTime,
                ))
            }

            item.tags.forEach { tag ->
                if (tagNameMap[tag.name] == null) {
                    missingTags.add(tag.name)
                }
            }
        }

        missingActivities.forEach { newItems.add(ImportNewItem(NewItemType.ACTIVITY, it)) }
        missingTags.forEach { newItems.add(ImportNewItem(NewItemType.TAG, it)) }

        return ImportPreview(
            totalCount = data.behaviors.size,
            duplicateCount = duplicateCount,
            newCount = missingActivities.size + missingTags.size,
            duplicateItems = duplicateItems,
            newItems = newItems,
        )
    }

    private fun timeOverlaps(
        existingStart: Long, existingEnd: Long?,
        newStart: Long, newEnd: Long?,
    ): Boolean {
        val eEnd = existingEnd ?: Long.MAX_VALUE
        val nEnd = newEnd ?: Long.MAX_VALUE
        return newStart < eEnd && existingStart < nEnd
    }
}
