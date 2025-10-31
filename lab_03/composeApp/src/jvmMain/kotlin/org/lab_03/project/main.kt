package org.lab_03.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "lab_03",
    ) {
        AllDistributionPlots()
    }
}
