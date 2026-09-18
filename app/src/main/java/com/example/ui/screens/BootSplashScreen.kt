package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun BootSplashScreen(
  isBooting: Boolean,
  bootProgress: Float,
  bootLogLines: List<String>,
  onStartSimulation: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF04060A), RobotBgDark, Color(0xFF0D1420))
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp)
    ) {
      // Robot Icon with animated glow
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(RobotAccentBlue.copy(alpha = 0.2f))
          .border(2.dp, RobotCyan, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.SmartToy,
          contentDescription = "Robot Icon",
          tint = RobotCyan,
          modifier = Modifier.size(40.dp)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "ULTIMATE AI",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp,
        color = RobotAccentBlue
      )
      Text(
        text = "COMPANION ROBOT",
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        color = RobotTextPrimary
      )
      Text(
        text = "V5.0",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = RobotCyan
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "ESP32 AI ROBOT SIMULATOR • DIGITAL TWIN",
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        color = RobotTextSecondary
      )

      Spacer(modifier = Modifier.height(28.dp))

      if (isBooting || bootLogLines.isNotEmpty()) {
        // Boot Diagnostic Terminal
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF030508)),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            LinearProgressIndicator(
              progress = { bootProgress },
              modifier = Modifier.fillMaxWidth().height(4.dp),
              color = RobotCyan,
              trackColor = RobotBorder
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
              items(bootLogLines) { line ->
                Text(
                  text = line,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  color = if (line.contains("ONLINE")) RobotSuccess else RobotTextSecondary,
                  fontWeight = if (line.contains("ONLINE")) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(vertical = 1.dp)
                )
              }
            }
          }
        }
      } else {
        // Start Simulation Button
        Button(
          onClick = onStartSimulation,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("start_simulation_button"),
          colors = ButtonDefaults.buttonColors(containerColor = RobotAccentBlue),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Start")
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "START SIMULATION",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = onStartSimulation,
          modifier = Modifier.fillMaxWidth().height(44.dp),
          shape = RoundedCornerShape(12.dp),
          border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
        ) {
          Text(
            text = "OPEN PREVIOUS SESSION",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = RobotTextSecondary
          )
        }
      }
    }
  }
}
