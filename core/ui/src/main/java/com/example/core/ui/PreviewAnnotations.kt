package com.example.core.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers.BLUE_DOMINATED_EXAMPLE
import androidx.compose.ui.tooling.preview.Wallpapers.GREEN_DOMINATED_EXAMPLE
import androidx.compose.ui.tooling.preview.Wallpapers.RED_DOMINATED_EXAMPLE
import androidx.compose.ui.tooling.preview.Wallpapers.YELLOW_DOMINATED_EXAMPLE

@Preview(name = "Red", wallpaper = RED_DOMINATED_EXAMPLE, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Blue", wallpaper = BLUE_DOMINATED_EXAMPLE, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Green", wallpaper = GREEN_DOMINATED_EXAMPLE, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Yellow", wallpaper = YELLOW_DOMINATED_EXAMPLE, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class PreviewDarkDynamicColors

@Preview(
    name = "Expanded",
    device = Devices.NEXUS_10,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class PreviewDarkExpanded

@Preview(
    name = "Expanded Portrait",
    widthDp = 800,
    heightDp = 1280,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class PreviewDarkExpandedPortrait

@Preview(
    name = "Compact Landscape",
    device = Devices.PIXEL_7_PRO,
    widthDp = 960,
    heightDp = 412,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class PreviewDarkLandscape


@Preview(
    name = "Expanded",
    device = Devices.NEXUS_10,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
annotation class PreviewLightExpanded

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL_7_PRO,
)
annotation class PreviewDark

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = Devices.PIXEL_7_PRO
)
annotation class PreviewLight

