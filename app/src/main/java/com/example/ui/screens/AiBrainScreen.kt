package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RobotSimulationEngine
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun AiBrainScreen(
  engine: RobotSimulationEngine,
  onOpenExplainability: () -> Unit,
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
    // 1. Current Selected Behavior & Explainability (Section 31 & 65)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("ai_decision_card"),
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
            Icon(Icons.Default.Psychology, contentDescription = "Brain", tint = RobotCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "SELECTED BEHAVIOR",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = RobotAccentBlue,
              letterSpacing = 1.sp
            )
          }

          Button(
            onClick = onOpenExplainability,
            colors = ButtonDefaults.buttonColors(containerColor = RobotElevatedPanel),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text("Why?", fontSize = 11.sp, color = RobotCyan, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = engine.behavior,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = RobotTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Confidence: ${(engine.behaviorConfidence * 100).roundToInt()}%",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = RobotSuccess
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Reason: ${engine.behaviorReason}",
          fontSize = 12.sp,
          color = RobotTextSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Priority Engine Layers (Section 32)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "PRIORITY ENGINE HIERARCHY",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        val layers = listOf(
          1 to "Critical Safety (Collision emergency stop)",
          2 to "Battery Health (Low power rest)",
          3 to "Collision Avoidance (Distance < 40cm sweep)",
          4 to "User Interaction (Touch / Voice command)",
          5 to "Navigation (Autonomous exploration)",
          6 to "Emotional Response (Mood gestures)",
          7 to "Play / Game Mode",
          8 to "Idle Micro-Movements"
        )

        layers.forEach { (num, desc) ->
          val isActiveLayer = engine.priorityLayer == num
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            shape = RoundedCornerShape(6.dp),
            color = if (isActiveLayer) RobotAccentBlue.copy(alpha = 0.2f) else Color.Transparent,
            border = if (isActiveLayer) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotCyan)) else null
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "L$num",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActiveLayer) RobotCyan else RobotTextMuted,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(28.dp)
              )
              Text(
                text = desc,
                fontSize = 11.sp,
                color = if (isActiveLayer) RobotTextPrimary else RobotTextSecondary,
                fontWeight = if (isActiveLayer) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Current Sensor & AI Input Vectors (Section 31)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "CURRENT SENSOR FUSION INPUTS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        InputRow("Battery Level", "${engine.batteryPercent.roundToInt()}% (${String.format("%.2fV", engine.batteryVoltage)})")
        InputRow("Ultrasonic Distance", "${engine.distanceCenter.roundToInt()} cm")
        InputRow("Capacitive Touch", if (engine.isTouchActive) "Active" else "Idle")
        InputRow("Microphone Sound", "${engine.noiseLevel.roundToInt()}%")
        InputRow("LDR Light", "${engine.lightLevel.roundToInt()}%")
        InputRow("Temperature", String.format("%.1f°C", engine.temperature))
        InputRow("Derived Mood", engine.currentMood.label)
        InputRow("Recent Memories", "${engine.memoryEvents.size} records")
      }
    }
  }
}

@Composable
private fun InputRow(key: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = key, fontSize = 11.sp, color = RobotTextSecondary)
    Text(
      text = value,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = RobotTextPrimary,
      fontFamily = FontFamily.Monospace
    )
  }
}
