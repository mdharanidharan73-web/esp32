package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RobotSystemStatus
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun TopNavBar(
  status: RobotSystemStatus,
  batteryPercent: Float,
  wifiConnected: Boolean,
  bluetoothConnected: Boolean,
  onOpenDrawer: () -> Unit,
  onOpenCommandPalette: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("top_nav_bar"),
    color = RobotPanel,
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
  ) {
    Column {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left: Drawer menu toggle & Title
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onOpenDrawer,
            modifier = Modifier.size(36.dp).testTag("drawer_menu_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.MenuOpen,
              contentDescription = "Open Navigation Menu",
              tint = RobotAccentBlue
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Column {
            Text(
              text = "Ultimate AI Companion",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = RobotTextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "V5.0",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = RobotCyan
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "• SIMULATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = RobotWarning
              )
            }
          }
        }

        // Right Action Items
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Status Badge
          StatusBadge(status = status)

          Spacer(modifier = Modifier.width(8.dp))

          // Battery indicator
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
          ) {
            Icon(
              imageVector = if (batteryPercent > 20f) Icons.Default.BatteryFull else Icons.Default.BatteryAlert,
              contentDescription = "Battery",
              tint = if (batteryPercent > 25f) RobotSuccess else RobotDanger,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "${batteryPercent.roundToInt()}%",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = RobotTextPrimary
            )
          }

          // Command Palette button
          IconButton(
            onClick = onOpenCommandPalette,
            modifier = Modifier.size(36.dp).testTag("command_palette_button")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Command Palette (Ctrl+K)",
              tint = RobotTextSecondary
            )
          }

          // Settings button
          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(36.dp).testTag("settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = RobotTextSecondary
            )
          }
        }
      }
    }
  }
}

@Composable
fun StatusBadge(status: RobotSystemStatus) {
  val (color, label) = when (status) {
    RobotSystemStatus.ONLINE -> RobotSuccess to "ONLINE"
    RobotSystemStatus.IDLE -> RobotAccentBlue to "IDLE"
    RobotSystemStatus.THINKING -> RobotCyan to "THINKING"
    RobotSystemStatus.MOVING -> RobotWarning to "MOVING"
    RobotSystemStatus.SLEEPING -> Color(0xFF9E9E9E) to "SLEEPING"
    RobotSystemStatus.LOW_BATTERY -> RobotDanger to "LOW BATTERY"
  }

  Surface(
    shape = RoundedCornerShape(6.dp),
    color = color.copy(alpha = 0.15f),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.5f)))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}
