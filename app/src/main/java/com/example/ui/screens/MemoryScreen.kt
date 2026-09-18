package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
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

@Composable
fun MemoryScreen(
  engine: RobotSimulationEngine,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(RobotBgDark)
      .padding(16.dp)
  ) {
    // 1. Long-Term Permanent Memory Card (Section 48)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("memory_profile_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Memory, contentDescription = "Memory", tint = RobotCyan)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "EEPROM & FLASH PERSISTENT MEMORY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RobotAccentBlue,
            letterSpacing = 1.sp
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          MemoryFact("Total Interactions", "${engine.touchCount} pats")
          MemoryFact("Friendship Level", "${engine.emotions.friendship.toInt()}%")
          MemoryFact("Trust Rating", "${engine.emotions.trust.toInt()}%")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          MemoryFact("Known Obstacles", "${engine.obstacles.size} objects")
          MemoryFact("Personality", "Friendly")
          MemoryFact("State Storage", "Non-Volatile")
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "EPISODIC TIMELINE MEMORIES",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = RobotAccentBlue,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    // 2. Memory Events Log
    LazyColumn(
      modifier = Modifier.fillMaxWidth().weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(engine.memoryEvents) { memory ->
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = RobotElevatedPanel,
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = memory.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RobotCyan
              )
              Text(
                text = memory.timestampStr,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = RobotTextMuted
              )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = memory.description,
              fontSize = 11.sp,
              color = RobotTextSecondary
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MemoryFact(label: String, value: String) {
  Column {
    Text(text = label, fontSize = 10.sp, color = RobotTextMuted)
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = RobotTextPrimary,
      fontFamily = FontFamily.Monospace
    )
  }
}
