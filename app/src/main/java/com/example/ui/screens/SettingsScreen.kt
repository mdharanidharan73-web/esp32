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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RobotSimulationEngine
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
  engine: RobotSimulationEngine,
  currentTheme: AppThemeMode,
  onThemeChange: (AppThemeMode) -> Unit,
  onRunDemoScenario: () -> Unit,
  onOpenFactoryReset: () -> Unit,
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
      text = "SYSTEM CONFIGURATION",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = RobotAccentBlue,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Theme Selection Card (Section 82)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("theme_selector_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Palette, contentDescription = "Theme", tint = RobotCyan)
          Spacer(modifier = Modifier.width(8.dp))
          Text("APP VISUAL THEME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotTextPrimary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            AppThemeMode.DARK to "Dark Slate",
            AppThemeMode.DARKER to "Deep Navy",
            AppThemeMode.OLED to "Pure OLED"
          ).forEach { (mode, label) ->
            val isSel = currentTheme == mode
            Surface(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp),
              color = if (isSel) RobotAccentBlue.copy(alpha = 0.25f) else RobotElevatedPanel,
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(if (isSel) RobotCyan else RobotBorder)
              )
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  fontSize = 11.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSel) RobotCyan else RobotTextSecondary
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Master Automated Demo Runner (Section 99)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("demo_scenario_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AutoAwesome, contentDescription = "Demo", tint = Color(0xFFFFC857))
          Spacer(modifier = Modifier.width(8.dp))
          Text("MASTER SYSTEM DEMO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotTextPrimary)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Executes the full automated showcase sequence:\nBoot → Wake → Greet → Blink & Look → Touch reaction → Autonomous explore → Obstacle avoidance → Servo scan → Sleep on dark → Wake on touch.",
          fontSize = 11.sp,
          color = RobotTextSecondary,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onRunDemoScenario,
          colors = ButtonDefaults.buttonColors(containerColor = RobotAccentBlue),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Run Demo")
          Spacer(modifier = Modifier.width(6.dp))
          Text("Run Automated Showcase", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Danger Zone / Factory Reset
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = "Reset", tint = RobotDanger)
          Spacer(modifier = Modifier.width(8.dp))
          Text("RESET & RESTORE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotDanger)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Restores all battery levels, clears episodic flash memory, and resets emotion states to default initial values.",
          fontSize = 11.sp,
          color = RobotTextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onOpenFactoryReset,
          colors = ButtonDefaults.buttonColors(containerColor = RobotDanger),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().height(40.dp)
        ) {
          Text("Factory Reset Robot", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 4. About & Version Spec
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("ABOUT ULTIMATE AI COMPANION V5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotAccentBlue)
        Spacer(modifier = Modifier.height(6.dp))
        Text("• Platform: ESP32 Dual-Core DevKit V1 (240MHz)", fontSize = 11.sp, color = RobotTextSecondary)
        Text("• Framework: Arduino C++ / ESP-IDF with FreeRTOS", fontSize = 11.sp, color = RobotTextSecondary)
        Text("• Digital Twin Architecture: Jetpack Compose 60 FPS Engine", fontSize = 11.sp, color = RobotTextSecondary)
        Text("• Wokwi Online Simulator Ready", fontSize = 11.sp, color = RobotTextSecondary)
      }
    }
  }
}
