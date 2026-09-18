package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.RobotSimulationEngine
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun ExplainabilityDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  engine: RobotSimulationEngine
) {
  if (!isOpen) return

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .testTag("ai_explainability_dialog"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Psychology, contentDescription = "AI Explainability", tint = RobotCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "AI DECISION EXPLAINABILITY",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = RobotAccentBlue,
              letterSpacing = 1.sp
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = RobotTextMuted)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = RobotElevatedPanel,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "CURRENT BEHAVIOR",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = RobotTextMuted
            )
            Text(
              text = engine.behavior,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = RobotCyan
            )
            Text(
              text = "Confidence: ${(engine.behaviorConfidence * 100).roundToInt()}% • Priority Layer: ${engine.priorityLayer}",
              fontSize = 11.sp,
              color = RobotSuccess
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "PRIMARY CAUSAL FACTORS",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          FactorRow("Trigger Reason", engine.behaviorReason)
          FactorRow("Mood Influence", "${engine.currentMood.label} (${engine.moodIntensity.roundToInt()}%)")
          FactorRow("Ultrasonic Reading", "${engine.distanceCenter.roundToInt()} cm")
          FactorRow("Ambient Lighting", "${engine.lightLevel.roundToInt()}% (LDR)")
          FactorRow("Battery Health", "${engine.batteryPercent.roundToInt()}% (${String.format("%.2f", engine.batteryVoltage)}V)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "EVALUATED ALTERNATIVES",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        val alternatives = listOf(
          "Explore Room" to 0.72f,
          "Look Around" to 0.63f,
          "Dance Party" to 0.18f,
          "Rest / Sleep" to 0.12f
        )

        alternatives.forEach { (name, conf) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = name, fontSize = 11.sp, color = RobotTextSecondary)
            Text(
              text = "${(conf * 100).roundToInt()}%",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = RobotTextMuted,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(38.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RobotAccentBlue),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Got It", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun FactorRow(factor: String, detail: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = factor, fontSize = 11.sp, color = RobotTextSecondary)
    Text(
      text = detail,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = RobotTextPrimary,
      fontFamily = FontFamily.Monospace
    )
  }
}
