package com.expense.tracker.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.sqrt

/**
 * SensorEventListener implementing a robust accelerometer-based shake detector.
 *
 * It uses:
 * - Normalized g-force calculation (accounts for earth gravity)
 * - Sensible threshold (2.7G) to differentiate intentional shakes from everyday movement
 * - A directional surge window requiring multiple rapid acceleration surges
 * - A cooldown timer (1500ms) to prevent multiple triggers from a single physical shake
 */
class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {
    private var lastShakeTimestamp: Long = 0
    private var surgeCount = 0
    private var lastSurgeTimestamp: Long = 0

    companion object {
        private const val SHAKE_THRESHOLD_G_FORCE = 2.7f
        private const val SHAKE_SURGE_WINDOW_MS = 500L
        private const val SHAKE_COOLDOWN_MS = 1500L
        private const val REQUIRED_SURGES = 2
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()
        if (now - lastShakeTimestamp < SHAKE_COOLDOWN_MS) return

        val x = event.values[0] / SensorManager.GRAVITY_EARTH
        val y = event.values[1] / SensorManager.GRAVITY_EARTH
        val z = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(x * x + y * y + z * z)

        if (gForce > SHAKE_THRESHOLD_G_FORCE) {
            if (now - lastSurgeTimestamp < SHAKE_SURGE_WINDOW_MS) {
                surgeCount++
                if (surgeCount >= REQUIRED_SURGES) {
                    surgeCount = 0
                    lastShakeTimestamp = now
                    onShake()
                }
            } else {
                surgeCount = 1
            }
            lastSurgeTimestamp = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

/**
 * Lifecycle-aware Composable helper that registers the accelerometer listener
 * exclusively when the host Activity / Composable is in ON_RESUME, and unregisters
 * on ON_PAUSE or disposal. Zero background battery consumption.
 */
@Composable
fun rememberShakeDetector(
    enabled: Boolean = true,
    onShake: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnShake by rememberUpdatedState(onShake)

    DisposableEffect(lifecycleOwner, enabled) {
        if (!enabled) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val detector = ShakeDetector { currentOnShake() }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    accelerometer?.let {
                        sensorManager.registerListener(detector, it, SensorManager.SENSOR_DELAY_UI)
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    sensorManager?.unregisterListener(detector)
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager?.unregisterListener(detector)
        }
    }
}
