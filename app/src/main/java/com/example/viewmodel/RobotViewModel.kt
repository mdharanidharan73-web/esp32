package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.RobotSimulationEngine
import com.example.engine.VoiceSpeaker
import com.example.model.*
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RobotNavDestination(val title: String, val category: String) {
  DASHBOARD("Dashboard", "MAIN"),
  SIMULATOR("Simulator", "MAIN"),
  ENVIRONMENT("Environment", "MAIN"),
  SENSORS("Sensors", "MAIN"),
  EMOTIONS("Emotions", "MAIN"),
  AI_BRAIN("AI Brain", "MAIN"),
  MEMORY("Memory", "MAIN"),
  GAMES("Games", "MAIN"),

  HARDWARE("Hardware", "HARDWARE"),
  SERVO("Servo SG90", "HARDWARE"),
  MOTORS("Motors L298N", "HARDWARE"),
  OLED("OLED Face", "HARDWARE"),
  RGB_LEDS("RGB LED Ring", "HARDWARE"),
  VOICE("Voice DFPlayer", "HARDWARE"),
  BLUETOOTH("Bluetooth", "HARDWARE"),
  WIFI("WiFi & OTA", "HARDWARE"),
  BATTERY("Battery & Power", "HARDWARE"),

  ESP32_CODE("ESP32 Code", "DEVELOPMENT"),
  WOKWI("Wokwi Simulator", "DEVELOPMENT"),
  PINOUT("Pin Matrix", "DEVELOPMENT"),
  DEBUG_CONSOLE("Debug Console", "DEVELOPMENT"),
  LOGS("Event Logs", "DEVELOPMENT"),
  EXPORT("Export Center", "DEVELOPMENT"),

  SETTINGS("Settings", "SYSTEM"),
  ABOUT("About", "SYSTEM")
}

data class TouchGameState(
  val isActive: Boolean = false,
  val score: Int = 0,
  val combo: Int = 0,
  val bestScore: Int = 0,
  val timeLeftSec: Int = 30
)

data class MemoryGameState(
  val isActive: Boolean = false,
  val sequence: List<String> = emptyList(),
  val playerIndex: Int = 0,
  val level: Int = 1,
  val bestLevel: Int = 1,
  val statusMessage: String = "Press START to begin"
)

data class ReactionGameState(
  val state: ReactionState = ReactionState.IDLE,
  val reactionTimeMs: Long = 0,
  val bestTimeMs: Long = 9999,
  val statusMessage: String = "Tap 'Start Test' and watch for the robot's eye flare!"
)

enum class ReactionState {
  IDLE,
  WAITING_FOR_TRIGGER,
  TRIGGERED,
  RESULT,
  TOO_EARLY
}

class RobotViewModel(application: Application) : AndroidViewModel(application) {
  val engine = RobotSimulationEngine()
  val voice = VoiceSpeaker(application)

  private val prefs = application.getSharedPreferences("robot_v5_prefs", Context.MODE_PRIVATE)

  private val _activeScreen = MutableStateFlow(RobotNavDestination.DASHBOARD)
  val activeScreen: StateFlow<RobotNavDestination> = _activeScreen.asStateFlow()

  private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
  val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

  private val _isBooted = MutableStateFlow(false)
  val isBooted: StateFlow<Boolean> = _isBooted.asStateFlow()

  private val _bootProgress = MutableStateFlow(0f)
  val bootProgress: StateFlow<Float> = _bootProgress.asStateFlow()

  private val _bootLogLines = MutableStateFlow<List<String>>(emptyList())
  val bootLogLines: StateFlow<List<String>> = _bootLogLines.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  private val _isCommandPaletteOpen = MutableStateFlow(false)
  val isCommandPaletteOpen: StateFlow<Boolean> = _isCommandPaletteOpen.asStateFlow()

  private val _isExplainabilityOpen = MutableStateFlow(false)
  val isExplainabilityOpen: StateFlow<Boolean> = _isExplainabilityOpen.asStateFlow()

  private val _isFactoryResetDialogOpen = MutableStateFlow(false)
  val isFactoryResetDialogOpen: StateFlow<Boolean> = _isFactoryResetDialogOpen.asStateFlow()

