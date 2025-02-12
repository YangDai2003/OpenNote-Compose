package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp

@PreviewScreenSizes
@Composable
private fun PreviewEmptyDataImage() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .sizeIn(maxWidth = 673.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                PaletteImage()
            }
        }
    }
}

@Composable
fun PaletteImage(colorScheme: ColorScheme = MaterialTheme.colorScheme) {
    val image = rememberPaletteImageVector(colorScheme)
    Image(
        imageVector = image,
        contentDescription = ""
    )
}

@Composable
private fun rememberPaletteImageVector(colorScheme: ColorScheme): ImageVector {
    val clipPathData = remember {
        PathData {
            moveTo(0f, 0f)
            horizontalLineTo(1114.7f)
            verticalLineTo(767.4f)
            horizontalLineTo(-1114.7f)
            close()
        }
    }
    return remember(colorScheme) {
        ImageVector.Builder(
            defaultWidth = 1114.7.dp,
            defaultHeight = 767.4.dp,
            viewportWidth = 1114.7f,
            viewportHeight = 767.4f
        )
            .addGroup(clipPathData = clipPathData)
            .addPath(
                pathData = addPathNodes("M343.4,334.8c0,129.3 -76.9,174.5 -171.7,174.5S0,464.2 0,334.8 171.7,41 171.7,41 343.4,205.5 343.4,334.8Z"),
                fill = SolidColor(colorScheme.surfaceVariant)
            )
            .addPath(
                pathData = addPathNodes("M165.4,489.5l1.8,-108.2l73.2,-133.9l-72.9,116.9l0.8,-48.7l50.4,-96.9l-50.2,84l0,0l1.4,-87.5l54,-77.1l-53.8,63.4l0.9,-160.5l-5.6,212.4l0.5,-8.8l-54.9,-84.1l54,100.9l-5.1,97.7l-0.2,-2.6l-63.3,-88.5l63.1,97.6l-0.6,12.2l-0.1,0.2l0.1,1l-13,248l17.3,0l2.1,-128.1l63,-97.4l-62.8,87.8z"),
                fill = SolidColor(colorScheme.onSurface)
            )
            .addPath(
                pathData = addPathNodes("M1114.7,643.4c0,44.7 -230.1,124 -514,124s-514,-79.3 -514,-124 230.1,-38 514,-38S1114.7,598.7 1114.7,643.4Z"),
                fill = SolidColor(colorScheme.onSurfaceVariant)
            )
            .addPath(
                pathData = addPathNodes("M1114.7,643.4c0,44.7 -230.1,124 -514,124s-514,-79.3 -514,-124 230.1,-38 514,-38S1114.7,598.7 1114.7,643.4Z"),
                fill = SolidColor(Color(0xFF000000)),
                strokeAlpha = 0.1f,
                fillAlpha = 0.1f
            )
            .addPath(
                pathData = addPathNodes("M86.7,643.4a514,81 0,1 0,1028 0a514,81 0,1 0,-1028 0z"),
                fill = SolidColor(colorScheme.onSurfaceVariant)
            )
            .addPath(
                pathData = addPathNodes("M497.4,654.9a122,25.5 0,1 0,244 0a122,25.5 0,1 0,-244 0z"),
                fill = SolidColor(Color(0xFF000000)),
                strokeAlpha = 0.1f,
                fillAlpha = 0.1f
            )
            .addPath(
                pathData = addPathNodes("M480.4,96.1m-48.6,0a48.6,48.6 0,1 1,97.1 0a48.6,48.6 0,1 1,-97.1 0"),
                fill = SolidColor(colorScheme.error)
            )
            .addPath(
                pathData = addPathNodes("M386.6,83m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(colorScheme.error),
                strokeAlpha = 0.3f,
                fillAlpha = 0.3f
            )
            .addPath(
                pathData = addPathNodes("M408.6,49.3m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(colorScheme.error),
                strokeAlpha = 0.5f,
                fillAlpha = 0.5f
            )
            .addPath(
                pathData = addPathNodes("M430.5,15.5m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(colorScheme.error)
            )
            .addPath(
                pathData = addPathNodes("M430.5,15.5m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(Color(0xFFFFFFFF)),
                strokeAlpha = 0.3f,
                fillAlpha = 0.3f
            )
            .addPath(
                pathData = addPathNodes("M479.2,329.5m-48.6,0a48.6,48.6 0,1 1,97.1 0a48.6,48.6 0,1 1,-97.1 0"),
                fill = SolidColor(colorScheme.inversePrimary)
            )
            .addPath(
                pathData = addPathNodes("M443.2,417.2m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(colorScheme.inversePrimary),
                strokeAlpha = 0.3f,
                fillAlpha = 0.3f
            )
            .addPath(
                pathData = addPathNodes("M416,387.5m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(colorScheme.inversePrimary),
                strokeAlpha = 0.5f,
                fillAlpha = 0.5f
            )
            .addPath(
                pathData = addPathNodes("M388.8,357.8m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(colorScheme.inversePrimary)
            )
            .addPath(
                pathData = addPathNodes("M388.8,357.8m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(Color(0xFFFFFFFF)),
                strokeAlpha = 0.3f,
                fillAlpha = 0.3f
            )
            .addPath(
                pathData = addPathNodes("M557.5,595.2s-0.3,17.3 -1.8,21.9 0,10.4 0,10.4l-5.6,14.8h20s4.7,-21.6 4.7,-23.6 -2.8,-18.2 -2.8,-18.2Z"),
                fill = SolidColor(Color(0xFFFFB9B9))
            )
            .addPath(
                pathData = addPathNodes("M572.9,641.1s-5,-13.8 -26.4,0c0,0 -8.4,12.4 -3.7,16.3s12.9,9 19.7,6.1 9.5,-6.2 10.4,-12.3A30.5,30.5 0,0 0,572.9 641.1Z"),
                fill = SolidColor(Color(0xFF2F2E41))
            )
            .addPath(
                pathData = addPathNodes("M665.3,595.2s0.3,17.3 1.8,21.9 0,10.4 0,10.4l5.6,14.8L652.7,642.3s-4.7,-21.6 -4.7,-23.6 2.8,-18.2 2.8,-18.2Z"),
                fill = SolidColor(Color(0xFFFFB9B9))
            )
            .addPath(
                pathData = addPathNodes("M649.9,641.1s5,-13.8 26.4,0c0,0 8.4,12.4 3.7,16.3s-12.9,9 -19.7,6.1 -9.5,-6.2 -10.4,-12.3A30.5,30.5 0,0 1,649.9 641.1Z"),
                fill = SolidColor(Color(0xFF2F2E41))
            )
            .addPath(
                pathData = addPathNodes("M552.5,268.8s14.5,20 16.3,26.4 12.9,5.8 12.1,0 -20.1,-36.7 -20.1,-36.7Z"),
                fill = SolidColor(Color(0xFFFFB9B9))
            )
            .addPath(
                pathData = addPathNodes("M669.8,262.2s-20.4,34.7 -15.3,38.5S679.4,270.5 679.4,270.5Z"),
                fill = SolidColor(Color(0xFFFFB9B9))
            )
            .addPath(
                pathData = addPathNodes("M650.7,289.3h-33.8c-6.2,0 -26.7,1.6 -29.9,0S571.6,287.9 571.6,287.9s-24.7,56.6 -18.1,101.8 -14.1,201.5 -4.3,206.2 18.5,10.7 27,11 24.1,-3.4 25.9,-5.9 10.5,-222 10.5,-222 3.1,216.6 8.9,221.4 22.4,8.1 32.7,6.5 21,-7.4 23.1,-11.3S670.1,482.9 670.1,482.9s14.4,-166.9 -9.3,-187.8Z"),
                fill = SolidColor(Color(0xFF2F2E41))
            )
            .addPath(
                pathData = addPathNodes("M601,117.4s7.4,24.3 -7.3,31.3 12.6,63.7 12.6,63.7 38.1,-48.7 38.1,-59.4l-6.4,-6.4s-12.4,-20.9 -8.6,-27.4S601,117.4 601,117.4Z"),
                fill = SolidColor(Color(0xFFFFB9B9))
            )
            .addPath(
                pathData = addPathNodes("M601,117.4s7.4,24.3 -7.3,31.3 12.6,63.7 12.6,63.7 38.1,-48.7 38.1,-59.4l-6.4,-6.4s-12.4,-20.9 -8.6,-27.4S601,117.4 601,117.4Z"),
                fill = SolidColor(Color(0xFF000000)),
                strokeAlpha = 0.1f,
                fillAlpha = 0.1f
            )
            .addPath(
                pathData = addPathNodes("M611.8,200.2s-8.9,-23.3 -7.9,-32 -1.4,-23 -5.2,-23.4 -42.9,12.3 -46.9,15 -7.6,56.9 -7.6,56.9l17.5,40.3s1.5,33 9.8,34.5 86.7,5.6 89.2,2.8 28.3,-79.7 28.3,-79.7l5.8,-45S687.5,160.5 679.8,158.4s-32.6,-11 -35.4,-11.3 -11.7,1.3 -12.4,4S611.8,200.2 611.8,200.2Z"),
                fill = SolidColor(colorScheme.secondaryContainer)
            )
            .addPath(
                pathData = addPathNodes("M617,102.5m-30.4,0a30.4,30.4 0,1 1,60.7 0a30.4,30.4 0,1 1,-60.7 0"),
                fill = SolidColor(Color(0xFFFFB9B9))
            )
            .addPath(
                pathData = addPathNodes(
                    "M627.4,46.3c-5.7,-6.9 -17.3,-7.6 -23.8,-1.5 -1.8,1.6 -3.1,3.7 -5,5.2a23.4,23.4 0,0 1,-5.9 3.4c-6.9,3 -14.3,5.2 -20.8,9.1s-12.2,10.2 -13,17.7c-0.4,4 0.5,8.1 -0.2,12 -1.1,6.2 -6.1,10.8 -10.3,15.5 -3.9,4.5 -7.5,10.9 -4.7,16.1 2.4,4.5 8.3,5.8 11.6,9.7 3.8,4.4 3.3,11.2 1.4,16.7s-5,10.8 -5.7,16.6c-1.3,9.6 4,18.9 10.8,25.7 3.1,3.1 6.7,5.9 11,7.2a19,19 0,0 0,18.3 -4.6,21 21,0 0,0 5.8,-18.1c-0.6,-3.6 -2.1,-7.1 -1.7,-10.7 0.8,-7.4 8.9,-12.1 10.8,-19.3 2,-7.4 -3,-14.6 -5.9,-21.6 -3.7,-8.9 -4.2,-18.8 -4.3,-28.4 -0,-5.5 0.5,-12 5.2,-14.9 3,-1.9 6.9,-1.7 10.5,-2 4.2,-0.3 8.4,-1.4 12.5,-1s8.6,2.7 9.7,6.8c1.3,4.5 -1.9,9.1 -1.9,13.8 0,2.3 0.8,4.6 0.8,6.9 0,6.7 -6.2,11.5 -9.7,17.2a23.4,23.4 0,0 0,2 26.4c3.5,4.2 8.9,7.7 9.5,13.2 0.3,3.1 -1,6.1 -1.7,9.1s-0.9,6.6 1.3,8.8c1.5,1.5 3.7,2 5.8,2.3a44.9,44.9 0,0 0,16.1 -0.8,7.7 7.7,0 0,0 3.7,-1.7c2.6,-2.5 1,-7 2.2,-10.5 1.2,-3.4 4.9,-5.1 8.3,-6s7.2,-1.4 10,-3.6c3.1,-2.4 4.5,-6.6 4.3,-10.6s-1.7,-7.7 -3.5,-11.3a3.7,3.7 0,0 1,-0.6 -2.2c0.3,-2 3.3,-1.9 5.3,-1.9 5.5,-0.1 9.6,-5.7 9.8,-11.1s-2.8,-10.5 -6.2,-14.8 -7.6,-7.9 -10.6,-12.4c-5.2,-7.7 -7,-17.6 -13.3,-24.5a24,24 0,0 0,-11.4 -6.8c-3.3,-0.9 -8.4,0.2 -11,-2.2 -1,-0.9 -1.5,-2.4 -2.5,-3.4 -1.6,-1.6 -3.8,-2.1 -5.7,-3.4C631.5,53.9 630.1,49.6 627.4,46.3Z"
                ),
                fill = SolidColor(Color(0xFF2F2E41))
            )
            .addPath(
                pathData = addPathNodes("M557.5,160.9l-5.6,-1.2s-69.9,55.3 -73.3,62.4 -12.6,13.5 6.8,25 67.2,30.2 67.2,30.2l11.1,-19.7 -54.4,-28s12.6,-17.8 38.8,-13.8Z"),
                fill = SolidColor(colorScheme.secondaryContainer)
            )
            .addPath(
                pathData = addPathNodes("M689.9,167.3l4.9,2.2s73.6,53.2 64.9,66.5 -79.8,45 -79.8,45l-12.6,-19.8s1,-0.5 2.7,-1.3c10.6,-5.1 49.3,-23.7 47,-25.3 -2.7,-1.8 -25,-21.6 -31.3,-22.4S689.9,167.3 689.9,167.3Z"),
                fill = SolidColor(colorScheme.secondaryContainer)
            )
            .addPath(
                pathData = addPathNodes("M759.5,342.1m-48.6,0a48.6,48.6 0,1 1,97.1 0a48.6,48.6 0,1 1,-97.1 0"),
                fill = SolidColor(colorScheme.tertiary)
            )
            .addPath(
                pathData = addPathNodes("M795.4,429.8m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(colorScheme.tertiary)
            )
            .addPath(
                pathData = addPathNodes("M795.4,429.8m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(Color(0xFFFFFFFF)),
                strokeAlpha = 0.3f,
                fillAlpha = 0.3f
            )
            .addPath(
                pathData = addPathNodes("M822.7,400.1m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(colorScheme.tertiary),
                strokeAlpha = 0.5f,
                fillAlpha = 0.5f
            )
            .addPath(
                pathData = addPathNodes("M849.9,370.4m-15.5,0a15.5,15.5 0,1 1,31 0a15.5,15.5 0,1 1,-31 0"),
                fill = SolidColor(colorScheme.tertiary),
                strokeAlpha = 0.3f,
                fillAlpha = 0.3f
            )
            .addPath(
                pathData = addPathNodes("M1053.4,556.9c0,59.8 -35.5,80.7 -79.4,80.7 -1,0 -2,-0 -3,-0 -2,-0 -4,-0.1 -6,-0.3 -39.6,-2.8 -70.3,-24.7 -70.3,-80.4 0,-57.6 73.6,-130.3 79.1,-135.6l0,-0c0.2,-0.2 0.3,-0.3 0.3,-0.3S1053.4,497.1 1053.4,556.9Z"),
                fill = SolidColor(colorScheme.primary)
            )
            .addPath(
                pathData = addPathNodes("M971.1,628.4l29,-40.6 -29.1,45 -0.1,4.7c-2,-0 -4,-0.1 -6,-0.3l3.1,-59.8 -0,-0.5 0.1,-0.1 0.3,-5.6 -29.2,-45.1 29.3,40.9 0.1,1.2 2.4,-45.2 -25,-46.6 25.3,38.7 2.5,-93.7 0,-0.3v0.3l-0.4,73.9 24.9,-29.3L973.2,501.6l-0.7,40.5 23.2,-38.8 -23.3,44.8 -0.4,22.5 33.7,-54.1L971.9,578.4Z"),
                fill = SolidColor(colorScheme.onSurface)
            )
            .addPath(
                pathData = addPathNodes("M227.4,637.6c0,28.9 -17.2,39 -38.4,39 -0.5,0 -1,-0 -1.5,-0 -1,-0 -2,-0.1 -2.9,-0.1 -19.1,-1.3 -34,-12 -34,-38.9 0,-27.8 35.6,-63 38.2,-65.6l0,-0c0.1,-0.1 0.1,-0.1 0.1,-0.1S227.4,608.7 227.4,637.6Z"),
                fill = SolidColor(colorScheme.primary)
            )
            .addPath(
                pathData = addPathNodes("M187.6,672.2l14,-19.6 -14.1,21.8 -0,2.3c-1,-0 -2,-0.1 -2.9,-0.1L186.1,647.6l-0,-0.2 0,-0 0.1,-2.7 -14.1,-21.8 14.2,19.8 0,0.6 1.1,-21.9L175.4,598.7l12.2,18.7 1.2,-45.3 0,-0.1v0.1l-0.2,35.7 12,-14.2 -12.1,17.3 -0.3,19.6 11.2,-18.8 -11.3,21.7 -0.2,10.9 16.3,-26.1L188,648Z"),
                fill = SolidColor(colorScheme.onSurface)
            )
            .addPath(
                pathData = addPathNodes("M894.4,656.6c0,28.9 -17.2,39 -38.4,39 -0.5,0 -1,-0 -1.5,-0 -1,-0 -2,-0.1 -2.9,-0.1 -19.1,-1.3 -34,-12 -34,-38.9 0,-27.8 35.6,-63 38.2,-65.6l0,-0c0.1,-0.1 0.1,-0.1 0.1,-0.1S894.4,627.7 894.4,656.6Z"),
                fill = SolidColor(colorScheme.primary)
            )
            .addPath(
                pathData = addPathNodes("M854.6,691.2l14,-19.6 -14.1,21.8 -0,2.3c-1,-0 -2,-0.1 -2.9,-0.1L853.1,666.6l-0,-0.2 0,-0 0.1,-2.7 -14.1,-21.8 14.2,19.8 0,0.6 1.1,-21.9L842.4,617.7l12.2,18.7 1.2,-45.3 0,-0.1v0.1l-0.2,35.7 12,-14.2 -12.1,17.3 -0.3,19.6 11.2,-18.8 -11.3,21.7 -0.2,10.9 16.3,-26.1L855,667Z"),
                fill = SolidColor(colorScheme.onSurface)
            )
            .build()
    }
}
