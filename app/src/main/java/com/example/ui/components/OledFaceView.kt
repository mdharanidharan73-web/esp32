package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.ExpressionType
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun OledFaceView(
  expression: ExpressionType,
  blinkProgress: Float = 0f,
  gazeX: Float = 0f,
  gazeY: Float = 0f,
  modifier: Modifier = Modifier,
  eyeColor: Color = RobotCyan
) {
  // Animated transitions for smooth eye movement
  val animatedGazeX by animateFloatAsState(targetValue = gazeX, animationSpec = tween(150), label = "gazeX")
  val animatedGazeY by animateFloatAsState(targetValue = gazeY, animationSpec = tween(150), label = "gazeY")

  // Subtle breathing/pulse animation
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "eyePulse"
  )

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF030508))
      .border(1.5.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
      .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val eyeWidth = w * 0.24f * pulseScale
      val baseEyeHeight = h * 0.52f * pulseScale

      // Effective eye openness influenced by blinking and sleep
      val eyeOpenness = when (expression) {
        ExpressionType.SLEEP -> 0.08f
        ExpressionType.YAWN -> 0.35f
        else -> (1f - blinkProgress).coerceIn(0.04f, 1f)
      }

      val eyeHeight = baseEyeHeight * eyeOpenness

      val leftEyeCenter = Offset(w * 0.30f + animatedGazeX * 14f, h * 0.50f + animatedGazeY * 10f)
      val rightEyeCenter = Offset(w * 0.70f + animatedGazeX * 14f, h * 0.50f + animatedGazeY * 10f)

      when (expression) {
        ExpressionType.HAPPY, ExpressionType.LAUGH, ExpressionType.CUTE -> {
          // Cheerful inverted arc eyes
          drawHappyEye(leftEyeCenter, eyeWidth, eyeHeight, eyeColor)
          drawHappyEye(rightEyeCenter, eyeWidth, eyeHeight, eyeColor)
          // Cute subtle smile curve below eyes
          drawSmile(Offset(w * 0.5f, h * 0.82f), w * 0.16f, eyeColor)
        }

        ExpressionType.WINK -> {
          drawHappyEye(leftEyeCenter, eyeWidth, eyeHeight, eyeColor)
          // Right eye closed wink line
          drawLine(
            color = eyeColor,
            start = Offset(rightEyeCenter.x - eyeWidth / 2, rightEyeCenter.y),
            end = Offset(rightEyeCenter.x + eyeWidth / 2, rightEyeCenter.y),
            strokeWidth = 6f
          )
        }

        ExpressionType.HEART_EYES -> {
          drawHeart(leftEyeCenter, eyeWidth * 0.9f, Color(0xFFFF5D73))
          drawHeart(rightEyeCenter, eyeWidth * 0.9f, Color(0xFFFF5D73))
        }

        ExpressionType.STAR_EYES -> {
          drawStar(leftEyeCenter, eyeWidth * 0.9f, Color(0xFFFFC857))
          drawStar(rightEyeCenter, eyeWidth * 0.9f, Color(0xFFFFC857))
        }

        ExpressionType.SPIRAL_EYES -> {
          drawSpiral(leftEyeCenter, eyeWidth * 0.8f, eyeColor)
          drawSpiral(rightEyeCenter, eyeWidth * 0.8f, eyeColor)
        }

        ExpressionType.ANGRY -> {
          drawAngryEye(leftEyeCenter, eyeWidth, eyeHeight, isLeft = true, color = Color(0xFFFF5D73))
          drawAngryEye(rightEyeCenter, eyeWidth, eyeHeight, isLeft = false, color = Color(0xFFFF5D73))
        }

        ExpressionType.SAD, ExpressionType.CRY -> {
          drawSadEye(leftEyeCenter, eyeWidth, eyeHeight, isLeft = true, color = RobotAccentBlue)
          drawSadEye(rightEyeCenter, eyeWidth, eyeHeight, isLeft = false, color = RobotAccentBlue)
          if (expression == ExpressionType.CRY) {
            // Tear drops
            drawCircle(Color(0xFF32D6FF), radius = 5f, center = Offset(leftEyeCenter.x, leftEyeCenter.y + eyeHeight + 10f))
            drawCircle(Color(0xFF32D6FF), radius = 5f, center = Offset(rightEyeCenter.x, rightEyeCenter.y + eyeHeight + 10f))
          }
        }

        ExpressionType.SURPRISED, ExpressionType.SCARED -> {
          // Large round eyes with inner small pupil
          drawCircle(eyeColor, radius = eyeWidth * 0.6f, center = leftEyeCenter, style = Stroke(width = 7f))
          drawCircle(eyeColor, radius = eyeWidth * 0.22f, center = leftEyeCenter)
          drawCircle(eyeColor, radius = eyeWidth * 0.6f, center = rightEyeCenter, style = Stroke(width = 7f))
          drawCircle(eyeColor, radius = eyeWidth * 0.22f, center = rightEyeCenter)
        }

        ExpressionType.SLEEP -> {
          // Soft sleeping lines
          drawLine(eyeColor.copy(alpha = 0.6f), Offset(leftEyeCenter.x - eyeWidth / 2, leftEyeCenter.y), Offset(leftEyeCenter.x + eyeWidth / 2, leftEyeCenter.y), strokeWidth = 5f)
          drawLine(eyeColor.copy(alpha = 0.6f), Offset(rightEyeCenter.x - eyeWidth / 2, rightEyeCenter.y), Offset(rightEyeCenter.x + eyeWidth / 2, rightEyeCenter.y), strokeWidth = 5f)
        }

        ExpressionType.SCANNING, ExpressionType.LOADING -> {
          // Cyber scan line sweeps across eyes
          drawRoundRect(
            color = eyeColor.copy(alpha = 0.4f),
            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2, leftEyeCenter.y - eyeHeight / 2),
            size = Size(eyeWidth, eyeHeight),
            cornerRadius = CornerRadius(8f, 8f)
          )
          drawRoundRect(
            color = eyeColor.copy(alpha = 0.4f),
            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2, rightEyeCenter.y - eyeHeight / 2),
            size = Size(eyeWidth, eyeHeight),
            cornerRadius = CornerRadius(8f, 8f)
          )
          // Glowing scan beam
          val sweepX = w * (0.2f + 0.6f * ((System.currentTimeMillis() % 1200) / 1200f))
          drawLine(Color.White, Offset(sweepX, h * 0.2f), Offset(sweepX, h * 0.8f), strokeWidth = 3f)
        }

        else -> {
          // Standard pill / rounded rectangle robotic eyes with specular highlight
          drawStandardEye(leftEyeCenter, eyeWidth, eyeHeight, eyeColor)
          drawStandardEye(rightEyeCenter, eyeWidth, eyeHeight, eyeColor)
        }
      }
    }
  }
}