  private val _selectedFirmwareIndex = MutableStateFlow(0)
  val selectedFirmwareIndex: StateFlow<Int> = _selectedFirmwareIndex.asStateFlow()

  // Game states
  private val _touchGame = MutableStateFlow(TouchGameState())
  val touchGame: StateFlow<TouchGameState> = _touchGame.asStateFlow()

  private val _memoryGame = MutableStateFlow(MemoryGameState())
  val memoryGame: StateFlow<MemoryGameState> = _memoryGame.asStateFlow()

  private val _reactionGame = MutableStateFlow(ReactionGameState())
  val reactionGame: StateFlow<ReactionGameState> = _reactionGame.asStateFlow()

  private var reactionTriggerTimeMs = 0L

  private var simulationLoopJob: Job? = null
  private var demoScenarioJob: Job? = null

  init {
    loadSettings()
    startSimulationLoop()
  }

  private fun loadSettings() {
    val bestTouch = prefs.getInt("best_touch", 0)
    _touchGame.value = _touchGame.value.copy(bestScore = bestTouch)
    val bestReaction = prefs.getLong("best_reaction", 9999L)
    _reactionGame.value = _reactionGame.value.copy(bestTimeMs = bestReaction)
    val bestMemory = prefs.getInt("best_memory", 1)
    _memoryGame.value = _memoryGame.value.copy(bestLevel = bestMemory)
  }

  fun startSimulationLoop() {
    simulationLoopJob?.cancel()
    simulationLoopJob = viewModelScope.launch {
      var lastTime = System.nanoTime()
      while (isActive) {
        val now = System.nanoTime()
        val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
        lastTime = now

        engine.update(dt)

        delay(16) // ~60 FPS
      }
    }
  }

  fun navigateTo(destination: RobotNavDestination) {
    _activeScreen.value = destination
  }

  fun setThemeMode(mode: AppThemeMode) {
    _themeMode.value = mode
  }

  fun toggleCommandPalette(open: Boolean) {
    _isCommandPaletteOpen.value = open
  }

  fun toggleExplainability(open: Boolean) {
    _isExplainabilityOpen.value = open
  }

  fun toggleFactoryResetDialog(open: Boolean) {
    _isFactoryResetDialogOpen.value = open
  }

  fun selectFirmwareFile(index: Int) {
    _selectedFirmwareIndex.value = index
  }

  fun showToast(msg: String) {
    _toastMessage.value = msg
    viewModelScope.launch {
      delay(2200)
      if (_toastMessage.value == msg) {
        _toastMessage.value = null
      }
    }
  }

  // --- Boot Sequence Animation ---
  fun startBootSequence() {
    viewModelScope.launch {
      _bootLogLines.value = emptyList()
      val checklist = listOf(
        "POWER SYSTEM ........ READY",
        "ESP32 CORE .......... READY",
        "OLED DISPLAY ........ READY",
        "SENSORS ............. READY",
        "MOTOR DRIVER ........ READY",
        "SERVO ............... READY",
        "RGB SYSTEM .......... READY",
        "WIFI ................ CONNECTING",
        "BLUETOOTH ........... READY",
        "MEMORY .............. LOADED",
        "AI PERSONALITY ...... LOADED",
        "NAVIGATION .......... READY",
        "ROBOT CORE .......... READY"
      )

      for (i in checklist.indices) {
        delay(120)
        _bootLogLines.value = _bootLogLines.value + checklist[i]
        _bootProgress.value = (i + 1) / checklist.size.toFloat()
      }

      delay(300)
      _bootLogLines.value = _bootLogLines.value + ">> ROBOT ONLINE <<"
      _isBooted.value = true
      engine.log(LogCategory.SYSTEM, "Full boot sequence completed successfully")
      voice.speak("Hello. I'm ready.")
      showToast("Robot System Online")
    }
  }

  // --- Touch Interaction ---
  fun onRobotTouched() {
    val speech = engine.touchRobot()
    voice.speak(speech)
    showToast("Robot touched! Happiness increased")

    if (_touchGame.value.isActive) {
      val newScore = _touchGame.value.score + 10 + (_touchGame.value.combo * 2)
      val newCombo = _touchGame.value.combo + 1
      val best = maxOf(newScore, _touchGame.value.bestScore)
      _touchGame.value = _touchGame.value.copy(score = newScore, combo = newCombo, bestScore = best)
      prefs.edit().putInt("best_touch", best).apply()
    }
  }

