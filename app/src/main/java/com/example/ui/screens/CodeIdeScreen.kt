package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Esp32FirmwareSource
import com.example.model.FirmwareFile
import com.example.ui.theme.*

@Composable
fun CodeIdeScreen(
  selectedIndex: Int,
  onSelectFile: (Int) -> Unit,
  onCopySuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val files = Esp32FirmwareSource.getAllFiles()
  val activeFile = files.getOrElse(selectedIndex) { files.first() }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(RobotBgDark)
      .padding(12.dp)
  ) {
    // 1. File Tabs Bar
    LazyRow(
      modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      itemsIndexed(files) { index: Int, file: FirmwareFile ->
        val isSel = index == selectedIndex
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isSel) RobotAccentBlue.copy(alpha = 0.25f) else RobotElevatedPanel,
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isSel) RobotCyan else RobotBorder)
          ),
          modifier = Modifier.testTag("file_tab_${file.filename}")
        ) {
          Row(
            modifier = Modifier
              .clickable { onSelectFile(index) }
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = file.filename,
              fontSize = 11.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
              color = if (isSel) RobotCyan else RobotTextSecondary,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // 2. Editor Toolbar (Filename, language, Copy button)
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
      color = Color(0xFF0F172A),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = activeFile.filename,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = RobotTextPrimary,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = activeFile.description,
            fontSize = 10.sp,
            color = RobotTextMuted
          )
        }

        Button(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(activeFile.filename, activeFile.content)
            clipboard.setPrimaryClip(clip)
            onCopySuccess()
          },
          colors = ButtonDefaults.buttonColors(containerColor = RobotAccentBlue),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.height(32.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // 3. Code Viewport with Line Numbers
    val codeLines = remember(activeFile.content) { activeFile.content.lines() }

    Surface(
      modifier = Modifier.fillMaxWidth().weight(1f),
      shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
      color = Color(0xFF070B12),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
    ) {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .horizontalScroll(rememberScrollState())
          .verticalScroll(rememberScrollState())
          .padding(8.dp)
      ) {
        // Line numbers column
        Column(modifier = Modifier.padding(end = 12.dp)) {
          codeLines.indices.forEach { idx ->
            Text(
              text = "${idx + 1}",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = Color(0xFF475569)
            )
          }
        }

        // Code content column
        Column {
          codeLines.forEach { line ->
            val color = when {
              line.trimStart().startsWith("//") || line.trimStart().startsWith("/*") -> Color(0xFF64748B)
              line.trimStart().startsWith("#include") || line.trimStart().startsWith("#define") -> Color(0xFFFF5D73)
              line.contains("void") || line.contains("int") || line.contains("float") || line.contains("bool") -> RobotCyan
              else -> RobotTextPrimary
            }
            Text(
              text = line,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = color
            )
          }
        }
      }
    }
  }
}
