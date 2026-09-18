package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RobotSimulationEngine
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun BatteryScreen(
  engine: RobotSimulationEngine,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  val infiniteTransition = rememberInfiniteTransition(label = "chargePulse")
  val chargeGlow by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(RobotBgDark)
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    // 1. Animated Battery Visual Card (Section 44)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("battery_large_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BatteryChargingFull, contentDescription = "Battery", tint = if (engine.batteryPercent > 20f) RobotSuccess else RobotDanger)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "2S LI-ION BATTERY SYSTEM",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = RobotAccentBlue,
              letterSpacing = 1.sp
            )
          }
          if (engine.isCharging) {
            Surface(shape = RoundedCornerShape(4.dp), color = RobotWarning.copy(alpha = 0.2f)) {
              Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = "Charging", tint = RobotWarning, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("CHARGING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RobotWarning)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Battery Canvas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.size(240.dp, 80.dp)) {
            val w = size.width
            val h = size.height
            val capWidth = 14f

            // Battery outer shell
            drawRoundRect(
              color = RobotBorder,
              topLeft = Offset(0f, 0f),
              size = Size(w - capWidth - 4f, h),
              cornerRadius = CornerRadius(12f, 12f),
              style = Stroke(width = 4f)
            )

            // Positive terminal cap
            drawRoundRect(
              color = RobotBorder,
              topLeft = Offset(w - capWidth, h * 0.28f),
              size = Size(capWidth, h * 0.44f),
              cornerRadius = CornerRadius(4f, 4f)
            )

            // Fill level
            val fillPercent = (engine.batteryPercent / 100f).coerceIn(0.04f, 1.0f)
            val fillWidth = (w - capWidth - 16f) * fillPercent
            val fillColor = if (engine.isCharging) RobotWarning else if (engine.batteryPercent > 25f) RobotSuccess else RobotDanger

            drawRoundRect(
              brush = Brush.horizontalGradient(listOf(fillColor.copy(alpha = 0.7f), fillColor)),
              topLeft = Offset(6f, 6f),
              size = Size(fillWidth, h - 12f),
              cornerRadius = CornerRadius(8f, 8f)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "${engine.batteryPercent.roundToInt()}%",
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          color = RobotTextPrimary,
          fontFamily = FontFamily.Monospace
        )

        Text(
          text = "${String.format("%.2f", engine.batteryVoltage)}V (Nominal 7.4V • Max 8.4V)",
          fontSize = 12.sp,
          color = RobotTextSecondary,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Charge Toggle
        Button(
          onClick = { engine.isCharging = !engine.isCharging },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (engine.isCharging) RobotWarning else RobotAccentBlue
          ),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.Bolt, contentDescription = "Charge")
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (engine.isCharging) "Disconnect Charger" else "Connect Virtual USB-C Charger", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Subsystem Drain Breakdown (Section 44)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "SUBSYSTEM POWER CONSUMPTION",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        DrainRow("ESP32 Dual-Core (240MHz + WiFi)", "160 mA", 0.16f)
        DrainRow("L298N Dual Motors (Active)", "380 mA", 0.38f)
        DrainRow("SG90 Micro Servo (Sweeping)", "180 mA", 0.18f)
        DrainRow("WS2812 12-LED Neopixel Ring", "140 mA", 0.14f)
        DrainRow("DFPlayer Mini + 8Ω Speaker", "80 mA", 0.08f)
        DrainRow("Sensors (HC-SR04, DHT22, OLED)", "60 mA", 0.06f)

        HorizontalDivider(color = RobotBorder, modifier = Modifier.padding(vertical = 8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Total Active Current:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotTextPrimary)
          Text("~1,000 mA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotWarning, fontFamily = FontFamily.Monospace)
        }
      }
    }
  }
}

@Composable
private fun DrainRow(name: String, current: String, fraction: Float) {
  Column(modifier = Modifier.padding(vertical = 3.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(text = name, fontSize = 11.sp, color = RobotTextSecondary)
      Text(text = current, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RobotCyan, fontFamily = FontFamily.Monospace)
    }
    Spacer(modifier = Modifier.height(2.dp))
    LinearProgressIndicator(
      progress = { fraction },
      modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
      color = RobotAccentBlue,
      trackColor = RobotElevatedPanel
    )
  }
}
