package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RobotSimulationEngine
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun SensorsScreen(
  engine: RobotSimulationEngine,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(RobotBgDark)
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    Text(
      text = "LIVE SENSOR TELEMETRY",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = RobotAccentBlue,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 1. Ultrasonic Card
    SensorDetailCard(
      title = "Ultrasonic Transceiver (HC-SR04)",
      icon = Icons.Default.Sensors,
      color = RobotCyan
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        SubMetric("Center", "${engine.distanceCenter.roundToInt()} cm", engine.distanceCenter < 50f)
        SubMetric("Left (-45°)", "${engine.distanceLeft.roundToInt()} cm", engine.distanceLeft < 50f)
        SubMetric("Right (+45°)", "${engine.distanceRight.roundToInt()} cm", engine.distanceRight < 50f)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Virtual Distance Slider:",
        fontSize = 11.sp,
        color = RobotTextSecondary
      )
      Slider(
        value = engine.distanceCenter,
        onValueChange = {
          engine.distanceCenter = it
          engine.distanceLeft = it + 15f
          engine.distanceRight = it - 10f
        },
        valueRange = 5f..400f,
        colors = SliderDefaults.colors(thumbColor = RobotCyan, activeTrackColor = RobotCyan)
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Capacitive Touch Card
    SensorDetailCard(
      title = "Capacitive Touch Sensor (GPIO 15)",
      icon = Icons.Default.TouchApp,
      color = if (engine.isTouchActive) RobotSuccess else RobotTextMuted
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        SubMetric("Status", if (engine.isTouchActive) "ACTIVE" else "IDLE", engine.isTouchActive)
        SubMetric("Touch Count", engine.touchCount.toString(), false)
        SubMetric("Last Touch", if (engine.lastTouchTimeMs > 0) "${(System.currentTimeMillis() - engine.lastTouchTimeMs) / 1000}s ago" else "None", false)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Button(
        onClick = { engine.touchRobot() },
        modifier = Modifier.fillMaxWidth().height(38.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RobotAccentBlue),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Trigger Virtual Touch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Sound & Noise ADC
    SensorDetailCard(
      title = "Sound Microphone ADC (GPIO 34)",
      icon = Icons.Default.Mic,
      color = RobotWarning
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        SubMetric("Noise Level", "${engine.noiseLevel.roundToInt()}%", engine.noiseLevel > 70f)
        val soundCategory = when {
          engine.noiseLevel < 30f -> "Quiet"
          engine.noiseLevel < 70f -> "Normal"
          else -> "Loud (Alert!)"
        }
        SubMetric("Acoustic State", soundCategory, engine.noiseLevel > 70f)
      }
      Slider(
        value = engine.noiseLevel,
        onValueChange = {
          engine.noiseLevel = it
          if (it > 80f) {
            engine.emotions = engine.emotions.copy(stress = minOf(100f, engine.emotions.stress + 10f))
            engine.log(com.example.model.LogCategory.SENSOR, "Loud noise spike detected: ${it.toInt()}%")
          }
        },
        valueRange = 0f..100f,
        colors = SliderDefaults.colors(thumbColor = RobotWarning, activeTrackColor = RobotWarning)
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 4. LDR Light Sensor
    SensorDetailCard(
      title = "Ambient Light LDR (GPIO 35)",
      icon = Icons.Default.WbSunny,
      color = RobotWarning
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        SubMetric("Illuminance", "${engine.lightLevel.roundToInt()}%", false)
        val lightState = when {
          engine.lightLevel < 25f -> "Dark (Sleep Trigger)"
          engine.lightLevel < 75f -> "Normal Daylight"
          else -> "Bright Studio"
        }
        SubMetric("Lighting Mode", lightState, engine.lightLevel < 25f)
      }
      Slider(
        value = engine.lightLevel,
        onValueChange = {
          engine.lightLevel = it
          if (it < 20f) {
            engine.emotions = engine.emotions.copy(sleepiness = minOf(100f, engine.emotions.sleepiness + 15f))
          }
        },
        valueRange = 0f..100f,
        colors = SliderDefaults.colors(thumbColor = RobotWarning, activeTrackColor = RobotWarning)
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 5. DHT22 Temperature & Humidity
    SensorDetailCard(
      title = "DHT22 Climate Sensor (GPIO 16)",
      icon = Icons.Default.Thermostat,
      color = RobotSuccess
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        SubMetric("Temperature", String.format("%.1f°C", engine.temperature), engine.temperature > 35f)
        SubMetric("Humidity", "${engine.humidity.roundToInt()}%", false)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text("Simulate Temperature (°C):", fontSize = 11.sp, color = RobotTextSecondary)
      Slider(
        value = engine.temperature,
        onValueChange = { engine.temperature = it },
        valueRange = 10f..45f,
        colors = SliderDefaults.colors(thumbColor = RobotSuccess, activeTrackColor = RobotSuccess)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 6. Environment Simulation Presets (Section 34)
    Text(
      text = "ENVIRONMENT SIMULATION PRESETS",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = RobotAccentBlue,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    val presets = listOf(
      "Normal" to {
        engine.lightLevel = 75f
        engine.noiseLevel = 30f
        engine.temperature = 26f
        engine.humidity = 60f
        engine.distanceCenter = 120f
      },
      "Dark Room" to {
        engine.lightLevel = 10f
        engine.emotions = engine.emotions.copy(sleepiness = 85f)
      },
      "Crowded" to {
        engine.noiseLevel = 85f
        engine.distanceCenter = 32f
        engine.emotions = engine.emotions.copy(stress = 65f)
      },
      "Quiet Room" to {
        engine.noiseLevel = 12f
        engine.emotions = engine.emotions.copy(stress = 10f)
      },
      "Hot Room" to {
        engine.temperature = 38f
      },
      "Cold Room" to {
        engine.temperature = 14f
      },
      "Obstacle Nearby" to {
        engine.distanceCenter = 22f
      },
      "Low Battery" to {
        engine.batteryPercent = 14f
      }
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      presets.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          row.forEach { (name, action) ->
            OutlinedButton(
              onClick = action,
              modifier = Modifier.weight(1f).height(38.dp),
              shape = RoundedCornerShape(8.dp),
              border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
            ) {
              Text(name, fontSize = 11.sp, color = RobotCyan, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SensorDetailCard(
  title: String,
  icon: ImageVector,
  color: Color,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = RobotPanel),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RobotTextPrimary)
      }
      Spacer(modifier = Modifier.height(10.dp))
      content()
    }
  }
}

@Composable
private fun SubMetric(label: String, value: String, warning: Boolean) {
  Column {
    Text(text = label, fontSize = 10.sp, color = RobotTextMuted)
    Text(
      text = value,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = if (warning) RobotWarning else RobotTextPrimary,
      fontFamily = FontFamily.Monospace
    )
  }
}
