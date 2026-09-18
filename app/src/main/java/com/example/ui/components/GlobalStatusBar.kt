package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RobotMode
import com.example.ui.theme.*

@Composable
fun GlobalStatusBar(
  mode: RobotMode,
  behavior: String,
  wifiConnected: Boolean,
  bluetoothConnected: Boolean,
  fps: Int = 60,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(RobotBgDark)
      .navigationBarsPadding()
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically
    ) {
      StatusItem(label = "ESP32", value = "ONLINE", color = RobotSuccess)
      StatusDivider()
      StatusItem(label = "CORE", value = "SIMULATION", color = RobotWarning)
      StatusDivider()
      StatusItem(label = "LOOP", value = "$fps FPS", color = RobotCyan)
      StatusDivider()
      StatusItem(label = "WIFI", value = if (wifiConnected) "CONNECTED" else "OFFLINE", color = if (wifiConnected) RobotSuccess else RobotDanger)
      StatusDivider()
      StatusItem(label = "BT", value = if (bluetoothConnected) "CONNECTED" else "DISCONNECTED", color = if (bluetoothConnected) RobotSuccess else RobotTextMuted)
      StatusDivider()
      StatusItem(label = "MODE", value = mode.label, color = RobotAccentBlue)
      StatusDivider()
      StatusItem(label = "TASK", value = behavior, color = RobotTextPrimary)
    }
  }
}

@Composable
private fun StatusItem(label: String, value: String, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = RobotTextMuted,
      fontFamily = FontFamily.Monospace
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = value,
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      color = color,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun StatusDivider() {
  Text(
    text = " • ",
    fontSize = 10.sp,
    color = RobotBorder,
    modifier = Modifier.padding(horizontal = 4.dp)
  )
}
