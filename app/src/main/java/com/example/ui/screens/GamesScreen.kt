package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.viewmodel.MemoryGameState
import com.example.viewmodel.ReactionGameState
import com.example.viewmodel.TouchGameState
import com.example.ui.theme.*

@Composable
fun GamesScreen(
  touchGame: TouchGameState,
  memoryGame: MemoryGameState,
  reactionGame: ReactionGameState,
  onStartTouchGame: () -> Unit,
  onStartMemoryGame: () -> Unit,
  onSubmitMemoryDir: (String) -> Unit,
  onStartReactionTest: () -> Unit,
  onReactionTap: () -> Unit,
  onStartDanceParty: () -> Unit,
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
    Text(
      text = "INTERACTIVE ROBOT GAMES",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = RobotAccentBlue,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 1. Touch Reaction Game (Section 49)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("touch_game_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TouchApp, contentDescription = "Touch", tint = RobotCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text("TOUCH FRENZY (30s)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotTextPrimary)
          }
          Text("Best: ${touchGame.bestScore}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RobotWarning, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (touchGame.isActive) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Score: ${touchGame.score}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RobotSuccess)
            Text("Combo: x${touchGame.combo}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RobotCyan)
            Text("Time: ${touchGame.timeLeftSec}s", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RobotDanger)
          }
          Text(
            text = "Keep tapping the robot in Dashboard or Simulator to score!",
            fontSize = 11.sp,
            color = RobotTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
          )
        } else {
          Button(
            onClick = onStartTouchGame,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RobotAccentBlue),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Start 30s Touch Challenge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Reflex Reaction Speed Test (Section 49)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("reaction_game_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, contentDescription = "Reaction", tint = RobotWarning)
            Spacer(modifier = Modifier.width(8.dp))
            Text("REFLEX SPEED TEST", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotTextPrimary)
          }
          Text("Best: ${if (reactionGame.bestTimeMs < 9000) "${reactionGame.bestTimeMs}ms" else "--"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RobotCyan, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = reactionGame.statusMessage, fontSize = 12.sp, color = RobotTextSecondary)

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onStartReactionTest,
            modifier = Modifier.weight(1f).height(42.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RobotElevatedPanel),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Start Test", fontSize = 12.sp, color = RobotCyan, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onReactionTap,
            modifier = Modifier.weight(1f).height(42.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RobotSuccess),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("TAP NOW!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Memory Simon Sequence Game (Section 49)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("memory_game_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Extension, contentDescription = "Simon", tint = RobotSuccess)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SIMON SEQUENCE GAME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotTextPrimary)
          }
          Text("Best: Lvl ${memoryGame.bestLevel}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RobotCyan, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = memoryGame.statusMessage, fontSize = 12.sp, color = RobotTextSecondary)

        Spacer(modifier = Modifier.height(10.dp))

        if (!memoryGame.isActive) {
          Button(
            onClick = onStartMemoryGame,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RobotAccentBlue),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Start Memory Challenge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("LEFT", "UP", "DOWN", "RIGHT").forEach { dir ->
              Button(
                onClick = { onSubmitMemoryDir(dir) },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RobotElevatedPanel),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(dir, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RobotCyan)
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 4. Dance Party Mode (Section 49)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.MusicNote, contentDescription = "Dance", tint = Color(0xFFFF5D73))
          Spacer(modifier = Modifier.width(8.dp))
          Text("ROBOT DANCE PARTY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RobotTextPrimary)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Triggers rhythmic head bobbing, motor swivels, joyful voice greetings, and full rainbow 12-LED RGB ring party light show!",
          fontSize = 11.sp,
          color = RobotTextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onStartDanceParty,
          modifier = Modifier.fillMaxWidth().height(40.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5D73)),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("TRIGGER DANCE PARTY!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }
  }
}
