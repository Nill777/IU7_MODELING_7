package org.lab_06.project

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "lab_06",
        state = rememberWindowState(width = 972.dp, height = 930.dp)
    ) {
        App()
    }
}