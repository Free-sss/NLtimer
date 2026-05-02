package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nltimer.feature.home.ui.sheet.NoteInputComponent

@Composable
fun ActivityNoteBoxDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            var noteText by remember { mutableStateOf("") }
            NoteInputComponent(
                note = noteText,
                onNoteChange = { noteText = it },
                onTopButton = { },
                onBottomButton = { }
            )
        }
    }
}
