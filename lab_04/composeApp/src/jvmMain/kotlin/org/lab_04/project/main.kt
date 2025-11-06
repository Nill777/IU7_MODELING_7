package org.lab_04.project

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "lab_04",
        state = rememberWindowState(width = 600.dp, height = 830.dp)
    ) {
        App()
    }
}