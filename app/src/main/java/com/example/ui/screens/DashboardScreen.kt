package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RobotSimulationEngine
import com.example.model.EmotionState
import com.example.model.LogEntry
import com.example.ui.components.Robot3DCard
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
  engine: RobotSimulationEngine,
  onRobotTouch: () -> Unit,
  onExplainClick: () -> Unit,
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
    // 1. Hero Robot Visual Card (Section 13)
    Robot3DCard(
      robotName = "ARIA",
      currentMood = engine.currentMood,
      currentEmotion = engine.emotions.happiness.roundToInt().toString(),
      currentBehavior = engine.behavior,
      servoAngle = engine.servoAngle,
      leftMotorSpeed = engine.leftMotorSpeed,
      rightMotorSpeed = engine.rightMotorSpeed,
      expression = engine.currentExpression,
      blinkProgress = engine.blinkProgress,
      gazeX = engine.gazeX,
      gazeY = engine.gazeY,
      rgbRingColors = engine.rgbRingColors,
      batteryPercent = engine.batteryPercent,
      distanceCenter = engine.distanceCenter,
      isTouchActive = engine.isTouchActive,
      onTouchTap = onRobotTouch
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Quick Telemetry Grid (Section 14)
    Text(
      text = "QUICK TELEMETRY",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = RobotAccentBlue,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      TelemetryCard(
        title = "Battery",
        value = "${engine.batteryPercent.roundToInt()}%",
        subtext = String.format("%.2fV", engine.batteryVoltage),
        icon = Icons.Default.BatteryChargingFull,
        color = if (engine.batteryPercent > 25f) RobotSuccess else RobotDanger,
        modifier = Modifier.weight(1f)
      )
      TelemetryCard(
        title = "Distance",
        value = "${engine.distanceCenter.roundToInt()} cm",
        subtext = if (engine.distanceCenter < 50f) "Obstructed" else "Clear",
        icon = Icons.Default.Sensors,
        color = if (engine.distanceCenter < 50f) RobotWarning else RobotCyan,
        modifier = Modifier.weight(1f)
      )
      TelemetryCard(
        title = "Servo",
        value = "${engine.servoAngle.roundToInt()}°",
        subtext = "Heading",
        icon = Icons.Default.RotateRight,
        color = RobotAccentBlue,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      TelemetryCard(
        title = "Temperature",
        value = String.format("%.1f°C", engine.temperature),
        subtext = "DHT22 Sensor",
        icon = Icons.Default.Thermostat,
        color = RobotWarning,
        modifier = Modifier.weight(1f)
      )
      TelemetryCard(
        title = "Humidity",
        value = "${engine.humidity.roundToInt()}%",
        subtext = "Relative RH",
        icon = Icons.Default.WaterDrop,
        color = RobotCyan,
        modifier = Modifier.weight(1f)
      )
      TelemetryCard(
        title = "FPS",
        value = "60",
        subtext = "Target 60Hz",
        icon = Icons.Default.Speed,
        color = RobotSuccess,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 3. Emotion Overview (Section 15)
    Card(
      modifier = Modifier.fillMaxWidth(),
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
          Text(
            text = "EMOTION OVERVIEW",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = RobotAccentBlue,
            letterSpacing = 1.sp
          )
          Text(
            text = "Mood: ${engine.currentMood.label} (${engine.moodIntensity.roundToInt()}%)",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = RobotCyan
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        EmotionBarItem("Happiness", engine.emotions.happiness, RobotSuccess)
        EmotionBarItem("Energy", engine.emotions.energy, RobotCyan)
        EmotionBarItem("Trust", engine.emotions.trust, RobotAccentBlue)
        EmotionBarItem("Curiosity", engine.emotions.curiosity, RobotWarning)
        EmotionBarItem("Fear", engine.emotions.fear, RobotDanger)
        EmotionBarItem("Friendship", engine.emotions.friendship, RobotSuccess)
        EmotionBarItem("Boredom", engine.emotions.boredom, RobotTextMuted)
        EmotionBarItem("Stress", engine.emotions.stress, RobotDanger)
        EmotionBarItem("Sleepiness", engine.emotions.sleepiness, Color(0xFF7E57C2))
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. Current Behavior Card (Section 16 & 65)
    Card(
      modifier = Modifier.fillMaxWidth(),
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
          Text(
            text = "CURRENT BEHAVIOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RobotTextMuted,
            letterSpacing = 1.sp
          )
          Button(
            onClick = onExplainClick,
            modifier = Modifier.height(28.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RobotElevatedPanel),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text("Why?", fontSize = 11.sp, color = RobotCyan, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = engine.behavior,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = RobotTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Reason: ${engine.behaviorReason}",
          fontSize = 12.sp,
          color = RobotTextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Confidence: ${(engine.behaviorConfidence * 100).roundToInt()}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = RobotSuccess
          )
          Text(
            text = "Priority Layer: ${engine.priorityLayer}",
            fontSize = 11.sp,
            color = RobotTextMuted
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 5. Live Activity Feed (Section 17)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "LIVE ACTIVITY FEED",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val recentLogs = engine.logs.take(6)
        if (recentLogs.isEmpty()) {
          Text(
            text = "No recent events recorded yet.",
            fontSize = 12.sp,
            color = RobotTextMuted,
            modifier = Modifier.padding(vertical = 8.dp)
          )
        } else {
          recentLogs.forEach { log ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = log.timestampStr,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = RobotTextMuted
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = log.message,
                fontSize = 12.sp,
                color = RobotTextPrimary,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TelemetryCard(
  title: String,
  value: String,
  subtext: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = RobotPanel),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, fontSize = 10.sp, color = RobotTextMuted, fontWeight = FontWeight.Bold)
        Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(14.dp))
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RobotTextPrimary)
      Text(text = subtext, fontSize = 9.sp, color = RobotTextSecondary)
    }
  }
}

@Composable
private fun EmotionBarItem(name: String, value: Float, color: Color) {
  val animatedValue by animateFloatAsState(targetValue = value / 100f, label = name)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = name,
      fontSize = 11.sp,
      color = RobotTextSecondary,
      modifier = Modifier.width(80.dp)
    )
    LinearProgressIndicator(
      progress = { animatedValue },
      modifier = Modifier
        .weight(1f)
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = color,
      trackColor = RobotElevatedPanel
    )
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = value.roundToInt().toString(),
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = RobotTextPrimary,
      fontFamily = FontFamily.Monospace,
      modifier = Modifier.width(26.dp)
    )
  }
}
