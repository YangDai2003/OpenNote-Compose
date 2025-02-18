package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.sin

/**
 * A custom shape with curly edges, resembling a circle with sinusoidal indentations.
 *
 * This shape creates an outline that appears as a circle with "curly" or "wavy"
 * edges, achieved by applying a sine wave function to the circle's radius. The
 * amplitude and the number of curls (waves) can be adjusted.
 *
 * @property curlAmplitude The amplitude of the sine wave, controlling the depth of the curls.
 *               A higher value results in deeper curls. Defaults to 16.0.
 * @property curlCount The number of curls (waves) around the circle's circumference.
 *                A higher count results in more frequent curls. Defaults to 12.
 */
class CurlyCornerShape(
    private val curlAmplitude: Double = 16.0,
    private val curlCount: Int = 12,
) : CornerBasedShape(
    topStart = ZeroCornerSize,
    topEnd = ZeroCornerSize,
    bottomEnd = ZeroCornerSize,
    bottomStart = ZeroCornerSize
) {

    /**
     * Calculates the x and y coordinates of a point on the curly circle at a given angle.
     *
     * @param centerX The x-coordinate of the center of the circle.
     * @param centerY The y-coordinate of the center of the circle.
     * @param baseRadius The base radius of the circle.
     * @param amplitude The amplitude of the sine wave.
     * @param angle The angle in radians.
     * @return A Pair containing the x and y coordinates of the point.
     */
    private fun calculateCurlyCirclePoint(
        centerX: Double,
        centerY: Double,
        baseRadius: Double,
        amplitude: Double,
        angle: Double,
    ): Pair<Double, Double> {
        // Calculate the radius with the sine wave applied.
        val radius = baseRadius + amplitude * sin(curlCount * angle)
        // Calculate x and y coordinates using polar coordinates.
        val x = centerX + radius * cos(angle)
        val y = centerY + radius * sin(angle)
        return Pair(x, y)
    }

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection
    ): Outline {
        val centerX = size.width / 2.0
        val centerY = size.height / 2.0
        val baseRadius = centerX - curlAmplitude
        val path = Path()

        // Start at the rightmost point
        val startPoint = calculateCurlyCirclePoint(centerX, centerY, baseRadius, curlAmplitude, 0.0)
        path.moveTo(startPoint.first.toFloat(), startPoint.second.toFloat())

        // Iterate through 360 degrees to draw the curly circle
        for (angleDegrees in 1..360) {
            // Convert the angle to radians.
            val angleRadians = Math.toRadians(angleDegrees.toDouble())

            // calculate the current point
            val currentPoint = calculateCurlyCirclePoint(centerX, centerY, baseRadius, curlAmplitude, angleRadians)

            path.lineTo(currentPoint.first.toFloat(), currentPoint.second.toFloat())
        }

        path.close()
        return Outline.Generic(path)
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize,
    ): CurlyCornerShape = CurlyCornerShape(
        curlAmplitude = this.curlAmplitude,
        curlCount = this.curlCount
    )
}
