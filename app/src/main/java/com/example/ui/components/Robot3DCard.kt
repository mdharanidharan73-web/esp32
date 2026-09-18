package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpressionType
import com.example.model.RobotMood
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun Robot3DCard(
  robotName: String,
  currentMood: RobotMood,
  currentEmotion: String,
  currentBehavior: String,
  servoAngle: Float,
  leftMotorSpeed: Float,
  rightMotorSpeed: Float,
  expression: ExpressionType,
  blinkProgress: Float,
  gazeX: Float,
  gazeY: Float,
  rgbRingColors: List<Long>,
  batteryPercent: Float,
  distanceCenter: Float,
  isTouchActive: Boolean,
  onTouchTap: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Servo tilt angle relative to 90deg center (-45 to +45)
  val tiltAngle by animateFloatAsState(
    targetValue = (servoAngle - 90f) * 0.45f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "headTilt"
  )

  // Floating idle bobbing animation
  val infiniteTransition = rememberInfiniteTransition(label = "idleBob")
  val bobY by infiniteTransition.animateFloat(
    initialValue = -3f,
    targetValue = 3f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "bobY"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("dashboard_robot_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = RobotPanel),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(RobotBorder, RobotElevatedPanel)))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top identity row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(RobotSuccess)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = robotName,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = RobotTextPrimary
            )
          }
          Text(
            text = "$currentMood • $currentBehavior",
            fontSize = 12.sp,
            color = RobotCyan
          )
        }

        // Live Mode Pill
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = RobotElevatedPanel,
          border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(RobotBorder, RobotBorder)))
        ) {
          Text(
            text = "AI DIGITAL TWIN",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = RobotAccentBlue,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Central Interactive Robot Chassis & OLED Viewport
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(230.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(
            Brush.radialGradient(
              colors = listOf(Color(0xFF131B27), Color(0xFF090D14)),
              center = Offset.Unspecified,
              radius = 500f
            )
          )
          .border(1.dp, RobotBorder, RoundedCornerShape(16.dp))
          .clickable { onTouchTap() }
          .testTag("robot_interactive_body"),
        contentAlignment = Alignment.Center
      ) {
        // Background RGB Neopixel Ring glow effect
        Canvas(modifier = Modifier.fillMaxSize()) {
          val centerX = size.width / 2
          val centerY = size.height / 2 + 10f
          val ringRadius = 88.dp.toPx()

          // Draw 12 Neopixel LEDs
          for (i in 0 until 12) {
            val angleRad = Math.toRadians((i * 30.0 - 90)).toFloat()
            val ledX = centerX + cos(angleRad) * ringRadius
            val ledY = centerY + sin(angleRad) * (ringRadius * 0.72f) // slightly elliptical perspective

            val colorVal = if (i < rgbRingColors.size) rgbRingColors[i] else 0xFF32D6FF
            val c = Color(colorVal)

            // Glow aura
            drawCircle(color = c.copy(alpha = 0.25f), radius = 14f, center = Offset(ledX, ledY))
            // LED diode core
            drawCircle(color = c, radius = 5.5f, center = Offset(ledX, ledY))
          }

          // Chassis shadow & tread base
          drawRoundRect(
            color = Color(0xFF070A0F),
            topLeft = Offset(centerX - 95.dp.toPx(), centerY + 58.dp.toPx()),
            size = Size(190.dp.toPx(), 22.dp.toPx()),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
          )
          // Differential Wheels (Left & Right)
          val leftWheelRot = (leftMotorSpeed * 2.5f) % 360f
          val rightWheelRot = (rightMotorSpeed * 2.5f) % 360f

          // Left wheel
          drawRoundRect(
            color = Color(0xFF1B2433),
            topLeft = Offset(centerX - 106.dp.toPx(), centerY + 40.dp.toPx()),
            size = Size(16.dp.toPx(), 44.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
          )
          // Right wheel
          drawRoundRect(
            color = Color(0xFF1B2433),
            topLeft = Offset(centerX + 90.dp.toPx(), centerY + 40.dp.toPx()),
            size = Size(16.dp.toPx(), 44.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
          )
        }

        // Robot Head & Chassis Body (with servo tilt rotation)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .offset(y = bobY.dp)
            .graphicsLayer { rotationZ = tiltAngle }
        ) {
          // Top Antenna
          Box(
            modifier = Modifier
              .width(4.dp)
              .height(14.dp)
              .background(RobotAccentBlue)
          )
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(if (isTouchActive) Color(0xFFFF5D73) else RobotCyan)
          )

          Spacer(modifier = Modifier.height(3.dp))

          // Head / Face Enclosure
          Box(
            modifier = Modifier
              .width(185.dp)
              .height(125.dp)
              .clip(RoundedCornerShape(22.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(Color(0xFF222C3D), Color(0xFF151D2A), Color(0xFF0F1520))
                )
              )
              .border(2.dp, if (isTouchActive) RobotCyan else RobotAccentBlue.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
              .padding(8.dp),
            contentAlignment = Alignment.Center
          ) {
            // Ultrasonic transceiver "ears" on head sides
            Box(
              modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-14).dp)
                .size(10.dp, 20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(RobotAccentBlue)
            )
            Box(
              modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 14.dp)
                .size(10.dp, 20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(RobotAccentBlue)
            )

            // Inner OLED Display
            OledFaceView(
              expression = expression,
              blinkProgress = blinkProgress,
              gazeX = gazeX,
              gazeY = gazeY,
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp)
            )
          }

          // Neck / Servo articulation swivel
          Box(
            modifier = Modifier
              .width(36.dp)
              .height(10.dp)
              .clip(RoundedCornerShape(3.dp))
              .background(Color(0xFF2A374A))
          )
        }

        // Touch Hint Floating Badge
        Surface(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(10.dp),
          shape = RoundedCornerShape(6.dp),
          color = Color(0x99000000)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.TouchApp,
              contentDescription = "Touch robot",
              tint = RobotCyan,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "TAP TO PET",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = RobotTextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Quick Sensor Indicators around robot (Section 13)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        SensorBadge(label = "ULTRASONIC", value = "${distanceCenter.roundToInt()} cm", active = distanceCenter < 50f)
        SensorBadge(label = "SERVO", value = "${servoAngle.roundToInt()}°", active = servoAngle != 90f)
        SensorBadge(label = "TOUCH", value = if (isTouchActive) "ACTIVE" else "IDLE", active = isTouchActive)
        SensorBadge(label = "BATTERY", value = "${batteryPercent.roundToInt()}%", active = batteryPercent <= 25f)
      }
    }
  }
}

@Composable
fun SensorBadge(label: String, value: String, active: Boolean) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      color = RobotTextMuted
    )
    Surface(
      shape = RoundedCornerShape(4.dp),
      color = if (active) RobotWarning.copy(alpha = 0.15f) else RobotElevatedPanel,
      border = CardDefaults.outlinedCardBorder().copy(
        brush = Brush.linearGradient(
          listOf(if (active) RobotWarning else RobotBorder, if (active) RobotWarning else RobotBorder)
        )
      )
    ) {
      Text(
        text = value,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (active) RobotWarning else RobotTextPrimary,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}
