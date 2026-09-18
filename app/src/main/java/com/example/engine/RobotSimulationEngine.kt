package com.example.engine

import com.example.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class RobotSimulationEngine {
  // Arena boundaries
  val arenaWidth = 800f
  val arenaHeight = 600f
  val robotRadius = 26f

  // Simulation Clock & Speed
  var simulationSpeed = 1.0f
  var isRunning = true
  var isAutoNavigation = true

  // Current State
  var position = Position(240f, 260f)
  var rotationDeg = 45f // 0 = right, 90 = down, 180 = left, 270 = up
  var targetPosition: Position? = Position(600f, 400f)

  var servoAngle = 90f
  var targetServoAngle = 90f

  var leftMotorSpeed = 0f
  var rightMotorSpeed = 0f

  var batteryPercent = 88f
  var batteryVoltage = 7.64f
  var isCharging = false

  var temperature = 27.4f
  var humidity = 62f
  var lightLevel = 75f // 0-100 (bright)
  var noiseLevel = 35f // 0-100 (normal)

  var distanceCenter = 120f
  var distanceLeft = 140f
  var distanceRight = 110f

  var isTouchActive = false
  var touchCount = 0
  var lastTouchTimeMs = 0L

  var emotions = EmotionState()
  var emotionHistory = mutableListOf<EmotionSample>()

  var currentMood = RobotMood.HAPPY
  var moodIntensity = 78f
  var dominantFactors = "Curiosity + Trust + Energy"

  var personality = PersonalityTraits()
  var mode = RobotMode.AI
  var behavior = "Exploring"
  var behaviorConfidence = 0.82f
  var behaviorReason = "High curiosity + safe environment"
  var priorityLayer = 5

  var currentExpression = ExpressionType.IDLE
  var blinkProgress = 0f
  var gazeX = 0f
  var gazeY = 0f

  var rgbEffect = RgbEffectMode.AUTO_EMOTION
  var rgbBrightness = 0.85f
  var rgbColor = 0xFF32D6FF
  var rgbRingColors = MutableList(12) { 0xFF32D6FF }

  var wifiConnected = true
  var wifiSsid = "Robot_WiFi"
  var wifiIp = "192.168.1.100"
  var wifiSignal = 87
  var otaReady = true

  var bluetoothConnected = true
  var bluetoothDevice = "Android Phone"

  var currentVoicePhrase = "Hello. I'm ready."
  var voiceVolume: Int = 22

  val systemStatus: RobotSystemStatus
    get() = when {
      batteryPercent <= 15f -> RobotSystemStatus.LOW_BATTERY
      mode == RobotMode.SLEEP -> RobotSystemStatus.SLEEPING
      leftMotorSpeed != 0f || rightMotorSpeed != 0f -> RobotSystemStatus.MOVING
      currentExpression == ExpressionType.THINKING || currentExpression == ExpressionType.SCANNING -> RobotSystemStatus.THINKING
      leftMotorSpeed == 0f && rightMotorSpeed == 0f -> RobotSystemStatus.IDLE
      else -> RobotSystemStatus.ONLINE
    }

  val obstacles = mutableListOf(
    Obstacle(id = "obs_1", x = 380f, y = 200f, radius = 28f, label = "Block A"),
    Obstacle(id = "obs_2", x = 500f, y = 350f, radius = 32f, label = "Cylinder B"),
    Obstacle(id = "obs_3", x = 200f, y = 420f, radius = 26f, label = "Box C"),
    Obstacle(id = "obs_4", x = 620f, y = 180f, radius = 30f, label = "Tower D")
  )

  val robotPath = LinkedList<Position>()
  val logs = mutableListOf<LogEntry>()
  val memoryEvents = mutableListOf<MemoryEvent>()

  // Navigation state machine
  private enum class NavState {
    FORWARD,
    STOP_AND_SCAN,
    SCAN_LEFT,
    SCAN_CENTER,
    SCAN_RIGHT,
    DECIDE_TURN,
    TURNING,
    BACKING_UP
  }

  private var navState = NavState.FORWARD
  private var navStateTimerMs = 0L
  private var chosenTurnDeg = 0f
  private var turnTargetAngle = 0f

  // Idle timers
  private var lastBlinkTimeMs = System.currentTimeMillis()
  private var nextBlinkIntervalMs = 2800L
  private var blinkStartTimeMs = 0L
  private var isBlinking = false

  private var lastIdleActionMs = System.currentTimeMillis()
  private var lastDecisionEvaluationMs = System.currentTimeMillis()

  init {
    log(LogCategory.SYSTEM, "Robot simulation core initialized")
    log(LogCategory.SYSTEM, "Sensors and telemetry calibrated")
    log(LogCategory.WIFI, "Connected to $wifiSsid ($wifiIp)")
    log(LogCategory.BLUETOOTH, "Paired with $bluetoothDevice")
    recordMemory("Boot", "Robot initialized and ready")
  }

  fun log(category: LogCategory, message: String) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timestamp = timeFormat.format(Date())
    val entry = LogEntry(timestampStr = timestamp, category = category, message = message)
    logs.add(0, entry)
    if (logs.size > 200) {
      logs.removeAt(logs.size - 1)
    }
  }

  fun recordMemory(type: String, details: String) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timestamp = timeFormat.format(Date())
    val event = MemoryEvent(timestampStr = timestamp, eventType = type, details = details)
    memoryEvents.add(0, event)
    if (memoryEvents.size > 100) {
      memoryEvents.removeAt(memoryEvents.size - 1)
    }
  }

  fun update(dtSec: Float) {
    if (!isRunning) return
    val scaledDt = dtSec * simulationSpeed

    // 1. Battery System Drain & Charging
    updateBattery(scaledDt)

    // 2. Servo Smoothing
    updateServo(scaledDt)

    // 3. Sensor Calculations (Raycasting)
    calculateSensorRays()

    // 4. Autonomous Navigation / Motion
    if (mode == RobotMode.AUTONOMOUS || mode == RobotMode.AI) {
      runAutonomousNavigation(scaledDt)
    } else if (mode == RobotMode.MANUAL) {
      applyDifferentialDrive(scaledDt)
    } else if (mode == RobotMode.DANCE) {
      runDanceRoutine(scaledDt)
    } else if (mode == RobotMode.SLEEP) {
      leftMotorSpeed = 0f
      rightMotorSpeed = 0f
    }

    // 5. Blinking and Idle Micro-Movements
    updateIdleBehaviors(scaledDt)

    // 6. Emotion Engine & Mood derivation
    updateEmotions(scaledDt)

    // 7. AI Behavior & Priority Engine
    if (System.currentTimeMillis() - lastDecisionEvaluationMs > 800) {
      evaluateAiDecisions()
      lastDecisionEvaluationMs = System.currentTimeMillis()
    }

    // 8. RGB LED Ring update
    updateRgbRing()
  }

  private fun updateBattery(dt: Float) {
    if (isCharging) {
      batteryPercent = min(100f, batteryPercent + dt * 0.8f)
      batteryVoltage = 7.0f + (batteryPercent / 100f) * 1.4f
    } else {
      // Consumption factors: motors, servo, leds, wifi
      val motorLoad = (abs(leftMotorSpeed) + abs(rightMotorSpeed)) / 200f
      val drainRate = 0.015f + (motorLoad * 0.045f)
      batteryPercent = max(0f, batteryPercent - dt * drainRate)
      batteryVoltage = 7.0f + (batteryPercent / 100f) * 1.4f

      if (batteryPercent <= 10f && mode != RobotMode.SLEEP) {
        mode = RobotMode.SLEEP
        currentExpression = ExpressionType.SLEEP
        log(LogCategory.BATTERY, "CRITICAL: Low battery, entering sleep mode")
      }
    }
  }

  private fun updateServo(dt: Float) {
    val diff = targetServoAngle - servoAngle
    val step = 160f * dt // 160 deg/sec
    if (abs(diff) <= step) {
      servoAngle = targetServoAngle
    } else {
      servoAngle += sign(diff) * step
    }
  }

  fun calculateSensorRays(): List<ScanRay> {
    val angles = listOf(-45f, 0f, 45f)
    val rays = mutableListOf<ScanRay>()

    val headAngleRad = Math.toRadians((rotationDeg + (servoAngle - 90f)).toDouble()).toFloat()

    // Center ray
    val centerDist = rayCastDistance(position.x, position.y, headAngleRad)
    distanceCenter = centerDist
    rays.add(ScanRay(0f, centerDist, null, centerDist > 50f))

    // Left ray (-45 deg relative to servo)
    val leftAngleRad = headAngleRad - Math.toRadians(45.0).toFloat()
    val leftDist = rayCastDistance(position.x, position.y, leftAngleRad)
    distanceLeft = leftDist
    rays.add(ScanRay(-45f, leftDist, null, leftDist > 50f))

    // Right ray (+45 deg relative to servo)
    val rightAngleRad = headAngleRad + Math.toRadians(45.0).toFloat()
    val rightDist = rayCastDistance(position.x, position.y, rightAngleRad)
    distanceRight = rightDist
    rays.add(ScanRay(45f, rightDist, null, rightDist > 50f))

    return rays
  }

  private fun rayCastDistance(originX: Float, originY: Float, angleRad: Float): Float {
    val cosA = cos(angleRad)
    val sinA = sin(angleRad)
    var minDistance = 400f // Max ultrasonic range 400cm

    // 1. Arena walls
    if (cosA > 0.001f) {
      val d = (arenaWidth - originX) / cosA
      if (d in 0f..minDistance) minDistance = d
    } else if (cosA < -0.001f) {
      val d = (-originX) / cosA
      if (d in 0f..minDistance) minDistance = d
    }

    if (sinA > 0.001f) {
      val d = (arenaHeight - originY) / sinA
      if (d in 0f..minDistance) minDistance = d
    } else if (sinA < -0.001f) {
      val d = (-originY) / sinA
      if (d in 0f..minDistance) minDistance = d
    }

    // 2. Obstacles
    for (obs in obstacles) {
      val dx = obs.x - originX
      val dy = obs.y - originY
      val proj = dx * cosA + dy * sinA
      if (proj > 0) {
        val perpDistSq = (dx * dx + dy * dy) - (proj * proj)
        if (perpDistSq < obs.radius * obs.radius) {
          val hitDist = proj - sqrt(max(0f, obs.radius * obs.radius - perpDistSq))
          if (hitDist in 0f..minDistance) {
            minDistance = hitDist
          }
        }
      }
    }

    return max(2f, minDistance)
  }

  private fun runAutonomousNavigation(dt: Float) {
    val now = System.currentTimeMillis()

    when (navState) {
      NavState.FORWARD -> {
        targetServoAngle = 90f
        val speed = 55f
        leftMotorSpeed = speed
        rightMotorSpeed = speed
        behavior = "Exploring"
        currentExpression = ExpressionType.IDLE

        if (distanceCenter < 48f) {
          // Obstacle detected! Stop immediately
          leftMotorSpeed = 0f
          rightMotorSpeed = 0f
          navState = NavState.STOP_AND_SCAN
          navStateTimerMs = now
          behavior = "Obstacle detected"
          currentExpression = ExpressionType.SURPRISED
          log(LogCategory.NAVIGATION, "Obstacle at ${distanceCenter.roundToInt()}cm - stopping to scan")
        }
      }

      NavState.STOP_AND_SCAN -> {
        leftMotorSpeed = 0f
        rightMotorSpeed = 0f
        if (now - navStateTimerMs > 350) {
          navState = NavState.SCAN_LEFT
          targetServoAngle = 135f
          navStateTimerMs = now
          behavior = "Scanning Left"
          currentExpression = ExpressionType.SCANNING
          log(LogCategory.SERVO, "Servo sweep to 135° (Left)")
        }
      }

      NavState.SCAN_LEFT -> {
        if (now - navStateTimerMs > 450) {
          navState = NavState.SCAN_RIGHT
          targetServoAngle = 45f
          navStateTimerMs = now
          behavior = "Scanning Right"
          log(LogCategory.SERVO, "Servo sweep to 45° (Right)")
        }
      }

      NavState.SCAN_RIGHT -> {
        if (now - navStateTimerMs > 450) {
          targetServoAngle = 90f
          navState = NavState.DECIDE_TURN
          behavior = "Evaluating Route"
          currentExpression = ExpressionType.THINKING
        }
      }

      NavState.SCAN_CENTER -> {
        if (now - navStateTimerMs > 350) {
          targetServoAngle = 90f
          navState = NavState.DECIDE_TURN
          behavior = "Evaluating Route"
          currentExpression = ExpressionType.THINKING
        }
      }

      NavState.DECIDE_TURN -> {
        // Compare measured left and right distances
        val chooseRight = if (distanceRight > distanceLeft + 15f) {
          true
        } else if (distanceLeft > distanceRight + 15f) {
          false
        } else {
          // If close, use random direction to avoid getting stuck
          (Math.random() > 0.5)
        }

        chosenTurnDeg = if (chooseRight) 65f else -65f
        turnTargetAngle = (rotationDeg + chosenTurnDeg + 360f) % 360f
        navState = NavState.TURNING
        navStateTimerMs = now
        val directionStr = if (chooseRight) "RIGHT" else "LEFT"
        behavior = "Turning $directionStr"
        log(LogCategory.AI, "Route selected: $directionStr (L:${distanceLeft.roundToInt()}cm, R:${distanceRight.roundToInt()}cm)")
      }

      NavState.TURNING -> {
        val turnSpeed = 48f
        if (chosenTurnDeg > 0) {
          leftMotorSpeed = turnSpeed
          rightMotorSpeed = -turnSpeed
        } else {
          leftMotorSpeed = -turnSpeed
          rightMotorSpeed = turnSpeed
        }

        val angleDiff = abs((rotationDeg - turnTargetAngle + 540f) % 360f - 180f)
        if (angleDiff < 8f || (now - navStateTimerMs > 1200)) {
          leftMotorSpeed = 0f
          rightMotorSpeed = 0f
          navState = NavState.FORWARD
          behavior = "Exploring"
          currentExpression = ExpressionType.HAPPY
          log(LogCategory.NAVIGATION, "Turn completed - resuming forward exploration")
        }
      }

      NavState.BACKING_UP -> {
        leftMotorSpeed = -40f
        rightMotorSpeed = -40f
        if (now - navStateTimerMs > 600) {
          navState = NavState.STOP_AND_SCAN
          navStateTimerMs = now
        }
      }
    }

    applyDifferentialDrive(dt)
  }

  private fun runDanceRoutine(dt: Float) {
    val phase = (System.currentTimeMillis() % 4000) / 1000f
    behavior = "Dancing"
    currentExpression = ExpressionType.CUTE

    if (phase < 1.0f) {
      leftMotorSpeed = 50f
      rightMotorSpeed = -50f
      targetServoAngle = 45f + (phase * 90f)
    } else if (phase < 2.0f) {
      leftMotorSpeed = -50f
      rightMotorSpeed = 50f
      targetServoAngle = 135f - ((phase - 1f) * 90f)
    } else if (phase < 3.0f) {
      leftMotorSpeed = 40f
      rightMotorSpeed = 40f
      targetServoAngle = 90f + sin(phase * 10f) * 25f
    } else {
      leftMotorSpeed = -40f
      rightMotorSpeed = -40f
      targetServoAngle = 90f
    }

    applyDifferentialDrive(dt)
  }

  private fun applyDifferentialDrive(dt: Float) {
    // Wheel base ~48 units, linear velocity average of wheels
    val linearSpeed = (leftMotorSpeed + rightMotorSpeed) * 0.5f
    val angularSpeed = (rightMotorSpeed - leftMotorSpeed) * 1.35f // deg/sec

    rotationDeg = (rotationDeg + angularSpeed * dt + 360f) % 360f

    val rotRad = Math.toRadians(rotationDeg.toDouble()).toFloat()
    val vx = cos(rotRad) * linearSpeed
    val vy = sin(rotRad) * linearSpeed

    var newX = position.x + vx * dt
    var newY = position.y + vy * dt

    // Arena boundary collision with bounce padding
    val margin = robotRadius + 6f
    newX = newX.coerceIn(margin, arenaWidth - margin)
    newY = newY.coerceIn(margin, arenaHeight - margin)

    // Obstacle soft collision
    for (obs in obstacles) {
      val dx = newX - obs.x
      val dy = newY - obs.y
      val dist = sqrt(dx * dx + dy * dy)
      val minDist = robotRadius + obs.radius
      if (dist < minDist && dist > 0.001f) {
        val push = (minDist - dist)
        newX += (dx / dist) * push
        newY += (dy / dist) * push
      }
    }

    position = Position(newX, newY)

    // Record trail
    if (robotPath.isEmpty() || hypot(position.x - robotPath.last.x, position.y - robotPath.last.y) > 16f) {
      robotPath.add(position)
      if (robotPath.size > 80) robotPath.removeFirst()
    }
  }

  private fun updateIdleBehaviors(dt: Float) {
    val now = System.currentTimeMillis()

    // Blinking
    if (!isBlinking && now - lastBlinkTimeMs > nextBlinkIntervalMs) {
      isBlinking = true
      blinkStartTimeMs = now
    }

    if (isBlinking) {
      val elapsed = now - blinkStartTimeMs
      val blinkDuration = 180f
      if (elapsed < blinkDuration / 2) {
        blinkProgress = elapsed / (blinkDuration / 2)
      } else if (elapsed < blinkDuration) {
        blinkProgress = 1f - (elapsed - blinkDuration / 2) / (blinkDuration / 2)
      } else {
        blinkProgress = 0f
        isBlinking = false
        lastBlinkTimeMs = now
        nextBlinkIntervalMs = 2400L + (Math.random() * 3200).toLong()
      }
    }

    // Micro gaze movements when idle
    if (mode == RobotMode.AI && behavior == "Exploring") {
      val gazeShiftPeriod = 2000L
      if (now - lastIdleActionMs > gazeShiftPeriod) {
        lastIdleActionMs = now
        gazeX = (Math.random() * 0.8 - 0.4).toFloat()
        gazeY = (Math.random() * 0.6 - 0.3).toFloat()
      }
    }
  }

  private fun updateEmotions(dt: Float) {
    // Gradual mood convergence and passive decay
    var hap = emotions.happiness
    var ene = emotions.energy
    var tru = emotions.trust
    var cur = emotions.curiosity
    var fea = emotions.fear
    var fri = emotions.friendship
    var bor = emotions.boredom
    var str = emotions.stress
    var sle = emotions.sleepiness

    // Inactivity adds boredom
    bor = min(100f, bor + dt * 0.4f)

    // Low light adds sleepiness
    if (lightLevel < 30f) {
      sle = min(100f, sle + dt * 1.2f)
    } else {
      sle = max(10f, sle - dt * 0.5f)
    }

    // Fear & Stress decay over time
    fea = max(5f, fea - dt * 1.5f)
    str = max(5f, str - dt * 1.2f)

    emotions = EmotionState(hap, ene, tru, cur, fea, fri, bor, str, sle)

    // Derive Mood
    deriveMood()

    // Emotion History Sample every 5 sec
    if (emotionHistory.isEmpty() || System.currentTimeMillis() - emotionHistory.last().timestampMs > 5000) {
      emotionHistory.add(EmotionSample(System.currentTimeMillis(), hap, cur, ene))
      if (emotionHistory.size > 30) emotionHistory.removeAt(0)
    }
  }

  private fun deriveMood() {
    val e = emotions
    when {
      e.sleepiness > 70f || batteryPercent <= 15f -> {
        currentMood = if (batteryPercent <= 15f) RobotMood.TIRED else RobotMood.SLEEPY
        moodIntensity = max(e.sleepiness, 100f - batteryPercent)
        dominantFactors = "Sleepiness + Low Battery"
      }
      e.fear > 55f -> {
        currentMood = RobotMood.SCARED
        moodIntensity = e.fear
        dominantFactors = "Obstacle Proximity + Low Clearance"
      }
      e.stress > 55f -> {
        currentMood = RobotMood.STRESSED
        moodIntensity = e.stress
        dominantFactors = "Rapid Movement + High Noise"
      }
      e.happiness > 75f && e.playfulness() > 70f -> {
        currentMood = RobotMood.PLAYFUL
        moodIntensity = e.happiness
        dominantFactors = "Touch Interaction + Trust"
      }
      e.curiosity > 70f -> {
        currentMood = RobotMood.CURIOUS
        moodIntensity = e.curiosity
        dominantFactors = "Curiosity + Open Terrain"
      }
      e.boredom > 60f -> {
        currentMood = RobotMood.BORED
        moodIntensity = e.boredom
        dominantFactors = "Prolonged Inactivity"
      }
      e.happiness > 60f -> {
        currentMood = RobotMood.HAPPY
        moodIntensity = e.happiness
        dominantFactors = "Stable Power + Safe Enclosure"
      }
      else -> {
        currentMood = RobotMood.CALM
        moodIntensity = 65f
        dominantFactors = "Equilibrium State"
      }
    }
  }

  private fun EmotionState.playfulness(): Float = (happiness + energy + friendship) / 3f

  private fun evaluateAiDecisions() {
    val candidates = mutableListOf<BehaviorCandidate>()

    // Priority layers
    if (batteryPercent <= 12f) {
      priorityLayer = 2 // Battery priority
      behavior = "Low Battery Rest"
      behaviorConfidence = 0.96f
      behaviorReason = "Battery at ${batteryPercent.roundToInt()}% requires rest"
      currentExpression = ExpressionType.SLEEP
      return
    }

    if (distanceCenter < 35f) {
      priorityLayer = 3 // Collision Avoidance
      candidates.add(BehaviorCandidate("AvoidObstacle", 0.94f, "Clear path obstructed"))
      candidates.add(BehaviorCandidate("ScanEnvironment", 0.88f, "Gather distance matrix"))
    } else {
      priorityLayer = 5 // Navigation
      candidates.add(BehaviorCandidate("Explore", 0.84f, "Curiosity high + path clear"))
      candidates.add(BehaviorCandidate("LookAround", 0.65f, "Ambient environment scan"))
      candidates.add(BehaviorCandidate("Interact", 0.72f, "High friendship affinity"))
      candidates.add(BehaviorCandidate("Dance", 0.40f, "High energy reserve"))
    }

    behaviorConfidence = candidates.maxOfOrNull { it.score } ?: 0.8f
    behaviorReason = candidates.maxByOrNull { it.score }?.reason ?: "Stable autonomous mode"
  }

  private fun updateRgbRing() {
    when (rgbEffect) {
      RgbEffectMode.AUTO_EMOTION -> {
        val color = when (currentMood) {
          RobotMood.HAPPY -> 0xFF37D67A // green/cyan
          RobotMood.SAD -> 0xFF4F8CFF // blue
          RobotMood.CURIOUS -> 0xFFFF9800 // orange
          RobotMood.ANGRY, RobotMood.SCARED -> 0xFFFF5D73 // red
          RobotMood.PLAYFUL, RobotMood.EXCITED -> 0xFF32D6FF // cyan
          RobotMood.SLEEPY, RobotMood.TIRED -> 0xFF283593 // dim blue
          else -> 0xFF4F8CFF
        }
        rgbColor = color
        for (i in 0 until 12) rgbRingColors[i] = color
      }
      RgbEffectMode.CHASE -> {
        val time = (System.currentTimeMillis() / 80) % 12
        for (i in 0 until 12) {
          val hue = ((i + time) % 12) * 30f
          rgbRingColors[i] = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)).toLong() and 0xFFFFFFFFL
        }
      }
      RgbEffectMode.WARNING -> {
        val flash = (System.currentTimeMillis() / 250) % 2 == 0L
        val c = if (flash) 0xFFFF5D73 else 0xFF220508
        for (i in 0 until 12) rgbRingColors[i] = c
      }
      else -> {
        for (i in 0 until 12) rgbRingColors[i] = rgbColor
      }
    }
  }

  // User Actions
  fun touchRobot(): String {
    isTouchActive = true
    touchCount++
    lastTouchTimeMs = System.currentTimeMillis()

    // Emotion shifts
    emotions = emotions.copy(
      happiness = min(100f, emotions.happiness + 15f),
      trust = min(100f, emotions.trust + 10f),
      friendship = min(100f, emotions.friendship + 8f),
      boredom = max(0f, emotions.boredom - 20f)
    )

    currentExpression = ExpressionType.HEART_EYES
    currentMood = RobotMood.HAPPY
    targetServoAngle = 90f + (if (Math.random() > 0.5) 20f else -20f)

    log(LogCategory.SENSOR, "Capacitive touch detected (Count: $touchCount)")
    log(LogCategory.EMOTION, "Happiness +15, Trust +10")
    recordMemory("Touch Interaction", "User touched robot head. Rapport deepened.")

    currentVoicePhrase = "I like when you pet me!"
    return currentVoicePhrase
  }

  fun triggerWake() {
    mode = RobotMode.AI
    currentExpression = ExpressionType.WAKE
    emotions = emotions.copy(sleepiness = 10f, energy = 90f)
    log(LogCategory.SYSTEM, "Robot wake command received")
    currentVoicePhrase = "I'm awake and ready!"
  }

  fun triggerSleep() {
    mode = RobotMode.SLEEP
    currentExpression = ExpressionType.SLEEP
    leftMotorSpeed = 0f
    rightMotorSpeed = 0f
    emotions = emotions.copy(sleepiness = 90f)
    log(LogCategory.SYSTEM, "Robot entered low-power sleep")
    currentVoicePhrase = "Good night!"
  }

  fun triggerDance() {
    mode = RobotMode.DANCE
    emotions = emotions.copy(happiness = 95f, energy = 90f)
    log(LogCategory.AI, "Dance routine activated")
    currentVoicePhrase = "Let's dance!"
  }

  fun manualMove(dir: String) {
    mode = RobotMode.MANUAL
    when (dir) {
      "FORWARD" -> {
        leftMotorSpeed = 65f
        rightMotorSpeed = 65f
        behavior = "Moving Forward"
        currentExpression = ExpressionType.HAPPY
      }
      "BACKWARD" -> {
        leftMotorSpeed = -55f
        rightMotorSpeed = -55f
        behavior = "Reversing"
        currentExpression = ExpressionType.LOOK_DOWN
      }
      "LEFT" -> {
        leftMotorSpeed = -45f
        rightMotorSpeed = 45f
        behavior = "Spinning Left"
        currentExpression = ExpressionType.LOOK_LEFT
      }
      "RIGHT" -> {
        leftMotorSpeed = 45f
        rightMotorSpeed = -45f
        behavior = "Spinning Right"
        currentExpression = ExpressionType.LOOK_RIGHT
      }
      "STOP" -> {
        leftMotorSpeed = 0f
        rightMotorSpeed = 0f
        behavior = "Stopped"
        currentExpression = ExpressionType.IDLE
      }
    }
    log(LogCategory.MOTOR, "Manual command: $dir (L:${leftMotorSpeed.toInt()}, R:${rightMotorSpeed.toInt()})")
  }
}
