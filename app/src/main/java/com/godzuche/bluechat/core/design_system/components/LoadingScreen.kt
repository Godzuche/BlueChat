package com.godzuche.bluechat.core.design_system.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluechat.R
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun WaterRipple() {
    var time by remember { mutableStateOf(0f) }
    var amplitude by remember { mutableStateOf(100f) }
    val frequency = 0.02f // Adjust for wave frequency
    val canvasWidth = 400.dp // Adjust for canvas width
    val canvasHeight = 200.dp // Adjust for canvas height

    LaunchedEffect(Unit) {
        while (true) {
            time += 0.01f
            delay(16) // Update at 60fps
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        translate(0f, (canvasHeight / 2).value * height) {
            drawPath(
                path = Path().apply {
                    moveTo(0f, 0f)
                    for (x in 0..width.toInt()) {
                        val y = (
                                amplitude * sin((x.toFloat() / width) * 2 * PI + time * frequency * PI)).toFloat()
                        lineTo(x.toFloat(), y)
                    }
                },
                color = Color.White,
//                alpha = 0.5f
            )
        }
    }
}

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
fun BtLoadingScreen(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
        ),
        label = "infinite bluetooth pulse animation",
    )

    Surface(
        color = Color.Transparent,
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.icons8_bluetooth_96),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(60.dp)
                    .graphicsLayer {
                        scaleX = progress
                        scaleY = progress
                        alpha = 1f - progress
                    },
            )
        }
    }
}

@Composable
fun WaveLoadingScreen(
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
        ),
        label = "infinite bluetooth pulse animation",
    )

    Surface(
        color = Color.Transparent,
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(screenWidth)
                    .height(screenWidth)
                    .graphicsLayer {
                        scaleX = progress
                        scaleY = progress
                        alpha = 1f - progress
                    }
                    .border(
                        width = 10.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
fun PhysicsRippleScanner(
    modifier: Modifier = Modifier.size(300.dp),
    rippleColor: Color = Color.Cyan,
    rippleCount: Int = 3,
    durationMillis: Int = 3000,
    iconSize: Dp = 100.dp,
    iconResId: Int = R.drawable.icons8_bluetooth_96,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple-transition")

    val ripples = List(rippleCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(
                    offsetMillis = (durationMillis / (2 * rippleCount)) * index,
                    offsetType = StartOffsetType.Delay
                ),
            ),
            label = "ripple-$index"
        )
    }

    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis / rippleCount / 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-icon"
    )

    Surface(color = Color.Transparent) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val steps = 360

                ripples.forEachIndexed { index, animatedProgress ->
                    val progress = animatedProgress.value
                    val radius = size.maxDimension * progress
                    val alpha = 1f - progress

                    val path = Path()
                    for (i in 0..steps) {
                        val angle = Math.toRadians(i.toDouble())
                        val baseX = cos(angle).toFloat()
                        val baseY = sin(angle).toFloat()

                        val wave = 4f * sin((i + progress * 360) * 0.1)
                        val noise = sin((i * 0.3) + (index * 0.5)) * 2f
                        val finalRadius = radius + wave + noise

                        val x = center.x + finalRadius * baseX
                        val y = center.y + finalRadius * baseY

                        if (i == 0) path.moveTo(
                            x.toFloat(),
                            y.toFloat()
                        ) else path.lineTo(x.toFloat(), y.toFloat())
                    }
                    path.close()

                    drawPath(
                        path = path,
                        color = rippleColor.copy(alpha = alpha),
                        style = Stroke(width = 3f * (1f - alpha))
                    )
                }
            }

            Image(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        alpha = 1f - iconPulse
                        scaleX = 1f - iconPulse
                        scaleY = 1f - iconPulse
                    }
            )
        }
    }
}

@Composable
fun PhysicsRippleScannerAdvanced(
    modifier: Modifier = Modifier.size(300.dp),
    rippleColor: Color = Color.Cyan,
    rippleCount: Int = 3,
    durationMillis: Int = 3000,
    iconSize: Dp = 100.dp,
    iconResId: Int,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple-transition")

    val frequency by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "freq"
    )

    val amplitude by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "amp"
    )

    val ripples = List(rippleCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(
                    offsetMillis = (durationMillis / (2 * rippleCount)) * index,
                    offsetType = StartOffsetType.Delay
                ),
            ), label = "ripple-$index"
        )
    }

    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis / rippleCount / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ), label = "pulse-icon"
    )

    Surface(color = Color.Transparent) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val steps = 360

                ripples.forEachIndexed { index, animatedProgress ->
                    val progress = animatedProgress.value
                    val radius = size.maxDimension * progress
                    val alpha = 1f - progress

                    val path = Path()
                    for (i in 0..steps) {
                        val angle = Math.toRadians(i.toDouble())
                        val baseX = cos(angle).toFloat()
                        val baseY = sin(angle).toFloat()

                        val noise = sin(i * frequency + progress * 10 + index) * amplitude
                        val finalRadius = radius + noise

                        val x = center.x + finalRadius * baseX
                        val y = center.y + finalRadius * baseY

                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()

                    drawPath(
                        path = path,
                        color = rippleColor.copy(alpha = alpha),
                        style = Stroke(width = 3f * (1f - alpha))
                    )
                }
            }

            Image(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        alpha = 1f - iconPulse
                        scaleX = 1f - iconPulse
                        scaleY = 1f - iconPulse
                    }
            )
        }
    }
}