  // --- Voice Actions ---
  fun speakPhrase(phrase: String) {
    engine.currentVoicePhrase = phrase
    engine.log(LogCategory.VOICE, "Spoken: \"$phrase\"")
    voice.speak(phrase)
    showToast("Spoken: \"$phrase\"")
  }

  // --- Robot Controls ---
  fun setSimulationRunning(running: Boolean) {
    engine.isRunning = running
    showToast(if (running) "Simulation Resumed" else "Simulation Paused")
  }

  fun setSimulationSpeed(speed: Float) {
    engine.simulationSpeed = speed
    showToast("Speed set to ${speed}x")
  }

  fun resetSimulation() {
    engine.position = Position(240f, 260f)
    engine.rotationDeg = 45f
    engine.leftMotorSpeed = 0f
    engine.rightMotorSpeed = 0f
    engine.targetServoAngle = 90f
    engine.servoAngle = 90f
    engine.mode = RobotMode.AI
    engine.robotPath.clear()
    engine.log(LogCategory.SYSTEM, "Simulation world reset")
    showToast("Simulation Reset")
  }

  fun stepSimulation() {
    engine.update(0.1f)
    showToast("Simulation Stepped +0.1s")
  }

  fun factoryReset() {
    engine.batteryPercent = 100f
    engine.emotions = EmotionState()
    engine.personality = PersonalityTraits()
    engine.touchCount = 0
    engine.logs.clear()
    engine.memoryEvents.clear()
    engine.log(LogCategory.SYSTEM, "Factory reset performed. Memory cleared.")
    resetSimulation()
    _isFactoryResetDialogOpen.value = false
    showToast("Robot factory reset complete")
  }

  // --- Demo Scenario Runner (Section 99) ---
  fun runDemoScenario() {
    demoScenarioJob?.cancel()
    demoScenarioJob = viewModelScope.launch {
      showToast("Starting Master Demo Sequence...")
      engine.log(LogCategory.AI, "Demo scenario started")

      // 1. Wake
      engine.triggerWake()
      voice.speak("Hello. I'm ready.")
      delay(2000)

      // 2. Idle / Blink / Look around
      engine.currentExpression = ExpressionType.BLINK
      delay(1200)
      engine.currentExpression = ExpressionType.LOOK_LEFT
      engine.targetServoAngle = 135f
      delay(1500)
      engine.targetServoAngle = 45f
      engine.currentExpression = ExpressionType.LOOK_RIGHT
      delay(1500)
      engine.targetServoAngle = 90f

      // 3. Touch
      onRobotTouched()
      delay(2000)

      // 4. Playful & Move forward
      engine.currentExpression = ExpressionType.CUTE
      engine.mode = RobotMode.AUTONOMOUS
      delay(2500)

      // 5. Obstacle encounter & scan
      engine.distanceCenter = 22f
      engine.currentExpression = ExpressionType.SURPRISED
      voice.speak("Obstacle detected!")
      delay(2000)

      // 6. Servo scan left & right
      engine.targetServoAngle = 140f
      delay(1200)
      engine.targetServoAngle = 40f
      delay(1200)
      engine.targetServoAngle = 90f

      // 7. Turn & continue
      engine.manualMove("RIGHT")
      delay(1000)
      engine.manualMove("FORWARD")
      delay(1500)

      // 8. Low light -> Sleepy -> Sleep
      engine.lightLevel = 10f
      engine.currentExpression = ExpressionType.YAWN
      delay(1800)
      engine.triggerSleep()
      voice.speak("Good night!")
      delay(2500)

      // 9. Touch -> Wake
      onRobotTouched()
      engine.triggerWake()
      showToast("Demo Sequence Complete!")
    }
  }

