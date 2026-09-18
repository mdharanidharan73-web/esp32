package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RobotSimulationEngine
import com.example.ui.theme.*
import kotlin.math.roundToInt

data class SoundTrack(val trackNum: String, val filename: String, val phrase: String)

@Composable
fun VoiceScreen(
  engine: RobotSimulationEngine,
  onSpeak: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var customPhrase by remember { mutableStateOf("") }

  val soundTracks = listOf(
    SoundTrack("001", "0001_hello.mp3", "Hello! I am ready."),
    SoundTrack("002", "0002_happy.mp3", "Yay! I love spending time with you!"),
    SoundTrack("003", "0003_sad.mp3", "Aww... I am feeling a little down."),
    SoundTrack("004", "0004_curious.mp3", "Hmm, what is this over here?"),
    SoundTrack("005", "0005_sleep.mp3", "Good night! Entering sleep mode."),
    SoundTrack("006", "0006_wake.mp3", "Good morning! Systems active."),
    SoundTrack("007", "0007_obstacle.mp3", "Obstacle detected ahead! Rerouting."),
    SoundTrack("008", "0008_laugh.mp3", "Hehehe, that tickles!"),
    SoundTrack("009", "0009_low_battery.mp3", "Warning! Battery low. Please charge me."),
    SoundTrack("010", "0010_cute.mp3", "Beep boop! You are my best friend.")
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(RobotBgDark)
      .padding(16.dp)
  ) {
    // 1. Hardware Module Card
    Card(
      modifier = Modifier.fillMaxWidth().testTag("dfplayer_card"),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RecordVoiceOver, contentDescription = "DFPlayer", tint = RobotCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "DFPLAYER MINI (UART2 GPIO 19/23)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = RobotAccentBlue,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "VOLUME ${engine.voiceVolume}/30",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RobotCyan,
            fontFamily = FontFamily.Monospace
          )
        }

        Slider(
          value = engine.voiceVolume.toFloat(),
          onValueChange = { engine.voiceVolume = it.roundToInt() },
          valueRange = 0f..30f,
          colors = SliderDefaults.colors(thumbColor = RobotCyan, activeTrackColor = RobotCyan)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Custom Speech Synthesizer
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = RobotPanel),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = customPhrase,
          onValueChange = { customPhrase = it },
          placeholder = { Text("Enter phrase to speak...", fontSize = 12.sp, color = RobotTextMuted) },
          singleLine = true,
          modifier = Modifier.weight(1f),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RobotAccentBlue,
            unfocusedBorderColor = RobotBorder,
            focusedTextColor = RobotTextPrimary,
            unfocusedTextColor = RobotTextPrimary
          )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (customPhrase.isNotBlank()) {
              onSpeak(customPhrase)
              customPhrase = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = RobotAccentBlue),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(50.dp)
        ) {
          Icon(Icons.Default.VolumeUp, contentDescription = "Speak")
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "SD CARD SOUND TRACKS",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = RobotAccentBlue,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    // 3. Sound Tracks List
    LazyColumn(
      modifier = Modifier.fillMaxWidth().weight(1f),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(soundTracks) { track ->
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = RobotElevatedPanel,
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotBorder))
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${track.trackNum} • ${track.filename}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RobotCyan,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "\"${track.phrase}\"",
                fontSize = 12.sp,
                color = RobotTextPrimary
              )
            }

            IconButton(
              onClick = { onSpeak(track.phrase) },
              colors = IconButtonDefaults.iconButtonColors(containerColor = RobotAccentBlue.copy(alpha = 0.2f))
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = RobotCyan)
            }
          }
        }
      }
    }
  }
}
