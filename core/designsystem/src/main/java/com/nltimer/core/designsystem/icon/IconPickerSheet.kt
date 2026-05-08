package com.nltimer.core.designsystem.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.core.designsystem.R
import java.text.BreakIterator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IconPickerSheet(
    currentIconKey: String?,
    onIconSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    defaultEmoji: String = "📖",
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialTab = if (IconKeyResolver.isMaterialIconOrNull(currentIconKey)) 1 else 0
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.icon_picker_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onIconSelected(null) }) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.icon_picker_reset),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val tabs = listOf(
                stringResource(R.string.icon_picker_tab_emoji),
                stringResource(R.string.icon_picker_tab_icons),
            )
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            when (selectedTab) {
                0 -> EmojiTab(
                    currentIconKey = currentIconKey,
                    defaultEmoji = defaultEmoji,
                    onIconSelected = {
                        onIconSelected(it)
                        onDismiss()
                    },
                )
                1 -> MaterialIconTab(
                    onIconSelected = {
                        onIconSelected(it)
                        onDismiss()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiTab(
    currentIconKey: String?,
    defaultEmoji: String,
    onIconSelected: (String?) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<EmojiCategory?>(null) }
    var manualInput by rememberSaveable { mutableStateOf("") }

    val allEmojis = remember { EmojiCategory.entries.flatMap { EmojiCatalog.findByCategory(it) } }
    val filteredEmojis = remember(searchQuery, selectedCategory) {
        val byCategory = selectedCategory?.let { EmojiCatalog.findByCategory(it) } ?: allEmojis
        if (searchQuery.isBlank()) byCategory else EmojiCatalog.searchEmoji(searchQuery).let { results ->
            if (selectedCategory != null) results.filter { it.category == selectedCategory } else results
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = stringResource(R.string.icon_picker_search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text(stringResource(R.string.emoji_category_frequent), style = MaterialTheme.typography.labelSmall) },
            )
            EmojiCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = if (selectedCategory == category) null else category },
                    label = { Text(emojiCategoryLabel(category), style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(filteredEmojis, key = { it.emoji + it.name }) { entry ->
                val isCurrent = currentIconKey == entry.emoji
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        .clickable { onIconSelected(entry.emoji) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = entry.emoji,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = manualInput,
                onValueChange = { raw ->
                    manualInput = truncateToCodePoints(raw, maxCodePoints = 4)
                },
                label = { Text(stringResource(R.string.icon_picker_manual_input)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (manualInput.isNotBlank()) {
                            onIconSelected(manualInput)
                        }
                    },
                ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    if (manualInput.isNotBlank()) {
                        onIconSelected(manualInput)
                    } else {
                        onIconSelected(currentIconKey ?: defaultEmoji)
                    }
                },
            ) {
                Text(stringResource(R.string.icon_picker_confirm))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MaterialIconTab(
    onIconSelected: (String?) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<IconCategory?>(null) }

    val allIcons = remember { MaterialIconCatalog.icons }
    val filteredIcons = remember(searchQuery, selectedCategory) {
        val byCategory = selectedCategory?.let { cat -> allIcons.filter { it.category == cat } } ?: allIcons
        if (searchQuery.isBlank()) byCategory else {
            val q = searchQuery.lowercase()
            byCategory.filter { entry ->
                entry.name.contains(q, ignoreCase = true) ||
                    entry.keywords.any { it.contains(q, ignoreCase = true) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = stringResource(R.string.icon_picker_search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text(stringResource(R.string.icon_category_action), style = MaterialTheme.typography.labelSmall) },
            )
            IconCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = if (selectedCategory == category) null else category },
                    label = { Text(iconCategoryLabel(category), style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(filteredIcons, key = { "${it.style}:${it.name}" }) { entry ->
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onIconSelected("mi:${entry.style}:${entry.name}") },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = entry.imageVectorProvider(),
                            contentDescription = entry.name,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    )
}

@Composable
private fun emojiCategoryLabel(category: EmojiCategory): String = when (category) {
    EmojiCategory.FREQUENT -> stringResource(R.string.emoji_category_frequent)
    EmojiCategory.EXPRESSIONS -> stringResource(R.string.emoji_category_expressions)
    EmojiCategory.GESTURES -> stringResource(R.string.emoji_category_gestures)
    EmojiCategory.ANIMALS -> stringResource(R.string.emoji_category_animals)
    EmojiCategory.FOOD -> stringResource(R.string.emoji_category_food)
    EmojiCategory.TRAVEL -> stringResource(R.string.emoji_category_travel)
    EmojiCategory.ACTIVITIES -> stringResource(R.string.emoji_category_activities)
    EmojiCategory.OBJECTS -> stringResource(R.string.emoji_category_objects)
    EmojiCategory.NATURE -> stringResource(R.string.emoji_category_nature)
    EmojiCategory.SYMBOLS -> stringResource(R.string.emoji_category_symbols)
}

@Composable
private fun iconCategoryLabel(category: IconCategory): String = when (category) {
    IconCategory.ACTION -> stringResource(R.string.icon_category_action)
    IconCategory.COMMUNICATION -> stringResource(R.string.icon_category_communication)
    IconCategory.CONTENT -> stringResource(R.string.icon_category_content)
    IconCategory.DEVICE -> stringResource(R.string.icon_category_device)
    IconCategory.IMAGE -> stringResource(R.string.icon_category_image)
    IconCategory.MAPS -> stringResource(R.string.icon_category_maps)
    IconCategory.NAVIGATION -> stringResource(R.string.icon_category_navigation)
    IconCategory.SOCIAL -> stringResource(R.string.icon_category_social)
    IconCategory.EDITOR -> stringResource(R.string.icon_category_editor)
    IconCategory.AV -> stringResource(R.string.icon_category_av)
    IconCategory.PLACES -> stringResource(R.string.icon_category_places)
    IconCategory.HARDWARE -> stringResource(R.string.icon_category_hardware)
}

private fun truncateToCodePoints(text: String, maxCodePoints: Int): String {
    val iterator = BreakIterator.getCharacterInstance()
    iterator.setText(text)
    val sb = StringBuilder()
    var count = 0
    var start = iterator.first()
    var end = iterator.next()
    while (end != BreakIterator.DONE && count < maxCodePoints) {
        sb.append(text, start, end)
        count++
        start = end
        end = iterator.next()
    }
    return sb.toString()
}
