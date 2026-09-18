package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
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
import com.example.model.LogCategory
import com.example.ui.theme.*

@Composable
fun DebugLogsScreen(
  engine: RobotSimulationEngine,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf<LogCategory?>(null) }
  var searchText by remember { mutableStateOf("") }

  val filteredLogs = remember(engine.logs.size, selectedCategory, searchText) {
    engine.logs.filter { log ->
      (selectedCategory == null || log.category == selectedCategory) &&
        (searchText.isBlank() || log.message.contains(searchText, ignoreCase = true))
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(RobotBgDark)
      .padding(16.dp)
  ) {
    // 1. Search & Filter Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text("Filter logs...", fontSize = 12.sp, color = RobotTextMuted) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = RobotCyan, modifier = Modifier.size(18.dp)) },
        singleLine = true,
        modifier = Modifier.weight(1f).height(48.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = RobotAccentBlue,
          unfocusedBorderColor = RobotBorder,
          focusedTextColor = RobotTextPrimary,
          unfocusedTextColor = RobotTextPrimary
        )
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = { engine.logs.clear() },
        colors = IconButtonDefaults.iconButtonColors(containerColor = RobotElevatedPanel)
      ) {
        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = RobotDanger)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 2. Category Chips
    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      item {
        FilterChip(
          selected = selectedCategory == null,
          onClick = { selectedCategory = null },
          label = { Text("ALL", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RobotAccentBlue,
            selectedLabelColor = Color.White
          )
        )
      }
      items(LogCategory.entries) { cat ->
        FilterChip(
          selected = selectedCategory == cat,
          onClick = { selectedCategory = cat },
          label = { Text(cat.name, fontSize = 10.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RobotAccentBlue,
            selectedLabelColor = Color.White
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Logs List
    Card(
      modifier = Modifier.fillMaxWidth().weight(1f).testTag("debug_logs_list"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF04060A)),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(12.dp)
    ) {
      if (filteredLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("No log entries match criteria.", fontSize = 12.sp, color = RobotTextMuted)
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(filteredLogs) { log ->
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text(
                text = log.timestampStr,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = RobotTextMuted,
                modifier = Modifier.width(62.dp)
              )

              val catColor = when (log.category) {
                LogCategory.SYSTEM -> RobotAccentBlue
                LogCategory.AI -> RobotCyan
                LogCategory.NAVIGATION -> RobotSuccess
                LogCategory.EMOTION -> Color(0xFFFF5D73)
                LogCategory.SENSOR -> RobotWarning
                LogCategory.VOICE -> Color(0xFFAB47BC)
                else -> RobotTextSecondary
              }

              Surface(
                shape = RoundedCornerShape(3.dp),
                color = catColor.copy(alpha = 0.15f),
                modifier = Modifier.padding(end = 6.dp)
              ) {
                Text(
                  text = log.category.name,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  color = catColor,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }

              Text(
                text = log.message,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
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
