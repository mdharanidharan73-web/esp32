package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.ui.theme.*

data class BtMessage(val sender: String, val text: String, val time: String)

@Composable
fun ConnectivityScreen(
  engine: RobotSimulationEngine,
  modifier: Modifier = Modifier
) {
  var commandText by remember { mutableStateOf("") }
  var messages by remember {
    mutableStateOf(
      listOf(
        BtMessage("ROBOT", "ESP32 SPP READY. Type HELP for commands.", "12:00:01"),
        BtMessage("USER", "STATUS", "12:00:04"),
        BtMessage("ROBOT", "OK BAT=78% DIST=120 MOOD=HAPPY", "12:00:05")
      )
    )
  }

  fun sendCommand(cmd: String) {
    if (cmd.isBlank()) return
    val userMsg = BtMessage("USER", cmd.uppercase(), "12:00:15")
    val robotReply = when (cmd.uppercase()) {
      "FORWARD", "F" -> { engine.manualMove("FORWARD"); "ACK: MOVING FORWARD" }
      "BACKWARD", "B" -> { engine.manualMove("BACKWARD"); "ACK: MOVING BACKWARD" }
      "LEFT", "L" -> { engine.manualMove("LEFT"); "ACK: TURNING LEFT" }
      "RIGHT", "R" -> { engine.manualMove("RIGHT"); "ACK: TURNING RIGHT" }
      "STOP", "S" -> { engine.manualMove("STOP"); "ACK: STOPPED" }
      "SLEEP" -> { engine.triggerSleep(); "ACK: ENTERING SLEEP MODE" }
      "WAKE" -> { engine.triggerWake(); "ACK: ROBOT AWAKE" }
      "STATUS" -> "BAT=${engine.batteryPercent.toInt()}% V=${String.format("%.2f", engine.batteryVoltage)}V DIST=${engine.distanceCenter.toInt()}cm BEHAVIOR=${engine.behavior}"
      else -> "ACK: EXECUTED COMMAND '$cmd'"
    }
    val botMsg = BtMessage("ROBOT", robotReply, "12:00:16")
    messages = messages + userMsg + botMsg
    engine.log(com.example.model.LogCategory.BLUETOOTH, "BT Command: $cmd -> $robotReply")
    commandText = ""
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(RobotBgDark)
      .padding(16.dp)
  ) {
    // 1. WiFi & Web Server Status Card (Section 43)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Wifi, contentDescription = "WiFi", tint = RobotSuccess)
            Spacer(modifier = Modifier.width(8.dp))
            Text("WIFI & WEB DASHBOARD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RobotAccentBlue, letterSpacing = 1.sp)
          }
          Surface(shape = RoundedCornerShape(4.dp), color = RobotSuccess.copy(alpha = 0.2f)) {
            Text("ONLINE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RobotSuccess, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("IP: 192.168.4.1 (SoftAP)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = RobotCyan)
          Text("Port: 80 / WS: 81", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = RobotTextSecondary)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Bluetooth SPP Terminal (Section 42)
    Text("BLUETOOTH CLASSIC / BLE TERMINAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RobotAccentBlue, letterSpacing = 1.sp)

    Spacer(modifier = Modifier.height(8.dp))

    Card(
      modifier = Modifier.fillMaxWidth().weight(1f).testTag("bluetooth_terminal_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF04060A)),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(14.dp)
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        items(messages) { msg ->
          Row(modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "[${msg.time}] ",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = RobotTextMuted
            )
            Text(
              text = if (msg.sender == "USER") ">> " else "<< ",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = if (msg.sender == "USER") RobotCyan else RobotWarning,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = msg.text,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = if (msg.sender == "USER") RobotTextPrimary else RobotSuccess
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 3. Command sender & quick pills
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      listOf("FORWARD", "LEFT", "STOP", "RIGHT", "STATUS").forEach { quickCmd ->
        OutlinedButton(
          onClick = { sendCommand(quickCmd) },
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
          modifier = Modifier.height(30.dp),
          border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
        ) {
          Text(quickCmd, fontSize = 9.sp, color = RobotCyan, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = commandText,
        onValueChange = { commandText = it },
        placeholder = { Text("Send serial command...", fontSize = 12.sp, color = RobotTextMuted) },
        singleLine = true,
        modifier = Modifier.weight(1f),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = RobotAccentBlue,
          unfocusedBorderColor = RobotBorder,
          focusedTextColor = RobotTextPrimary,
          unfocusedTextColor = RobotTextPrimary
        )
      )
      Spacer(modifier = Modifier.width(8.dp))
      IconButton(
        onClick = { sendCommand(commandText) },
        colors = IconButtonDefaults.iconButtonColors(containerColor = RobotAccentBlue)
      ) {
        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
      }
    }
  }
}
