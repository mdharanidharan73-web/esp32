package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.RobotMode
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.RobotNavDestination
import com.example.viewmodel.RobotViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: RobotViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

      MyApplicationTheme(themeMode = themeMode) {
        RobotMainContent(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun RobotMainContent(viewModel: RobotViewModel) {
  val isBooted by viewModel.isBooted.collectAsStateWithLifecycle()
  val bootProgress by viewModel.bootProgress.collectAsStateWithLifecycle()
  val bootLogLines by viewModel.bootLogLines.collectAsStateWithLifecycle()
  val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
  val isCmdPaletteOpen by viewModel.isCommandPaletteOpen.collectAsStateWithLifecycle()
  val isExplainOpen by viewModel.isExplainabilityOpen.collectAsStateWithLifecycle()
  val isResetDialogOpen by viewModel.isFactoryResetDialogOpen.collectAsStateWithLifecycle()
  val selectedFirmwareIndex by viewModel.selectedFirmwareIndex.collectAsStateWithLifecycle()

  val touchGame by viewModel.touchGame.collectAsStateWithLifecycle()
  val memoryGame by viewModel.memoryGame.collectAsStateWithLifecycle()
  val reactionGame by viewModel.reactionGame.collectAsStateWithLifecycle()
  val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val engine = viewModel.engine

  // Command palette command list (Section 102)
  val commands = remember {
    listOf(
      CommandItem("cmd_fw", "Move Forward", "Motion", Icons.Default.ArrowUpward) {
        viewModel.engine.manualMove("FORWARD")
      },
      CommandItem("cmd_auto", "Start Autonomous Mode", "AI", Icons.Default.PrecisionManufacturing) {
        viewModel.engine.mode = RobotMode.AUTONOMOUS
        viewModel.showToast("Autonomous Mode Started")
      },
      CommandItem("cmd_happy", "Make Robot Happy", "Emotion", Icons.Default.Mood) {
        viewModel.engine.emotions = viewModel.engine.emotions.copy(happiness = 95f, boredom = 5f)
        viewModel.voice.speak("Yay! I am so happy!")
        viewModel.showToast("Robot Happiness Set to 95%")
      },
      CommandItem("cmd_sleep", "Put Robot to Sleep", "Action", Icons.Default.Bedtime) {
        viewModel.engine.triggerSleep()
        viewModel.voice.speak("Good night!")
        viewModel.showToast("Sleep Mode Activated")
      },
      CommandItem("cmd_wake", "Wake Robot", "Action", Icons.Default.WbSunny) {
        viewModel.engine.triggerWake()
        viewModel.voice.speak("Hello! I'm awake!")
        viewModel.showToast("Robot Awakened")
      },
      CommandItem("cmd_scan", "Scan Environment", "Navigation", Icons.Default.Radar) {
        viewModel.engine.targetServoAngle = 140f
        viewModel.showToast("Scanning Environment...")
      },
      CommandItem("cmd_sensors", "Open Sensors Screen", "Navigation", Icons.Default.Sensors) {
        viewModel.navigateTo(RobotNavDestination.SENSORS)
      },
      CommandItem("cmd_memory", "Open Memory Screen", "Navigation", Icons.Default.Memory) {
        viewModel.navigateTo(RobotNavDestination.MEMORY)
      },
      CommandItem("cmd_brain", "Open AI Brain Screen", "Navigation", Icons.Default.Psychology) {
        viewModel.navigateTo(RobotNavDestination.AI_BRAIN)
      },
      CommandItem("cmd_code", "Open ESP32 Code Viewer", "Development", Icons.Default.Code) {
        viewModel.navigateTo(RobotNavDestination.ESP32_CODE)
      },
      CommandItem("cmd_demo", "Run Master Demo Sequence", "System", Icons.Default.PlayCircle) {
        viewModel.runDemoScenario()
      },
      CommandItem("cmd_reset", "Reset Simulation Arena", "Simulator", Icons.Default.RestartAlt) {
        viewModel.resetSimulation()
      }
    )
  }

  if (!isBooted) {
    BootSplashScreen(
      isBooting = bootLogLines.isNotEmpty(),
      bootProgress = bootProgress,
      bootLogLines = bootLogLines,
      onStartSimulation = { viewModel.startBootSequence() }
    )
  } else {
    ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        ModalDrawerSheet(
          drawerContainerColor = RobotBgSecondary,
          drawerContentColor = RobotTextPrimary
        ) {
          AppNavigationDrawerContent(
            currentDestination = activeScreen,
            onSelectDestination = { dest ->
              viewModel.navigateTo(dest)
              scope.launch { drawerState.close() }
            }
          )
        }
      }
    ) {
      Scaffold(
        modifier = Modifier.fillMaxSize().testTag("robot_main_scaffold"),
        topBar = {
          TopNavBar(
            status = engine.systemStatus,
            batteryPercent = engine.batteryPercent,
            wifiConnected = true,
            bluetoothConnected = true,
            onOpenDrawer = { scope.launch { drawerState.open() } },
            onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
            onOpenSettings = { viewModel.navigateTo(RobotNavDestination.SETTINGS) }
          )
        },
        bottomBar = {
          GlobalStatusBar(
            mode = engine.mode,
            behavior = engine.behavior,
            wifiConnected = true,
            bluetoothConnected = true
          )
        },
        containerColor = RobotBgDark
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          // Dynamic destination screen router
          when (activeScreen) {
            RobotNavDestination.DASHBOARD -> {
              DashboardScreen(
                engine = engine,
                onRobotTouch = { viewModel.onRobotTouched() },
                onExplainClick = { viewModel.toggleExplainability(true) }
              )
            }

            RobotNavDestination.SIMULATOR, RobotNavDestination.ENVIRONMENT -> {
              SimulatorScreen(
                engine = engine,
                onManualMove = { dir -> engine.manualMove(dir) },
                onReset = { viewModel.resetSimulation() },
                onStep = { viewModel.stepSimulation() },
                onSpeedChange = { spd -> viewModel.setSimulationSpeed(spd) },
                onTogglePlay = { play -> viewModel.setSimulationRunning(play) }
              )
            }

            RobotNavDestination.SENSORS -> {
              SensorsScreen(engine = engine)
            }

            RobotNavDestination.EMOTIONS -> {
              EmotionsScreen(engine = engine)
            }

            RobotNavDestination.AI_BRAIN -> {
              AiBrainScreen(
                engine = engine,
                onOpenExplainability = { viewModel.toggleExplainability(true) }
              )
            }

            RobotNavDestination.MEMORY -> {
              MemoryScreen(engine = engine)
            }

            RobotNavDestination.GAMES -> {
              GamesScreen(
                touchGame = touchGame,
                memoryGame = memoryGame,
                reactionGame = reactionGame,
                onStartTouchGame = { viewModel.startTouchGame() },
                onStartMemoryGame = { viewModel.startMemoryGame() },
                onSubmitMemoryDir = { dir -> viewModel.submitMemoryInput(dir) },
                onStartReactionTest = { viewModel.startReactionTest() },
                onReactionTap = { viewModel.onReactionTap() },
                onStartDanceParty = {
                  engine.mode = RobotMode.DANCE
                  engine.log(com.example.model.LogCategory.AI, "Dance party mode active")
                  viewModel.voice.speak("Let's dance!")
                  viewModel.showToast("Dance Party Mode Active!")
                }
              )
            }

            RobotNavDestination.HARDWARE, RobotNavDestination.SERVO, RobotNavDestination.MOTORS,
            RobotNavDestination.RGB_LEDS, RobotNavDestination.PINOUT, RobotNavDestination.OLED -> {
              HardwareScreen(engine = engine)
            }

            RobotNavDestination.VOICE -> {
              VoiceScreen(
                engine = engine,
                onSpeak = { phrase -> viewModel.speakPhrase(phrase) }
              )
            }

            RobotNavDestination.BLUETOOTH, RobotNavDestination.WIFI -> {
              ConnectivityScreen(engine = engine)
            }

            RobotNavDestination.BATTERY -> {
              BatteryScreen(engine = engine)
            }

            RobotNavDestination.ESP32_CODE, RobotNavDestination.WOKWI, RobotNavDestination.EXPORT -> {
              CodeIdeScreen(
                selectedIndex = selectedFirmwareIndex,
                onSelectFile = { idx -> viewModel.selectFirmwareFile(idx) },
                onCopySuccess = { viewModel.showToast("Code copied to clipboard!") }
              )
            }

            RobotNavDestination.LOGS, RobotNavDestination.DEBUG_CONSOLE -> {
              DebugLogsScreen(engine = engine)
            }

            RobotNavDestination.SETTINGS, RobotNavDestination.ABOUT -> {
              SettingsScreen(
                engine = engine,
                currentTheme = currentTheme,
                onThemeChange = { mode -> viewModel.setThemeMode(mode) },
                onRunDemoScenario = { viewModel.runDemoScenario() },
                onOpenFactoryReset = { viewModel.toggleFactoryResetDialog(true) }
              )
            }
          }

          // In-App Toast Notification
          AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 16.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFF1E293B),
              border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(RobotCyan)),
              shadowElevation = 8.dp
            ) {
              Text(
                text = toastMessage ?: "",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
              )
            }
          }
        }
      }
    }
  }

  // Command Palette Dialog (Section 102)
  CommandPaletteDialog(
    isOpen = isCmdPaletteOpen,
    onDismiss = { viewModel.toggleCommandPalette(false) },
    commands = commands
  )

  // AI Explainability Dialog (Section 65)
  ExplainabilityDialog(
    isOpen = isExplainOpen,
    onDismiss = { viewModel.toggleExplainability(false) },
    engine = engine
  )

  // Factory Reset Confirmation Dialog
  if (isResetDialogOpen) {
    AlertDialog(
      onDismissRequest = { viewModel.toggleFactoryResetDialog(false) },
      title = { Text("Confirm Factory Reset", color = RobotDanger, fontWeight = FontWeight.Bold) },
      text = {
        Text("This will restore default battery health, erase flash memories, and reinitialize emotion values. Are you sure?")
      },
      confirmButton = {
        Button(
          onClick = { viewModel.factoryReset() },
          colors = ButtonDefaults.buttonColors(containerColor = RobotDanger)
        ) {
          Text("Yes, Reset")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { viewModel.toggleFactoryResetDialog(false) }) {
          Text("Cancel")
        }
      },
      containerColor = RobotPanel,
      shape = RoundedCornerShape(16.dp)
    )
  }
}
