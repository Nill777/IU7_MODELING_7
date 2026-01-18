package org.lab_05.project

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "lab_05",
        state = rememberWindowState(width = 656.dp, height = 680.dp)
    ) {
        App()
    }
}