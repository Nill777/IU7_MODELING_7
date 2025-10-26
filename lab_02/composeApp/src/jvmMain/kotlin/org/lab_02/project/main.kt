package org.lab_02.project

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "lab_02",
        state = WindowState(width = 1000.dp, height = 950.dp)
    ) {
        App()
    }
}