package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class CommandItem(
  val id: String,
  val title: String,
  val category: String,
  val icon: ImageVector,
  val action: () -> Unit
)

@Composable
fun CommandPaletteDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  commands: List<CommandItem>
) {
  if (!isOpen) return

  var searchFilter by remember { mutableStateOf("") }
  val filteredCommands = remember(searchFilter, commands) {
    if (searchFilter.isBlank()) commands
    else commands.filter { it.title.contains(searchFilter, ignoreCase = true) || it.category.contains(searchFilter, ignoreCase = true) }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .testTag("command_palette_dialog"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Search bar
        OutlinedTextField(
          value = searchFilter,
          onValueChange = { searchFilter = it },
          placeholder = { Text("Type a command or search...", color = RobotTextMuted, fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = RobotCyan) },
          trailingIcon = {
            if (searchFilter.isNotEmpty()) {
              IconButton(onClick = { searchFilter = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", tint = RobotTextMuted)
              }
            }
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RobotAccentBlue,
            unfocusedBorderColor = RobotBorder,
            focusedTextColor = RobotTextPrimary,
            unfocusedTextColor = RobotTextPrimary
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Results list
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
        ) {
          items(filteredCommands) { cmd ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  cmd.action()
                  onDismiss()
                }
                .padding(horizontal = 10.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = cmd.icon,
                contentDescription = cmd.title,
                tint = RobotCyan,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = cmd.title,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = RobotTextPrimary
                )
                Text(
                  text = cmd.category,
                  fontSize = 11.sp,
                  color = RobotTextMuted
                )
              }
              Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Execute",
                tint = RobotTextMuted,
                modifier = Modifier.size(16.dp)
              )
            }
            HorizontalDivider(color = RobotBorder.copy(alpha = 0.5f))
          }
        }
      }
    }
  }
}
