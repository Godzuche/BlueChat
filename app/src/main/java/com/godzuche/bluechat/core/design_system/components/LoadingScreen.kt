package com.godzuche.bluechat.core.design_system.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bluechat.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.random.Random

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color.Transparent,
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun PhysicsRippleScanner(
    modifier: Modifier = Modifier,
    rippleCount: Int = 3,
    durationMillis: Int = 3000,
    rippleColor: Color = Color.Cyan,
) {
    val phaseOffsets =
        remember { List(rippleCount) { Random.nextFloat() * 2f * PI.toFloat() } } // random soft jitter
//    val frequencies = remember { List(rippleCount) { Random.nextFloat() * 0.5f + 0.5f } }
//    val amplitudes = remember { List(rippleCount) { Random.nextFloat() * 0.3f + 0.7f } }

    // Animate amplitude and frequency separately for each ripple
    val infiniteTransition = rememberInfiniteTransition(label = "modulation-transition")

    // Smoothly animated frequency & amplitude for each ripple
    val animatedFrequencies = List(rippleCount) {
        infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis * 2, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "freq-$it"
        )
    }

    val animatedAmplitudes = List(rippleCount) {
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis * 2, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "amp-$it"
        )
    }

    val iconPulse = rememberInfiniteTransition(label = "pulse-transition").animateFloat(
        initialValue = 0f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis / rippleCount, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-icon"
    )

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
//            withFrameNanos { frameTime ->
//                time = frameTime
//            }
            withInfiniteAnimationFrameNanos { time = it }
        }
    }

    Surface(
        color = Color.Transparent,
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val seconds = time / 1_000_000_000f

                repeat(rippleCount) { index ->
//                val freq = frequencies[index]
//                val amp = amplitudes[index]
                    val freq = animatedFrequencies[index]
                    val amp = animatedAmplitudes[index]
                    val phase = phaseOffsets[index]

                    // Calculate expanding wave
                    // waveTime = seconds + phase // Simple linear wave
//                val waveTime = seconds * 0.5f + phase // Slow it down
                    val waveTime = seconds * 0.3f + phase // Slower propagation with phase offset
                    val omega = 2f * PI.toFloat() * freq.value
//                val progress = (seconds * freq.value + index * 0.3f) % 1f
//                val progress = ((waveTime + index * 0.20f) % 1f).coerceIn(0f, 1f)
                    val progress = ((waveTime + index * 0.25f) % 1f).coerceIn(0f, 1f)
                    val baseRadius = size.maxDimension * progress
                    val waveOscillation = amp.value * cos(omega * waveTime)
//                val waveRadius = baseRadius + waveOscillation * 20f
                    val waveRadius = baseRadius + waveOscillation * 15f // reduce jitter

                    val radius = waveRadius.coerceAtLeast(0f)
//                val alpha = (1f - progress).coerceIn(0f, 1f) * exp(-progress * 2f)
//                val alpha = (1f - progress).coerceIn(0f, 1f) * exp(-progress * 1.5f) // smoother fade
                    val alpha =
                        (1f - progress).coerceIn(0f, 1f) * exp(-progress * 1.2f) // smoother fade

                    drawCircle(
                        color = rippleColor.copy(alpha = alpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 4f * alpha)
                    )
                }
            }

            Image(
                painter = painterResource(R.drawable.icons8_bluetooth_96),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        alpha = 1f - iconPulse.value
                        scaleX = 1f - iconPulse.value
                        scaleY = 1f - iconPulse.value
                    }
            )
        }
    }
}

@Composable
fun RadarScan(
    modifier: Modifier = Modifier,
    radarColor: Color = Color.Green,
    beamAlpha: Float = 0.3f,
    rippleCount: Int = 4,
    rippleRadius: Float = 300f,
    durationMillis: Int = 2000,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar-scan")

    // Animate the rotating beam
    val angle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing)
        ),
        label = "beam-rotation"
    )

    // Animate ripple rings
    val ripples =
        List(rippleCount) { index ->
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset((durationMillis / rippleCount) * index)
                ),
                label = "ripple-$index"
            )
        }
    Surface(
        color = Color.Transparent,
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
//        val maxRadius = rippleRadius.coerceAtMost(size.minDimension / 2f)
                val maxRadius = size.maxDimension / 2f

                // Draw ripple rings
                ripples.forEach { progress ->
                    val radius = maxRadius * progress.value
                    val alpha = 1f - progress.value
                    drawCircle(
                        color = radarColor.copy(alpha = alpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }

                // Draw radar sweep (arc beam)
                drawArc(
                    color = radarColor.copy(alpha = beamAlpha),
                    startAngle = angle.value,
                    sweepAngle = 45f,
                    useCenter = true,
                    topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                    size = Size(maxRadius * 2, maxRadius * 2),
                    style = Fill,
                )

                // Draw center dot
                drawCircle(
                    color = radarColor,
                    radius = 6f,
                    center = center
                )
            }
        }
    }
}
