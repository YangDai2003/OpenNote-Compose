package com.yangdai.opennote.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.launch
import kotlin.random.Random

@Stable
data class Confetti(
    val color: Color,
    val position: Offset,
    val rotation: Float,
    val scale: Float,
    val shape: ConfettiShape,
    val rotationSpeed: Float,
    val velocity: Offset
)

enum class ConfettiShape {
    CIRCLE, SQUARE
}

/**
 * A composable function that creates a confetti effect animation.
 *
 * This function simulates a confetti shower with customizable parameters such as colors,
 * particle count, duration, rotation speed, particle size, initial velocity, gravity, and drag.
 * It uses a `Canvas` to draw the confetti particles and animates their position and rotation over time.
 *
 * @param particleColors A list of colors to use for the confetti particles. Defaults to a vibrant
 *                       rainbow-like set of colors.
 * @param particleCount The number of confetti particles to spawn. Defaults to 520.
 * @param effectDurationMillis The duration of the confetti effect animation in milliseconds.
 *                             Defaults to 3600ms (3.6 seconds).
 * @param rotationSpeed The speed at which the confetti particles rotate, in degrees per frame.
 *                      Defaults to 5f.
 * @param particleSize The base size of the confetti particles. Defaults to 10f.
 * @param horizontalVelocity The initial horizontal velocity of the confetti particles. Defaults to 3f.
 *                           Positive values move particles to the right, negative to the left.
 * @param verticalVelocity The initial vertical velocity of the confetti particles. Defaults to -30f,
 *                         meaning particles initially move upwards.
 * @param gravityForce The force of gravity acting on the confetti particles. Defaults to 0.3f.
 */
@Composable
fun ConfettiEffect(
    particleColors: List<Color> = listOf(
        Color(0xFFFF6B6B),  // 暖红色
        Color(0xFF4ECDC4),  // 湖蓝色
        Color(0xFFFFBE0B),  // 明黄色
        Color(0xFF4D96FF),  // 靛蓝色
        Color(0xFFFF8585),  // 珊瑚红
        Color(0xFF43AA8B),  // 翡翠绿
        Color(0xFFFB5607),  // 橙色
        Color(0xFF7209B7),  // 紫色
        Color(0xFFFC3A52),  // 玫红色
        Color(0xFF3F8EFC),  // 天蓝色
        Color(0xFFFF9F1C),  // 金橙色
        Color(0xFF2EC4B6)   // 青绿色
    ),
    particleCount: Int = 520,
    effectDurationMillis: Int = 5000,
    rotationSpeed: Float = 5f,
    particleSize: Float = 10f,
    horizontalVelocity: Float = 3f,    // 初始水平速度
    verticalVelocity: Float = -30f,   // 初始向上速度
    gravityForce: Float = 0.3f,       // 重力加速度
    dragCoefficient: Float = 1f    // 空气阻力系数
) {
    var particles by remember { mutableStateOf(emptyList<Confetti>()) }
    val animationScope = rememberCoroutineScope()
    var componentWidth by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                componentWidth = size.width.toFloat()
            }
    ) {
        particles.forEach { particle ->
            // 使用 rotate 来实现旋转效果
            withTransform({
                rotate(
                    degrees = particle.rotation,
                    pivot = particle.position
                )
            }) {
                when (particle.shape) {
                    ConfettiShape.CIRCLE -> {
                        drawCircle(
                            color = particle.color,
                            radius = particleSize * particle.scale,
                            center = particle.position
                        )
                    }

                    ConfettiShape.SQUARE -> {
                        val size = particleSize * 2 * particle.scale
                        drawRect(
                            color = particle.color,
                            topLeft = Offset(
                                particle.position.x - size / 2,
                                particle.position.y - size / 2
                            ),
                            size = Size(size, size)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (componentWidth > 0)
            particles = spawnConfettiParticles(
                confettiCount = particleCount,
                colors = particleColors,
                componentWidth = componentWidth,
                baseRotationSpeed = rotationSpeed,
                initialVelocityX = horizontalVelocity,
                initialVelocityY = verticalVelocity
            )

        animationScope.launch {
            val shootingAnimatable = Animatable(0f)
            shootingAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(effectDurationMillis, easing = LinearEasing)
            ) {
                particles = computeConfettiPositions(
                    confetti = particles,
                    gravityForce = gravityForce,
                    dragCoefficient = dragCoefficient
                )
            }

            particles = emptyList()
        }
    }
}

private fun spawnConfettiParticles(
    confettiCount: Int,
    colors: List<Color>,
    componentWidth: Float,
    baseRotationSpeed: Float,
    initialVelocityX: Float,
    initialVelocityY: Float
): List<Confetti> {
    return List(confettiCount) {
        // 随机选择左侧或右侧发射
        val isLeftSide = Random.nextBoolean()
        val startX = if (isLeftSide) {
            componentWidth * 0.2f // 左侧20%位置
        } else {
            componentWidth * 0.8f // 右侧80%位置
        }

        // 初始速度
        val velocityX =
            initialVelocityX * (if (isLeftSide) 1f else -1f) * (0.8f + Random.nextFloat() * 0.4f)
        val velocityY = initialVelocityY * (0.8f + Random.nextFloat() * 0.4f)

        Confetti(
            color = colors.random(),
            position = Offset(
                x = startX + Random.nextFloat() * 800 - 400, // 在发射点附近随机偏移
                y = componentWidth * 0.6f + Random.nextFloat() * 50 // 在屏幕高度60%处发射
            ),
            rotation = Random.nextFloat() * 360f,
            scale = 0.8f + Random.nextFloat() * 0.4f,
            shape = if (Random.nextBoolean()) ConfettiShape.CIRCLE else ConfettiShape.SQUARE,
            rotationSpeed = baseRotationSpeed * (0.5f + Random.nextFloat()) *
                    if (Random.nextBoolean()) 1f else -1f,
            velocity = Offset(velocityX, velocityY)
        )
    }
}

private fun computeConfettiPositions(
    confetti: List<Confetti>,
    gravityForce: Float,
    dragCoefficient: Float
): List<Confetti> {
    return confetti.map { particle ->
        // 更新速度（考虑重力和空气阻力）
        val newVelocity = Offset(
            x = particle.velocity.x * dragCoefficient,
            y = particle.velocity.y + gravityForce
        )

        // 更新位置
        val newPosition = Offset(
            x = particle.position.x + newVelocity.x,
            y = particle.position.y + newVelocity.y
        )

        particle.copy(
            position = newPosition,
            velocity = newVelocity,
            rotation = (particle.rotation + particle.rotationSpeed) % 360
        )
    }
}