@Composable
fun PhysicsRippleScanner2(
    modifier: Modifier = Modifier.fillMaxSize(),
    rippleCount: Int = 3,
    durationMillis: Int = 3000,
    rippleColor: Color = Color.Cyan,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple-transition")

    // Animate frequency and amplitude for each ripple
    val rippleModulations = remember {
        List(rippleCount) {
            RippleModulation(
                frequency = Random.nextFloat() * 2f + 1f,
                amplitude = Random.nextFloat() * 0.5f + 0.5f,
                phaseOffset = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }

    val ripples = List(rippleCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(
                    offsetMillis = (durationMillis / (2 * rippleCount)) * index,
                    offsetType = StartOffsetType.Delay
                ),
            ),
            label = "ripple-$index"
        )
    }

    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis / rippleCount / 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-icon"
    )

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
//            val time = withFrameNanos { it } / 1_000_000_000f // time in seconds

//            ripples.forEachIndexed { index, animatedProgress ->
//                val progress = animatedProgress.value
//                val modulation = rippleModulations[index]
//
//                // Dynamic wave equation (sinusoidal) for ripple distortion
//                val waveOffset = modulation.amplitude * sin(modulation.frequency * 2 * PI.toFloat() * progress + modulation.phaseOffset)
//                val radius = (size.maxDimension * (progress + waveOffset.coerceIn(-0.2f, 0.2f))).coerceAtLeast(0f)
//                val alpha = (1f - progress).coerceIn(0f, 1f)
//
//                drawCircle(
//                    color = rippleColor.copy(alpha = alpha),
//                    radius = radius,
//                    center = center,
//                    style = Stroke(width = 4f * (1f - alpha))
//                )
//            }

            ripples.forEachIndexed { index, animatedProgress ->
                val progress = animatedProgress.value
                val modulation = rippleModulations[index]

                val baseRadius = size.maxDimension * progress

                // Radial wave propagation using time and distance
                val waveTime = System.currentTimeMillis() / 1000f + modulation.phaseOffset
                val oscillation =
                    modulation.amplitude * cos(2 * PI * modulation.frequency * waveTime)
                val dynamicRadius = baseRadius + oscillation * 20f
                val clampedRadius = dynamicRadius.toFloat().coerceAtLeast(0f)
                val alpha = (1f - progress).coerceIn(0f, 1f) * exp(-progress * 2)

                drawCircle(
                    color = rippleColor.copy(alpha = alpha),
                    radius = clampedRadius,
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
                    alpha = 1f - iconPulse
                    scaleX = 1f - iconPulse
                    scaleY = 1f - iconPulse
                }
        )
    }
}

private data class RippleModulation(
    val frequency: Float,
    val amplitude: Float,
    val phaseOffset: Float,
)

@Composable
fun PhysicsRippleScanner3(
    modifier: Modifier = Modifier.fillMaxSize(),
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
//    val animatedAmplitudes = List(rippleCount) {
//        infiniteTransition.animateFloat(
//            initialValue = 0.7f,
//            targetValue = 1f,
//            animationSpec = infiniteRepeatable(
//                animation = tween(5000 + it * 500, easing = LinearEasing),
//                repeatMode = RepeatMode.Reverse
//            ),
//            label = "amplitude-$it"
//        )
//    }
//    val animatedFrequencies = List(rippleCount) {
//        infiniteTransition.animateFloat(
//            initialValue = 0.5f,
//            targetValue = 1f,
//            animationSpec = infiniteRepeatable(
//                animation = tween(5000 + it * 500, easing = LinearEasing),
//                repeatMode = RepeatMode.Reverse
//            ),
//            label = "frequency-$it"
//        )
//    }

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
fun WaterRippleEffect(
    modifier: Modifier = Modifier,
    rippleColor: Color = Color.Blue,
    rippleRadius: Float = 300f,
    rippleCount: Int = 4,
    durationMillis: Int = 2000,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple-transition")

    val ripples =
        List(rippleCount) { index ->
            infiniteTransition.animateFloat(
                initialValue = 0f
                /** index * 1f / rippleCount*/
                ,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(
                        offsetMillis = (durationMillis / (2 * rippleCount)) * index /*+ index * 30*/,
                        offsetType = StartOffsetType.Delay
                    ),
                ),
                label = "ripple-$index"
            )
        }

    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis / rippleCount / 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-icon"
    )

    Surface(
        color = Color.Transparent,
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = modifier) {
                val center = Offset(size.width / 2f, size.height / 2f)
                ripples.forEach { animatedProgress ->
//            val radius = rippleRadius * animatedProgress.value
                    val radius = size.maxDimension * animatedProgress.value
                    val alpha = 1f - animatedProgress.value

                    drawCircle(
                        color = rippleColor.copy(alpha = alpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 4f * (1f - alpha)) // Thin as it fades
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.icons8_bluetooth_96),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        alpha = 1f - iconPulse
                        scaleX = 1f - iconPulse
                        scaleY = 1f - iconPulse
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

@Composable
fun HostConnectedAnimation(
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
) {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.5f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(0f, animationSpec = tween(500))
        onDone()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .background(Color.Green.copy(alpha = 0.6f), CircleShape)
        )
        Text("Connected", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun RadarWithConnectionFlow() {
    var hostFound by remember { mutableStateOf(false) }
    var showConnectedAnim by remember { mutableStateOf(false) }

    // Trigger host detection after 5 seconds for demo
    LaunchedEffect(Unit) {
        delay(5000)
        hostFound = true
    }

    Surface(
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                !hostFound -> {
                    RadarScan(
                        radarColor = Color.Green,
                        rippleRadius = 200f,
                        rippleCount = 3
                    )
                }

                hostFound && !showConnectedAnim -> {
                    HostConnectedAnimation(onDone = {
                        showConnectedAnim = true
                    })
                }

                showConnectedAnim -> {
                    Text(
                        text = "Connected to Host X",
                        color = Color.Green,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
