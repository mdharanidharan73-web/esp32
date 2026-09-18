package com.example.model

data class Position(
  val x: Float,
  val y: Float
)

data class Obstacle(
  val id: String,
  val x: Float,
  val y: Float,
  val radius: Float = 24f,
  val label: String = "Obstacle"
)

data class ScanRay(
  val angleDeg: Float,
  val distanceCm: Float,
  val hitPoint: Position?,
  val isClear: Boolean
)

data class EmotionState(
  val happiness: Float = 80f,
  val energy: Float = 90f,
  val trust: Float = 50f,
  val curiosity: Float = 75f,
  val fear: Float = 10f,
  val friendship: Float = 40f,
  val boredom: Float = 10f,
  val stress: Float = 5f,
  val sleepiness: Float = 15f
)

data class EmotionSample(
  val timestampMs: Long,
  val happiness: Float,
  val curiosity: Float,
  val energy: Float
)

enum class RobotMood(val label: String, val description: String) {
  HAPPY("Happy", "Cheerful and responsive"),
  CALM("Calm", "Peaceful and balanced"),
  CURIOUS("Curious", "Investigating environment"),
  PLAYFUL("Playful", "Ready for games and tricks"),
  EXCITED("Excited", "High energetic enthusiasm"),
  SAD("Sad", "Low energy and feeling neglected"),
  SCARED("Scared", "Startled by obstacle or loud noise"),
  ANGRY("Angry", "Irritated by rapid jarring inputs"),
  TIRED("Tired", "Battery depleting"),
  BORED("Bored", "Lack of recent interaction"),
  SLEEPY("Sleepy", "Low light, ready for slumber"),
  STRESSED("Stressed", "Too many collisions/noise"),
  CONFUSED("Confused", "Navigation pathway blocked")
}

data class PersonalityTraits(
  val playfulness: Float = 80f,
  val curiosity: Float = 85f,
  val caution: Float = 60f,
  val energy: Float = 75f,
  val sociability: Float = 70f,
  val sensitivity: Float = 50f,
  val patience: Float = 65f
)

enum class PersonalityPreset(val title: String, val traits: PersonalityTraits) {
  FRIENDLY("Friendly", PersonalityTraits(playfulness = 75f, curiosity = 70f, caution = 50f, energy = 70f, sociability = 90f, sensitivity = 60f, patience = 80f)),
  CUTE("Cute", PersonalityTraits(playfulness = 85f, curiosity = 80f, caution = 65f, energy = 75f, sociability = 85f, sensitivity = 70f, patience = 75f)),
  CURIOUS("Curious", PersonalityTraits(playfulness = 70f, curiosity = 95f, caution = 40f, energy = 80f, sociability = 65f, sensitivity = 50f, patience = 60f)),
  ENERGETIC("Energetic", PersonalityTraits(playfulness = 90f, curiosity = 85f, caution = 35f, energy = 95f, sociability = 80f, sensitivity = 45f, patience = 40f)),
  CALM("Calm", PersonalityTraits(playfulness = 40f, curiosity = 55f, caution = 75f, energy = 45f, sociability = 60f, sensitivity = 30f, patience = 95f)),
  PLAYFUL("Playful", PersonalityTraits(playfulness = 95f, curiosity = 85f, caution = 40f, energy = 85f, sociability = 85f, sensitivity = 50f, patience = 55f)),
  PROTECTIVE("Protective", PersonalityTraits(playfulness = 45f, curiosity = 60f, caution = 90f, energy = 70f, sociability = 50f, sensitivity = 80f, patience = 80f))
}

enum class RobotMode(val label: String) {
  AI("AI Mode"),
  MANUAL("Manual Control"),
  AUTONOMOUS("Autonomous Explore"),
  FOLLOW("Follow Target"),
  DANCE("Dance Performance"),
  SLEEP("Deep Sleep")
}

enum class RobotSystemStatus(val label: String) {
  ONLINE("ONLINE"),
  IDLE("IDLE"),
  THINKING("THINKING"),
  MOVING("MOVING"),
  SLEEPING("SLEEPING"),
  LOW_BATTERY("LOW BATTERY")
}

enum class ExpressionType(val title: String) {
  IDLE("Idle"),
  HAPPY("Happy"),
  SAD("Sad"),
  ANGRY("Angry"),
  THINKING("Thinking"),
  SLEEP("Sleep"),
  BLINK("Blink"),
  DOUBLE_BLINK("Double Blink"),
  WINK("Wink"),
  LOOK_LEFT("Left"),
  LOOK_RIGHT("Right"),
  LOOK_UP("Up"),
  LOOK_DOWN("Down"),
  CONFUSED("Confused"),
  HEART_EYES("Heart Eyes"),
  STAR_EYES("Star Eyes"),
  SPIRAL_EYES("Spiral Eyes"),
  LAUGH("Laugh"),
  CRY("Cry"),
  CUTE("Cute"),
  SCARED("Scared"),
  EXCITED("Excited"),
  SURPRISED("Surprised"),
  YAWN("Yawn"),
  BORED("Bored"),
  ROBOT("Robot"),
  AI("AI"),
  LISTENING("Listening"),
  TALKING("Talking"),
  LOADING("Loading"),
  SCANNING("Scanning"),
  WAKE("Wake")
}

enum class RgbEffectMode(val title: String) {
  AUTO_EMOTION("Auto Emotion"),
  BREATHING("Breathing"),
  CHASE("Rainbow Chase"),
  WARNING("Warning Pulse"),
  CHARGING("Charging Flow"),
  STATIC("Static Color")
}

data class BehaviorCandidate(
  val name: String,
  val score: Float,
  val reason: String
)

data class LogEntry(
  val id: Long = System.currentTimeMillis(),
  val timestampStr: String,
  val category: LogCategory,
  val message: String
)

enum class LogCategory {
  SYSTEM,
  SENSOR,
  EMOTION,
  AI,
  NAVIGATION,
  MOTOR,
  SERVO,
  MEMORY,
  VOICE,
  WIFI,
  BLUETOOTH,
  BATTERY
}

data class MemoryEvent(
  val timestampStr: String,
  val eventType: String,
  val details: String,
  val confidence: Float = 0.95f
) {
  val title: String get() = eventType
  val description: String get() = details
}

data class RobotSettings(
  val robotName: String = "ARIA",
  val ownerName: String = "Engineer",
  val maxSpeed: Float = 85f,
  val acceleration: Float = 40f,
  val braking: Float = 60f,
  val sensorSensitivity: Float = 80f,
  val autoSleepTimeoutSec: Int = 120,
  val lowBatteryThreshold: Float = 30f,
  val criticalBatteryThreshold: Float = 10f,
  val soundVolume: Float = 0.85f,
  val speechRate: Float = 1.0f
)