  // --- Games Implementation ---
  fun startTouchGame() {
    _touchGame.value = TouchGameState(isActive = true, score = 0, combo = 0, bestScore = _touchGame.value.bestScore, timeLeftSec = 30)
    viewModelScope.launch {
      while (_touchGame.value.isActive && _touchGame.value.timeLeftSec > 0) {
        delay(1000)
        val remaining = _touchGame.value.timeLeftSec - 1
        _touchGame.value = _touchGame.value.copy(timeLeftSec = remaining)
      }
      _touchGame.value = _touchGame.value.copy(isActive = false)
      showToast("Time's Up! Final Score: ${_touchGame.value.score}")
    }
  }

  fun startMemoryGame() {
    val directions = listOf("LEFT", "RIGHT", "UP", "DOWN")
    val seq = List(3) { directions.random() }
    _memoryGame.value = MemoryGameState(
      isActive = true,
      sequence = seq,
      playerIndex = 0,
      level = 1,
      bestLevel = _memoryGame.value.bestLevel,
      statusMessage = "Memorize: ${seq.joinToString(" → ")}"
    )
    voice.speak("Memorize the sequence!")
  }

  fun submitMemoryInput(dir: String) {
    val game = _memoryGame.value
    if (!game.isActive) return

    if (game.sequence[game.playerIndex] == dir) {
      val nextIndex = game.playerIndex + 1
      if (nextIndex >= game.sequence.size) {
        // Level cleared!
        val newLevel = game.level + 1
        val best = maxOf(newLevel, game.bestLevel)
        val directions = listOf("LEFT", "RIGHT", "UP", "DOWN")
        val nextSeq = List(newLevel + 2) { directions.random() }
        _memoryGame.value = game.copy(
          level = newLevel,
          bestLevel = best,
          sequence = nextSeq,
          playerIndex = 0,
          statusMessage = "Level $newLevel! Memorize: ${nextSeq.joinToString(" → ")}"
        )
        prefs.edit().putInt("best_memory", best).apply()
        showToast("Level $newLevel Passed!")
        voice.speak("Great job! Level $newLevel")
      } else {
        _memoryGame.value = game.copy(playerIndex = nextIndex, statusMessage = "Correct! ($nextIndex/${game.sequence.size})")
      }
    } else {
      _memoryGame.value = game.copy(isActive = false, statusMessage = "Wrong direction! Game Over. Reached Level ${game.level}")
      showToast("Incorrect! Game Over.")
      voice.speak("Oops! Try again.")
    }
  }

  fun startReactionTest() {
    _reactionGame.value = _reactionGame.value.copy(
      state = ReactionState.WAITING_FOR_TRIGGER,
      statusMessage = "Wait for eyes to flash cyan..."
    )
    viewModelScope.launch {
      val delayMs = 1500L + (Math.random() * 3000).toLong()
      delay(delayMs)
      if (_reactionGame.value.state == ReactionState.WAITING_FOR_TRIGGER) {
        reactionTriggerTimeMs = System.currentTimeMillis()
        engine.currentExpression = ExpressionType.STAR_EYES
        _reactionGame.value = _reactionGame.value.copy(
          state = ReactionState.TRIGGERED,
          statusMessage = "TAP NOW!"
        )
      }
    }
  }

  fun onReactionTap() {
    val now = System.currentTimeMillis()
    when (_reactionGame.value.state) {
      ReactionState.WAITING_FOR_TRIGGER -> {
        _reactionGame.value = _reactionGame.value.copy(
          state = ReactionState.TOO_EARLY,
          statusMessage = "Too early! Wait for the flash."
        )
      }
      ReactionState.TRIGGERED -> {
        val reactionMs = now - reactionTriggerTimeMs
        val best = minOf(reactionMs, _reactionGame.value.bestTimeMs)
        _reactionGame.value = _reactionGame.value.copy(
          state = ReactionState.RESULT,
          reactionTimeMs = reactionMs,
          bestTimeMs = best,
          statusMessage = "Reaction: ${reactionMs}ms! (Best: ${best}ms)"
        )
        prefs.edit().putLong("best_reaction", best).apply()
        voice.speak("$reactionMs milliseconds! Fast reflexes!")
      }
      else -> {}
    }
  }

  override fun onCleared() {
    super.onCleared()
    simulationLoopJob?.cancel()
    demoScenarioJob?.cancel()
    voice.shutdown()
  }
}
