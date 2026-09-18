package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RobotSimulationEngine
import com.example.model.PersonalityPreset
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun EmotionsScreen(
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
    // 1. Current Derived Mood Hero Card (Section 29)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("mood_overview_card"),
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
            Icon(Icons.Default.Mood, contentDescription = "Mood", tint = RobotCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "CURRENT MOOD DERIVATION",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = RobotAccentBlue,
              letterSpacing = 1.sp
            )
          }
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = RobotCyan.copy(alpha = 0.2f)
          ) {
            Text(
              text = "${engine.moodIntensity.roundToInt()}% INTENSITY",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = RobotCyan,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = engine.currentMood.label,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = RobotTextPrimary
        )

        Text(
          text = engine.currentMood.description,
          fontSize = 12.sp,
          color = RobotTextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Dominant Factors: ${engine.dominantFactors}",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = RobotWarning
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Emotion Timeline Graph (Section 28)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "EMOTION HISTORY TIMELINE",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // History Chart
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(Color(0xFF070B12), RoundedCornerShape(10.dp))
            .padding(8.dp)
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val history = engine.emotionHistory
            if (history.size > 1) {
              val w = size.width
              val h = size.height

              // Grid lines
              drawLine(Color(0xFF1B2433), Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 1f)
              drawLine(Color(0xFF1B2433), Offset(0f, h * 0.50f), Offset(w, h * 0.50f), strokeWidth = 1f)
              drawLine(Color(0xFF1B2433), Offset(0f, h * 0.75f), Offset(w, h * 0.75f), strokeWidth = 1f)

              val hapPath = Path()
              val curPath = Path()

              for (i in history.indices) {
                val sample = history[i]
                val x = (i / (history.size - 1).toFloat()) * w
                val yHap = h - (sample.happiness / 100f) * h
                val yCur = h - (sample.curiosity / 100f) * h

                if (i == 0) {
                  hapPath.moveTo(x, yHap)
                  curPath.moveTo(x, yCur)
                } else {
                  hapPath.lineTo(x, yHap)
                  curPath.lineTo(x, yCur)
                }
              }

              drawPath(hapPath, color = RobotSuccess, style = Stroke(width = 3f))
              drawPath(curPath, color = RobotCyan, style = Stroke(width = 2.5f))
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(RobotSuccess))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Happiness", fontSize = 10.sp, color = RobotTextSecondary)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(RobotCyan))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Curiosity", fontSize = 10.sp, color = RobotTextSecondary)
          }
          Text("Realtime Window", fontSize = 10.sp, color = RobotTextMuted)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Personality Engine & Presets (Section 30)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "PERSONALITY ENGINE & PRESETS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PersonalityPreset.entries.take(4).forEach { preset ->
            FilterChip(
              selected = engine.personality == preset.traits,
              onClick = { engine.personality = preset.traits },
              label = { Text(preset.title, fontSize = 10.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RobotAccentBlue,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PersonalityPreset.entries.drop(4).forEach { preset ->
            FilterChip(
              selected = engine.personality == preset.traits,
              onClick = { engine.personality = preset.traits },
              label = { Text(preset.title, fontSize = 10.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RobotAccentBlue,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Traits Sliders
        PersonalitySlider("Playfulness", engine.personality.playfulness) {
          engine.personality = engine.personality.copy(playfulness = it)
        }
        PersonalitySlider("Curiosity", engine.personality.curiosity) {
          engine.personality = engine.personality.copy(curiosity = it)
        }
        PersonalitySlider("Caution", engine.personality.caution) {
          engine.personality = engine.personality.copy(caution = it)
        }
        PersonalitySlider("Energy", engine.personality.energy) {
          engine.personality = engine.personality.copy(energy = it)
        }
        PersonalitySlider("Sociability", engine.personality.sociability) {
          engine.personality = engine.personality.copy(sociability = it)
        }
        PersonalitySlider("Sensitivity", engine.personality.sensitivity) {
          engine.personality = engine.personality.copy(sensitivity = it)
        }
        PersonalitySlider("Patience", engine.personality.patience) {
          engine.personality = engine.personality.copy(patience = it)
        }
      }
    }
  }
}

@Composable
private fun PersonalitySlider(title: String, value: Float, onChange: (Float) -> Unit) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(text = title, fontSize = 11.sp, color = RobotTextSecondary)
      Text(text = "${value.roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RobotCyan, fontFamily = FontFamily.Monospace)
    }
    Slider(
      value = value,
      onValueChange = onChange,
      valueRange = 0f..100f,
      colors = SliderDefaults.colors(thumbColor = RobotCyan, activeTrackColor = RobotCyan)
    )
  }
}
