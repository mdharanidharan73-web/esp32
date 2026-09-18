package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RobotSimulationEngine
import com.example.model.RgbEffectMode
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun HardwareScreen(
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
    // 1. Servo SG90 Virtual Module (Section 38)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("servo_hardware_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RotateRight, contentDescription = "Servo", tint = RobotCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "SERVO SG90 (GPIO 13)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = RobotAccentBlue,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "${engine.servoAngle.roundToInt()}°",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = RobotTextPrimary,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Servo Dial Visualizer
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
          contentAlignment = Alignment.Center
        ) {
          val animatedAngle by animateFloatAsState(targetValue = engine.servoAngle, label = "servoDial")
          Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height - 10f)
            val radius = 70.dp.toPx()

            // Arc track 0 to 180 deg
            drawArc(
              color = RobotBorder,
              startAngle = 180f,
              sweepAngle = 180f,
              useCenter = false,
              topLeft = Offset(center.x - radius, center.y - radius),
              size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
              style = Stroke(width = 6f)
            )

            // Needle angle (0 deg is left, 90 deg is up, 180 deg is right)
            val needleRad = Math.toRadians((180f + animatedAngle).toDouble()).toFloat()
            val needleEnd = Offset(center.x + cos(needleRad) * radius, center.y + sin(needleRad) * radius)

            drawLine(RobotCyan, center, needleEnd, strokeWidth = 3.5f)
            drawCircle(RobotAccentBlue, radius = 6f, center = center)
          }
        }

        Slider(
          value = engine.targetServoAngle,
          onValueChange = { engine.targetServoAngle = it },
          valueRange = 0f..180f,
          colors = SliderDefaults.colors(thumbColor = RobotCyan, activeTrackColor = RobotCyan)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons
        val servoPresets = listOf(
          "LOOK LEFT" to 140f,
          "CENTER" to 90f,
          "LOOK RIGHT" to 40f,
          "CURIOUS" to 110f,
          "SCAN" to 135f
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          servoPresets.forEach { (label, angle) ->
            OutlinedButton(
              onClick = { engine.targetServoAngle = angle },
              modifier = Modifier.weight(1f).height(32.dp),
              contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
              shape = RoundedCornerShape(6.dp),
              border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
            ) {
              Text(label, fontSize = 8.sp, color = RobotCyan, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Dual Motors L298N (Section 39)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("motors_hardware_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Speed, contentDescription = "Motors", tint = RobotWarning)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "DUAL DC MOTORS (L298N DRIVER)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RobotAccentBlue,
            letterSpacing = 1.sp
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MotorChannelCard(
            channel = "LEFT MOTOR",
            speed = engine.leftMotorSpeed,
            pwm = (abs(engine.leftMotorSpeed) * 2.55f).roundToInt(),
            currentMa = 120f + abs(engine.leftMotorSpeed) * 2.1f,
            modifier = Modifier.weight(1f)
          )
          MotorChannelCard(
            channel = "RIGHT MOTOR",
            speed = engine.rightMotorSpeed,
            pwm = (abs(engine.rightMotorSpeed) * 2.55f).roundToInt(),
            currentMa = 120f + abs(engine.rightMotorSpeed) * 2.1f,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. WS2812 12-LED Neopixel Ring (Section 40)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("rgb_hardware_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LightMode, contentDescription = "RGB", tint = RobotSuccess)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "12-LED RGB NEOPIXEL RING (GPIO 4)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = RobotAccentBlue,
              letterSpacing = 1.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Effect Mode Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            RgbEffectMode.AUTO_EMOTION to "Auto Mood",
            RgbEffectMode.CHASE to "Rainbow",
            RgbEffectMode.WARNING to "Warning",
            RgbEffectMode.CHARGING to "Charging"
          ).forEach { (mode, title) ->
            FilterChip(
              selected = engine.rgbEffect == mode,
              onClick = { engine.rgbEffect = mode },
              label = { Text(title, fontSize = 10.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RobotAccentBlue,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Brightness: ${(engine.rgbBrightness * 100).roundToInt()}%", fontSize = 11.sp, color = RobotTextSecondary)
        Slider(
          value = engine.rgbBrightness,
          onValueChange = { engine.rgbBrightness = it },
          valueRange = 0.1f..1.0f,
          colors = SliderDefaults.colors(thumbColor = RobotSuccess, activeTrackColor = RobotSuccess)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. ESP32 Pinout Matrix (Section 59)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "ESP32 DEVKIT V1 PINOUT MATRIX",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        val pinMap = listOf(
          "OLED SDA" to "GPIO 21 (I2C Data)",
          "OLED SCL" to "GPIO 22 (I2C Clock)",
          "HC-SR04 TRIG" to "GPIO 5 (Output)",
          "HC-SR04 ECHO" to "GPIO 18 (Input)",
          "SERVO SG90" to "GPIO 13 (PWM Timer)",
          "L298N ENA / IN1/2" to "GPIO 25, 26, 27",
          "L298N ENB / IN3/4" to "GPIO 14, 32, 33",
          "WS2812 RGB RING" to "GPIO 4 (NeoPixel Data)",
          "TOUCH SENSOR" to "GPIO 15 (Capacitive)",
          "SOUND ADC" to "GPIO 34 (Analog Input)",
          "LDR LIGHT ADC" to "GPIO 35 (Analog Input)",
          "DHT22 SENSOR" to "GPIO 16 (1-Wire)",
          "DFPLAYER RX/TX" to "GPIO 19, 23 (UART2)",
          "BATTERY ADC" to "GPIO 36 (ADC1_CH0)"
        )

        pinMap.chunked(2).forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            row.forEach { (comp, gpio) ->
              Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                color = RobotElevatedPanel
              ) {
                Column(modifier = Modifier.padding(6.dp)) {
                  Text(text = comp, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RobotCyan)
                  Text(text = gpio, fontSize = 9.sp, color = RobotTextSecondary, fontFamily = FontFamily.Monospace)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MotorChannelCard(
  channel: String,
  speed: Float,
  pwm: Int,
  currentMa: Float,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    color = RobotElevatedPanel
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(text = channel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RobotWarning)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = if (speed > 0) "FORWARD" else if (speed < 0) "REVERSE" else "STOP",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (speed != 0f) RobotSuccess else RobotTextMuted
      )
      Text(text = "PWM: $pwm / 255", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = RobotTextSecondary)
      Text(text = "Current: ${currentMa.roundToInt()} mA", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = RobotTextSecondary)
    }
  }
}
