package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.RobotNavDestination
import com.example.ui.theme.*

@Composable
fun AppNavigationDrawerContent(
  currentDestination: RobotNavDestination,
  onSelectDestination: (RobotNavDestination) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxHeight()
      .width(280.dp)
      .background(RobotBgSecondary)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    // Header
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .background(RobotAccentBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.SmartToy,
          contentDescription = "Robot",
          tint = RobotCyan
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = "AI Companion V5",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = RobotTextPrimary
        )
        Text(
          text = "Robotics Lab Digital Twin",
          fontSize = 11.sp,
          color = RobotTextSecondary
        )
      }
    }

    HorizontalDivider(color = RobotBorder, modifier = Modifier.padding(bottom = 12.dp))

    // Categories
    NavCategoryHeader(title = "MAIN WORKSPACE")
    NavItem(RobotNavDestination.DASHBOARD, Icons.Default.Dashboard, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.SIMULATOR, Icons.Default.PrecisionManufacturing, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.ENVIRONMENT, Icons.Default.Layers, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.SENSORS, Icons.Default.Sensors, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.EMOTIONS, Icons.Default.Mood, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.AI_BRAIN, Icons.Default.Psychology, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.MEMORY, Icons.Default.Memory, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.GAMES, Icons.Default.SportsEsports, currentDestination, onSelectDestination)

    Spacer(modifier = Modifier.height(14.dp))
    NavCategoryHeader(title = "VIRTUAL HARDWARE")
    NavItem(RobotNavDestination.SERVO, Icons.Default.RotateRight, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.MOTORS, Icons.Default.Speed, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.OLED, Icons.Default.Face, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.RGB_LEDS, Icons.Default.LightMode, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.VOICE, Icons.Default.RecordVoiceOver, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.BLUETOOTH, Icons.Default.Bluetooth, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.WIFI, Icons.Default.Wifi, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.BATTERY, Icons.Default.BatteryChargingFull, currentDestination, onSelectDestination)

    Spacer(modifier = Modifier.height(14.dp))
    NavCategoryHeader(title = "DEVELOPMENT & TOOLS")
    NavItem(RobotNavDestination.ESP32_CODE, Icons.Default.Code, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.WOKWI, Icons.Default.Hub, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.PINOUT, Icons.Default.SettingsInputComponent, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.DEBUG_CONSOLE, Icons.Default.Terminal, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.LOGS, Icons.Default.FormatListBulleted, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.EXPORT, Icons.Default.Download, currentDestination, onSelectDestination)

    Spacer(modifier = Modifier.height(14.dp))
    NavCategoryHeader(title = "SYSTEM")
    NavItem(RobotNavDestination.SETTINGS, Icons.Default.Tune, currentDestination, onSelectDestination)
    NavItem(RobotNavDestination.ABOUT, Icons.Default.Info, currentDestination, onSelectDestination)
  }
}

@Composable
private fun NavCategoryHeader(title: String) {
  Text(
    text = title,
    fontSize = 10.sp,
    fontWeight = FontWeight.Bold,
    color = RobotAccentBlue,
    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
  )
}

@Composable
private fun NavItem(
  destination: RobotNavDestination,
  icon: ImageVector,
  current: RobotNavDestination,
  onSelect: (RobotNavDestination) -> Unit
) {
  val selected = destination == current
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp)
      .testTag("nav_item_${destination.name.lowercase()}"),
    shape = RoundedCornerShape(8.dp),
    color = if (selected) RobotAccentBlue.copy(alpha = 0.18f) else Color.Transparent
  ) {
    Row(
      modifier = Modifier
        .clickable { onSelect(destination) }
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = destination.title,
        tint = if (selected) RobotCyan else RobotTextSecondary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = destination.title,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) RobotTextPrimary else RobotTextSecondary
      )
    }
  }
}
