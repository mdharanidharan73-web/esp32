package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RobotSimulationEngine
import com.example.model.Obstacle
import com.example.model.Position
import com.example.model.RobotMode
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun SimulatorScreen(
  engine: RobotSimulationEngine,
  onManualMove: (String) -> Unit,
  onReset: () -> Unit,
  onStep: () -> Unit,
  onSpeedChange: (Float) -> Unit,
  onTogglePlay: (Boolean) -> Unit,
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
    // 1. Simulation Toolbar (Section 19)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("simulation_toolbar"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Play / Pause
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { onTogglePlay(!engine.isRunning) },
              colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (engine.isRunning) RobotWarning.copy(alpha = 0.2f) else RobotSuccess.copy(alpha = 0.2f)
              )
            ) {
              Icon(
                imageVector = if (engine.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (engine.isRunning) "Pause" else "Run",
                tint = if (engine.isRunning) RobotWarning else RobotSuccess
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Step Button
            IconButton(
              onClick = onStep,
              colors = IconButtonDefaults.iconButtonColors(containerColor = RobotElevatedPanel)
            ) {
              Icon(Icons.Default.SkipNext, contentDescription = "Step", tint = RobotCyan)
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Reset Button
            IconButton(
              onClick = onReset,
              colors = IconButtonDefaults.iconButtonColors(containerColor = RobotElevatedPanel)
            ) {
              Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = RobotTextSecondary)
            }
          }

          // Mode Toggle (AUTO vs MANUAL)
          Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
              selected = engine.mode == RobotMode.AUTONOMOUS || engine.mode == RobotMode.AI,
              onClick = { engine.mode = RobotMode.AUTONOMOUS },
              label = { Text("AUTO", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RobotAccentBlue,
                selectedLabelColor = Color.White
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            FilterChip(
              selected = engine.mode == RobotMode.MANUAL,
              onClick = { engine.mode = RobotMode.MANUAL },
              label = { Text("MANUAL", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RobotCyan,
                selectedLabelColor = Color.Black
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Speed Buttons & FPS
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(0.25f, 0.5f, 1.0f, 2.0f, 4.0f).forEach { speed ->
              val isSel = abs(engine.simulationSpeed - speed) < 0.05f
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSel) RobotAccentBlue else RobotElevatedPanel)
                  .clickable { onSpeedChange(speed) }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "${speed}x",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSel) Color.White else RobotTextSecondary
                )
              }
            }
          }

          Text(
            text = "60 FPS • ${engine.behavior}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = RobotCyan,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Interactive 2D Robot World Arena (Sections 20, 35, 36)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(380.dp)
        .testTag("robot_world_viewport"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF070B12)),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
              detectTapGestures { tapOffset ->
                // Scale screen tap coordinates to engine arena space
                val normX = (tapOffset.x / size.width) * engine.arenaWidth
                val normY = (tapOffset.y / size.height) * engine.arenaHeight
                // Add or move obstacle to tap location
                val newObsId = "obs_${System.currentTimeMillis() % 1000}"
                engine.obstacles.add(Obstacle(id = newObsId, x = normX, y = normY, radius = 28f, label = "Obstacle"))
                engine.log(com.example.model.LogCategory.NAVIGATION, "Placed obstacle at (${normX.toInt()}, ${normY.toInt()})")
              }
            }
        ) {
          val scaleX = size.width / engine.arenaWidth
          val scaleY = size.height / engine.arenaHeight

          // 1. Technical background grid
          val gridSpacingX = 40f * scaleX
          val gridSpacingY = 40f * scaleY
          var gx = 0f
          while (gx < size.width) {
            drawLine(Color(0xFF131B28), Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 1f)
            gx += gridSpacingX
          }
          var gy = 0f
          while (gy < size.height) {
            drawLine(Color(0xFF131B28), Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
            gy += gridSpacingY
          }

          // 2. Robot Path trail
          if (engine.robotPath.size > 1) {
            val path = Path()
            val first = engine.robotPath.first()
            path.moveTo(first.x * scaleX, first.y * scaleY)
            for (p in engine.robotPath) {
              path.lineTo(p.x * scaleX, p.y * scaleY)
            }
            drawPath(path, color = RobotAccentBlue.copy(alpha = 0.35f), style = Stroke(width = 2.5f))
          }

          // 3. Render Obstacles
          for (obs in engine.obstacles) {
            val ox = obs.x * scaleX
            val oy = obs.y * scaleY
            val r = obs.radius * scaleX
            // Obstacle outer safety boundary
            drawCircle(RobotDanger.copy(alpha = 0.15f), radius = r + 8f, center = Offset(ox, oy))
            // Obstacle solid core
            drawCircle(Color(0xFF263238), radius = r, center = Offset(ox, oy))
            drawCircle(RobotDanger, radius = r, center = Offset(ox, oy), style = Stroke(width = 2f))
          }

          // 4. Ultrasonic scan rays
          val rx = engine.position.x * scaleX
          val ry = engine.position.y * scaleY
          val headAngleRad = Math.toRadians((engine.rotationDeg + (engine.servoAngle - 90f)).toDouble()).toFloat()

          // Sweep cone
          val coneRays = listOf(
            headAngleRad - Math.toRadians(45.0).toFloat() to engine.distanceLeft,
            headAngleRad to engine.distanceCenter,
            headAngleRad + Math.toRadians(45.0).toFloat() to engine.distanceRight
          )

          for ((angle, dist) in coneRays) {
            val hitDist = dist * scaleX
            val rayEndX = rx + cos(angle) * hitDist
            val rayEndY = ry + sin(angle) * hitDist
            val isBlocked = dist < 50f
            val rayColor = if (isBlocked) RobotDanger else RobotCyan

            drawLine(
              color = rayColor.copy(alpha = 0.6f),
              start = Offset(rx, ry),
              end = Offset(rayEndX, rayEndY),
              strokeWidth = 2f
            )
            drawCircle(rayColor, radius = 4f, center = Offset(rayEndX, rayEndY))
          }

          // 5. Robot Physical Body & Chassis
          val robotR = engine.robotRadius * scaleX

          // Outer collision halo
          drawCircle(RobotAccentBlue.copy(alpha = 0.2f), radius = robotR + 6f, center = Offset(rx, ry))

          // Main Chassis
          drawCircle(Color(0xFF1E293B), radius = robotR, center = Offset(rx, ry))
          drawCircle(RobotAccentBlue, radius = robotR, center = Offset(rx, ry), style = Stroke(width = 3f))

          // Heading direction pointer
          val rotRad = Math.toRadians(engine.rotationDeg.toDouble()).toFloat()
          val dirX = rx + cos(rotRad) * (robotR + 10f)
          val dirY = ry + sin(rotRad) * (robotR + 10f)
          drawLine(RobotCyan, Offset(rx, ry), Offset(dirX, dirY), strokeWidth = 3.5f)

          // Servo Head Angle indicator
          val servoDirX = rx + cos(headAngleRad) * (robotR + 14f)
          val servoDirY = ry + sin(headAngleRad) * (robotR + 14f)
          drawCircle(Color(0xFFFFC857), radius = 4.5f, center = Offset(servoDirX, servoDirY))
        }

        // Overlay badges
        Surface(
          modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
          shape = RoundedCornerShape(6.dp),
          color = Color(0xBB000000)
        ) {
          Column(modifier = Modifier.padding(6.dp)) {
            Text(text = "TAP SCREEN TO ADD OBSTACLE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RobotCyan)
            Text(
              text = "L: ${engine.distanceLeft.roundToInt()}cm | C: ${engine.distanceCenter.roundToInt()}cm | R: ${engine.distanceRight.roundToInt()}cm",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = RobotTextPrimary,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Clear Obstacles button
        Button(
          onClick = { engine.obstacles.clear() },
          modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).height(28.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RobotElevatedPanel),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text("Clear", fontSize = 10.sp, color = RobotTextSecondary)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Manual D-Pad Controls (Section 107)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("manual_controls_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "MANUAL MOTION CONTROLS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = RobotAccentBlue,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // D-Pad
        IconButton(
          onClick = { onManualMove("FORWARD") },
          modifier = Modifier.size(44.dp),
          colors = IconButtonDefaults.iconButtonColors(containerColor = RobotElevatedPanel)
        ) {
          Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Forward", tint = RobotCyan)
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { onManualMove("LEFT") },
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = RobotElevatedPanel)
          ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = RobotCyan)
          }

          Button(
            onClick = { onManualMove("STOP") },
            colors = ButtonDefaults.buttonColors(containerColor = RobotDanger),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(48.dp, 44.dp),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text("STOP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }

          IconButton(
            onClick = { onManualMove("RIGHT") },
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = RobotElevatedPanel)
          ) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = RobotCyan)
          }
        }

        IconButton(
          onClick = { onManualMove("BACKWARD") },
          modifier = Modifier.size(44.dp),
          colors = IconButtonDefaults.iconButtonColors(containerColor = RobotElevatedPanel)
        ) {
          Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Backward", tint = RobotCyan)
        }
      }
    }
  }
}