private fun DrawScope.drawStandardEye(center: Offset, width: Float, height: Float, color: Color) {
  drawRoundRect(
    color = color,
    topLeft = Offset(center.x - width / 2, center.y - height / 2),
    size = Size(width, height),
    cornerRadius = CornerRadius(10f, 10f)
  )
  // Eye shine highlight
  if (height > 12f) {
    drawCircle(
      color = Color.White.copy(alpha = 0.85f),
      radius = min(width * 0.18f, height * 0.2f),
      center = Offset(center.x - width * 0.18f, center.y - height * 0.2f)
    )
  }
}

private fun DrawScope.drawHappyEye(center: Offset, width: Float, height: Float, color: Color) {
  val path = Path().apply {
    moveTo(center.x - width / 2, center.y + height * 0.2f)
    quadraticTo(
      center.x, center.y - height * 0.7f,
      center.x + width / 2, center.y + height * 0.2f
    )
  }
  drawPath(path, color = color, style = Stroke(width = 8f))
}

private fun DrawScope.drawAngryEye(center: Offset, width: Float, height: Float, isLeft: Boolean, color: Color) {
  val path = Path().apply {
    if (isLeft) {
      moveTo(center.x - width / 2, center.y - height * 0.2f)
      lineTo(center.x + width / 2, center.y - height * 0.6f)
      lineTo(center.x + width / 2, center.y + height * 0.4f)
      lineTo(center.x - width / 2, center.y + height * 0.3f)
    } else {
      moveTo(center.x - width / 2, center.y - height * 0.6f)
      lineTo(center.x + width / 2, center.y - height * 0.2f)
      lineTo(center.x + width / 2, center.y + height * 0.3f)
      lineTo(center.x - width / 2, center.y + height * 0.4f)
    }
    close()
  }
  drawPath(path, color = color)
}

private fun DrawScope.drawSadEye(center: Offset, width: Float, height: Float, isLeft: Boolean, color: Color) {
  val path = Path().apply {
    if (isLeft) {
      moveTo(center.x - width / 2, center.y - height * 0.5f)
      lineTo(center.x + width / 2, center.y - height * 0.2f)
      lineTo(center.x + width / 2, center.y + height * 0.3f)
      lineTo(center.x - width / 2, center.y + height * 0.4f)
    } else {
      moveTo(center.x - width / 2, center.y - height * 0.2f)
      lineTo(center.x + width / 2, center.y - height * 0.5f)
      lineTo(center.x + width / 2, center.y + height * 0.4f)
      lineTo(center.x - width / 2, center.y + height * 0.3f)
    }
    close()
  }
  drawPath(path, color = color)
}

private fun DrawScope.drawSmile(center: Offset, width: Float, color: Color) {
  val path = Path().apply {
    moveTo(center.x - width / 2, center.y)
    quadraticTo(center.x, center.y + 12f, center.x + width / 2, center.y)
  }
  drawPath(path, color = color, style = Stroke(width = 4f))
}

private fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
  val path = Path().apply {
    val s = size / 2
    moveTo(center.x, center.y + s * 0.6f)
    cubicTo(center.x - s * 1.2f, center.y - s * 0.2f, center.x - s * 1.0f, center.y - s * 1.1f, center.x, center.y - s * 0.4f)
    cubicTo(center.x + s * 1.0f, center.y - s * 1.1f, center.x + s * 1.2f, center.y - s * 0.2f, center.x, center.y + s * 0.6f)
    close()
  }
  drawPath(path, color = color)
}

private fun DrawScope.drawStar(center: Offset, size: Float, color: Color) {
  val path = Path()
  val outerR = size * 0.5f
  val innerR = outerR * 0.45f
  for (i in 0 until 10) {
    val r = if (i % 2 == 0) outerR else innerR
    val angle = (i * 36 - 90) * (PI / 180f).toFloat()
    val x = center.x + r * cos(angle)
    val y = center.y + r * sin(angle)
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  path.close()
  drawPath(path, color = color)
}

private fun DrawScope.drawSpiral(center: Offset, size: Float, color: Color) {
  drawCircle(color = color, radius = size * 0.45f, center = center, style = Stroke(width = 4f))
  drawCircle(color = color, radius = size * 0.25f, center = center, style = Stroke(width = 4f))
}
