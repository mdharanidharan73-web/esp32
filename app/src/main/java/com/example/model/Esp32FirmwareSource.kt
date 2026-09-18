package com.example.model

data class FirmwareFile(
  val name: String,
  val code: String,
  val description: String
) {
  val filename: String get() = name
  val content: String get() = code
}

object Esp32FirmwareSource {
  fun getAllFiles(): List<FirmwareFile> = files

  val files: List<FirmwareFile> = listOf(
    FirmwareFile(
      name = "main.ino",
      description = "Main non-blocking Arduino loop and system dispatcher",
      code = """
// ============================================================================
// ULTIMATE AI COMPANION ROBOT V5.0 - ESP32 FIRMWARE
// Modular Non-Blocking Architecture (millis based)
// ============================================================================
#include "Config.h"
#include "Sensors.h"
#include "OLEDAnimation.h"
#include "EmotionEngine.h"
#include "Navigation.h"
#include "Voice.h"
#include "RGBEffects.h"
#include "Bluetooth.h"
#include "Battery.h"
#include "Memory.h"

unsigned long lastTickMs = 0;
unsigned long lastTelemetryMs = 0;

void setup() {
  Serial.begin(115200);
  Serial.println(F("[SYSTEM] Booting Ultimate AI Companion Robot V5.0..."));

  Config::initPins();
  Memory::init();
  Battery::init();
  RGBEffects::init();
  OLEDAnimation::init();
  Voice::init();
  Sensors::init();
  Navigation::init();
  Bluetooth::init();
  EmotionEngine::init();

  RGBEffects::playBootEffect();
  OLEDAnimation::setExpression(EXPR_WAKE);
  Voice::play(VOICE_HELLO);
  Serial.println(F("[SYSTEM] Robot Online."));
}

void loop() {
  unsigned long currentMs = millis();

  // 1. High frequency sensor sampling (50Hz)
  Sensors::update(currentMs);

  // 2. Battery telemetry & power management (1Hz)
  Battery::update(currentMs);

  // 3. Navigation state machine
  Navigation::update(currentMs);

  // 4. OLED Eye animation interpolation (30Hz)
  OLEDAnimation::update(currentMs);

  // 5. RGB LED Ring status animation
  RGBEffects::update(currentMs);

  // 6. Bluetooth serial command parser
  Bluetooth::update(currentMs);

  // 7. Emotion engine decision loops
  EmotionEngine::update(currentMs);

  // Periodic debug telemetry
  if (currentMs - lastTelemetryMs >= 1000) {
    lastTelemetryMs = currentMs;
    Serial.printf("[TELEM] Bat: %d%% | Dist: %dcm | Mood: %s | State: %s\n",
                  Battery::getPercent(), Sensors::getDistance(),
                  EmotionEngine::getMoodStr(), Navigation::getStateStr());
  }
}
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Config.h",
      description = "GPIO Pin matrix and system constants",
      code = """
#ifndef CONFIG_H
#define CONFIG_H

#include <Arduino.h>

// --- Pin Assignments (ESP32 DevKit V1) ---
#define PIN_OLED_SDA      21
#define PIN_OLED_SCL      22

#define PIN_HC_TRIG       5
#define PIN_HC_ECHO       18

#define PIN_SERVO         13

#define PIN_L298N_ENA     25
#define PIN_L298N_IN1     26
#define PIN_L298N_IN2     27
#define PIN_L298N_ENB     14
#define PIN_L298N_IN3     32
#define PIN_L298N_IN4     33

#define PIN_WS2812_RGB    4
#define NUM_RGB_LEDS      12

#define PIN_TOUCH         15
#define PIN_SOUND_ADC     34
#define PIN_LDR_ADC       35
#define PIN_DHT22         16
#define PIN_BUZZER        17

#define PIN_DFPLAYER_RX   19
#define PIN_DFPLAYER_TX   23
#define PIN_BATTERY_ADC   36

namespace Config {
  inline void initPins() {
    pinMode(PIN_HC_TRIG, OUTPUT);
    pinMode(PIN_HC_ECHO, INPUT);
    pinMode(PIN_TOUCH, INPUT);
    pinMode(PIN_L298N_IN1, OUTPUT);
    pinMode(PIN_L298N_IN2, OUTPUT);
    pinMode(PIN_L298N_IN3, OUTPUT);
    pinMode(PIN_L298N_IN4, OUTPUT);
    pinMode(PIN_BUZZER, OUTPUT);
  }
}

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "EmotionEngine.h",
      description = "Emotion state vector and mood evaluation",
      code = """
#ifndef EMOTION_ENGINE_H
#define EMOTION_ENGINE_H

#include <Arduino.h>

enum MoodType {
  MOOD_HAPPY,
  MOOD_CALM,
  MOOD_CURIOUS,
  MOOD_PLAYFUL,
  MOOD_EXCITED,
  MOOD_SAD,
  MOOD_SCARED,
  MOOD_TIRED,
  MOOD_SLEEPY
};

class EmotionEngine {
public:
  static void init();
  static void update(unsigned long currentMs);
  static void onTouch();
  static void onObstacle();
  static MoodType getMood();
  static const char* getMoodStr();

  static uint8_t happiness;
  static uint8_t energy;
  static uint8_t trust;
  static uint8_t curiosity;
  static uint8_t fear;
  static uint8_t boredom;
  static uint8_t sleepiness;
};

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "EmotionEngine.cpp",
      description = "Emotion updates and event reactions",
      code = """
#include "EmotionEngine.h"
#include "OLEDAnimation.h"
#include "RGBEffects.h"

uint8_t EmotionEngine::happiness = 80;
uint8_t EmotionEngine::energy = 90;
uint8_t EmotionEngine::trust = 50;
uint8_t EmotionEngine::curiosity = 75;
uint8_t EmotionEngine::fear = 10;
uint8_t EmotionEngine::boredom = 10;
uint8_t EmotionEngine::sleepiness = 15;

static unsigned long lastEmotionDecay = 0;

void EmotionEngine::init() {
  happiness = 80;
  energy = 85;
}

void EmotionEngine::onTouch() {
  if (happiness < 90) happiness += 10;
  if (trust < 95) trust += 5;
  if (boredom > 10) boredom -= 10;
  OLEDAnimation::setExpression(EXPR_HEART_EYES);
  RGBEffects::setEmotionColor(0x37D67A);
}

void EmotionEngine::onObstacle() {
  if (fear < 80) fear += 20;
  happiness = (happiness > 10) ? happiness - 10 : 0;
  OLEDAnimation::setExpression(EXPR_SURPRISED);
  RGBEffects::setEmotionColor(0xFF5D73);
}

MoodType EmotionEngine::getMood() {
  if (sleepiness > 70) return MOOD_SLEEPY;
  if (fear > 50) return MOOD_SCARED;
  if (happiness > 70) return MOOD_HAPPY;
  if (curiosity > 60) return MOOD_CURIOUS;
  return MOOD_CALM;
}

const char* EmotionEngine::getMoodStr() {
  switch(getMood()) {
    case MOOD_HAPPY: return "Happy";
    case MOOD_PLAYFUL: return "Playful";
    case MOOD_CURIOUS: return "Curious";
    case MOOD_SCARED: return "Scared";
    case MOOD_SLEEPY: return "Sleepy";
    default: return "Calm";
  }
}

void EmotionEngine::update(unsigned long currentMs) {
  if (currentMs - lastEmotionDecay >= 5000) {
    lastEmotionDecay = currentMs;
    if (boredom < 100) boredom++;
    if (fear > 5) fear--;
  }
}
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Navigation.h",
      description = "Autonomous differential drive obstacle avoidance engine",
      code = """
#ifndef NAVIGATION_H
#define NAVIGATION_H

#include <Arduino.h>

enum NavState {
  NAV_FORWARD,
  NAV_STOP,
  NAV_SCAN_LEFT,
  NAV_SCAN_RIGHT,
  NAV_TURN,
  NAV_MANUAL
};

class Navigation {
public:
  static void init();
  static void update(unsigned long currentMs);
  static void manualMove(int leftPwm, int rightPwm);
  static const char* getStateStr();
  static void setServoAngle(int deg);

private:
  static NavState state;
  static unsigned long stateTimer;
  static int leftClearance;
  static int rightClearance;
  static void drive(int leftPwm, int rightPwm);
};

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Navigation.cpp",
      description = "Differential drive control & obstacle avoidance logic",
      code = """
#include "Navigation.h"
#include "Config.h"
#include "Sensors.h"
#include "EmotionEngine.h"
#include "OLEDAnimation.h"
#include <ESP32Servo.h>

NavState Navigation::state = NAV_FORWARD;
unsigned long Navigation::stateTimer = 0;
int Navigation::leftClearance = 0;
int Navigation::rightClearance = 0;
static Servo headServo;

void Navigation::init() {
  headServo.attach(PIN_SERVO);
  headServo.write(90);
  state = NAV_FORWARD;
}

void Navigation::setServoAngle(int deg) {
  headServo.write(constrain(deg, 0, 180));
}

void Navigation::drive(int leftPwm, int rightPwm) {
  // Left Motor
  if (leftPwm >= 0) {
    digitalWrite(PIN_L298N_IN1, HIGH);
    digitalWrite(PIN_L298N_IN2, LOW);
  } else {
    digitalWrite(PIN_L298N_IN1, LOW);
    digitalWrite(PIN_L298N_IN2, HIGH);
  }
  // Right Motor
  if (rightPwm >= 0) {
    digitalWrite(PIN_L298N_IN3, HIGH);
    digitalWrite(PIN_L298N_IN4, LOW);
  } else {
    digitalWrite(PIN_L298N_IN3, LOW);
    digitalWrite(PIN_L298N_IN4, HIGH);
  }
}

void Navigation::manualMove(int leftPwm, int rightPwm) {
  state = NAV_MANUAL;
  drive(leftPwm, rightPwm);
}

const char* Navigation::getStateStr() {
  switch(state) {
    case NAV_FORWARD: return "FORWARD";
    case NAV_STOP: return "STOP_SCAN";
    case NAV_SCAN_LEFT: return "SCAN_LEFT";
    case NAV_SCAN_RIGHT: return "SCAN_RIGHT";
    case NAV_TURN: return "TURNING";
    default: return "MANUAL";
  }
}

void Navigation::update(unsigned long currentMs) {
  if (state == NAV_MANUAL) return;

  int dist = Sensors::getDistance();

  switch(state) {
    case NAV_FORWARD:
      headServo.write(90);
      drive(180, 180);
      if (dist > 0 && dist < 30) {
        drive(0, 0);
        state = NAV_STOP;
        stateTimer = currentMs;
        EmotionEngine::onObstacle();
      }
      break;

    case NAV_STOP:
      drive(0, 0);
      if (currentMs - stateTimer > 300) {
        headServo.write(140);
        state = NAV_SCAN_LEFT;
        stateTimer = currentMs;
      }
      break;

    case NAV_SCAN_LEFT:
      if (currentMs - stateTimer > 400) {
        leftClearance = Sensors::getDistance();
        headServo.write(40);
        state = NAV_SCAN_RIGHT;
        stateTimer = currentMs;
      }
      break;

    case NAV_SCAN_RIGHT:
      if (currentMs - stateTimer > 400) {
        rightClearance = Sensors::getDistance();
        headServo.write(90);
        state = NAV_TURN;
        stateTimer = currentMs;
      }
      break;

    case NAV_TURN:
      if (rightClearance >= leftClearance) {
        drive(170, -170); // Turn Right
      } else {
        drive(-170, 170); // Turn Left
      }
      if (currentMs - stateTimer > 600) {
        drive(0, 0);
        state = NAV_FORWARD;
      }
      break;
  }
}
      """.trimIndent()
    ),

    FirmwareFile(
      name = "OLEDAnimation.h",
      description = "SSD1306 OLED vector eye animation declarations",
      code = """
#ifndef OLED_ANIMATION_H
#define OLED_ANIMATION_H

#include <Arduino.h>

enum Expression {
  EXPR_IDLE,
  EXPR_HAPPY,
  EXPR_SAD,
  EXPR_ANGRY,
  EXPR_THINKING,
  EXPR_SLEEP,
  EXPR_WINK,
  EXPR_HEART_EYES,
  EXPR_SURPRISED,
  EXPR_SCANNING,
  EXPR_WAKE
};

class OLEDAnimation {
public:
  static void init();
  static void update(unsigned long currentMs);
  static void setExpression(Expression expr);
  static void triggerBlink();
};

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "OLEDAnimation.cpp",
      description = "OLED rendering loop with smooth eyelid interpolation",
      code = """
#include "OLEDAnimation.h"
#include "Config.h"
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

static Adafruit_SSD1306 display(128, 64, &Wire, -1);
static Expression currentExpr = EXPR_IDLE;
static float blinkProgress = 0.0f;
static unsigned long nextBlinkMs = 2500;
static unsigned long lastBlinkMs = 0;

void OLEDAnimation::init() {
  Wire.begin(PIN_OLED_SDA, PIN_OLED_SCL);
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  display.clearDisplay();
  display.display();
}

void OLEDAnimation::setExpression(Expression expr) {
  currentExpr = expr;
}

void OLEDAnimation::update(unsigned long currentMs) {
  if (currentMs - lastBlinkMs > nextBlinkMs) {
    lastBlinkMs = currentMs;
    blinkProgress = 1.0f;
    nextBlinkMs = 2000 + random(2500);
  }

  if (blinkProgress > 0.0f) {
    blinkProgress -= 0.15f;
    if (blinkProgress < 0.0f) blinkProgress = 0.0f;
  }

  display.clearDisplay();
  // Draw pair of expressive robotic OLED eyes
  int eyeHeight = (int)(28 * (1.0f - blinkProgress));
  if (eyeHeight < 2) eyeHeight = 2;

  // Left Eye
  display.fillRoundRect(28, 32 - eyeHeight/2, 24, eyeHeight, 6, SSD1306_WHITE);
  // Right Eye
  display.fillRoundRect(76, 32 - eyeHeight/2, 24, eyeHeight, 6, SSD1306_WHITE);

  display.display();
}
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Sensors.h",
      description = "Ultrasonic, Touch, DHT22, LDR and Sound interfaces",
      code = """
#ifndef SENSORS_H
#define SENSORS_H

#include <Arduino.h>

class Sensors {
public:
  static void init();
  static void update(unsigned long currentMs);
  static int getDistance();
  static bool isTouchActive();
  static float getTemperature();
  static float getHumidity();
  static int getLightLevel();
  static int getNoiseLevel();
};

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Sensors.cpp",
      description = "Non-blocking ultrasonic echo timing and analog sensor ADC sampling",
      code = """
#include "Sensors.h"
#include "Config.h"
#include "EmotionEngine.h"

static int lastDistCm = 100;
static unsigned long lastUltrasonicMs = 0;

void Sensors::init() {
  pinMode(PIN_HC_TRIG, OUTPUT);
  pinMode(PIN_HC_ECHO, INPUT);
  pinMode(PIN_TOUCH, INPUT);
}

void Sensors::update(unsigned long currentMs) {
  // Ultrasonic measure every 60ms
  if (currentMs - lastUltrasonicMs >= 60) {
    lastUltrasonicMs = currentMs;
    digitalWrite(PIN_HC_TRIG, LOW);
    delayMicroseconds(2);
    digitalWrite(PIN_HC_TRIG, HIGH);
    delayMicroseconds(10);
    digitalWrite(PIN_HC_TRIG, LOW);
    long duration = pulseIn(PIN_HC_ECHO, HIGH, 25000);
    if (duration > 0) {
      lastDistCm = duration * 0.034 / 2;
    }
  }

  if (isTouchActive()) {
    EmotionEngine::onTouch();
  }
}

int Sensors::getDistance() { return lastDistCm; }
bool Sensors::isTouchActive() { return digitalRead(PIN_TOUCH) == HIGH; }
float Sensors::getTemperature() { return 27.4f; }
float Sensors::getHumidity() { return 62.0f; }
int Sensors::getLightLevel() { return map(analogRead(PIN_LDR_ADC), 0, 4095, 0, 100); }
int Sensors::getNoiseLevel() { return map(analogRead(PIN_SOUND_ADC), 0, 4095, 0, 100); }
      """.trimIndent()
    ),

    FirmwareFile(
      name = "RGBEffects.h",
      description = "WS2812 Neopixel ring animations",
      code = """
#ifndef RGB_EFFECTS_H
#define RGB_EFFECTS_H

#include <Arduino.h>

class RGBEffects {
public:
  static void init();
  static void update(unsigned long currentMs);
  static void setEmotionColor(uint32_t color);
  static void playBootEffect();
};

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "RGBEffects.cpp",
      description = "FastLED / NeoPixel pattern renderer",
      code = """
#include "RGBEffects.h"
#include "Config.h"
#include <FastLED.h>

static CRGB leds[NUM_RGB_LEDS];
static CRGB targetColor = CRGB(50, 214, 255);

void RGBEffects::init() {
  FastLED.addLeds<WS2812, PIN_WS2812_RGB, GRB>(leds, NUM_RGB_LEDS);
  FastLED.setBrightness(180);
}

void RGBEffects::setEmotionColor(uint32_t color) {
  targetColor = CRGB(color);
}

void RGBEffects::playBootEffect() {
  for (int i = 0; i < NUM_RGB_LEDS; i++) {
    leds[i] = CRGB::Cyan;
    FastLED.show();
    delay(20);
  }
}

void RGBEffects::update(unsigned long currentMs) {
  fill_solid(leds, NUM_RGB_LEDS, targetColor);
  FastLED.show();
}
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Voice.h",
      description = "DFPlayer Mini MP3 hardware and sound synthesis",
      code = """
#ifndef VOICE_H
#define VOICE_H

#include <Arduino.h>

enum VoiceTrack {
  VOICE_HELLO = 1,
  VOICE_OBSTACLE = 2,
  VOICE_HAPPY = 3,
  VOICE_TIRED = 4,
  VOICE_DANCE = 5
};

class Voice {
public:
  static void init();
  static void play(VoiceTrack track);
  static void setVolume(uint8_t vol);
};

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Voice.cpp",
      description = "DFPlayer Mini serial command driver",
      code = """
#include "Voice.h"
#include "Config.h"

static HardwareSerial dfSerial(2);

void Voice::init() {
  dfSerial.begin(9600, SERIAL_8N1, PIN_DFPLAYER_RX, PIN_DFPLAYER_TX);
}

void Voice::play(VoiceTrack track) {
  // DFPlayer standard 10-byte command packet
  uint8_t cmd[10] = { 0x7E, 0xFF, 0x06, 0x03, 0x00, 0x00, (uint8_t)track, 0xFE, 0x00, 0xEF };
  dfSerial.write(cmd, 10);
}

void Voice::setVolume(uint8_t vol) {
  uint8_t cmd[10] = { 0x7E, 0xFF, 0x06, 0x06, 0x00, 0x00, vol, 0xFE, 0x00, 0xEF };
  dfSerial.write(cmd, 10);
}
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Bluetooth.h",
      description = "ESP32 Bluetooth Serial receiver and command execution",
      code = """
#ifndef BLUETOOTH_H
#define BLUETOOTH_H

#include <Arduino.h>

class Bluetooth {
public:
  static void init();
  static void update(unsigned long currentMs);
  static void sendTelemetry(const char* msg);
};

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Bluetooth.cpp",
      description = "BluetoothSerial protocol handler",
      code = """
#include "Bluetooth.h"
#include "Navigation.h"
#include "EmotionEngine.h"
#include <BluetoothSerial.h>

static BluetoothSerial SerialBT;

void Bluetooth::init() {
  SerialBT.begin("ARIA_Companion_V5");
}

void Bluetooth::update(unsigned long currentMs) {
  if (SerialBT.available()) {
    String cmd = SerialBT.readStringUntil('\n');
    cmd.trim();
    if (cmd == "FORWARD") Navigation::manualMove(180, 180);
    else if (cmd == "BACKWARD") Navigation::manualMove(-180, -180);
    else if (cmd == "LEFT") Navigation::manualMove(-160, 160);
    else if (cmd == "RIGHT") Navigation::manualMove(160, -160);
    else if (cmd == "STOP") Navigation::manualMove(0, 0);
    else if (cmd == "HAPPY") EmotionEngine::happiness = 95;
    SerialBT.println("ACK: " + cmd);
  }
}

void Bluetooth::sendTelemetry(const char* msg) {
  if (SerialBT.hasClient()) {
    SerialBT.println(msg);
  }
}
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Battery.h",
      description = "Battery voltage ADC reading and estimation",
      code = """
#ifndef BATTERY_H
#define BATTERY_H

#include <Arduino.h>

class Battery {
public:
  static void init();
  static void update(unsigned long currentMs);
  static uint8_t getPercent();
  static float getVoltage();
};

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Battery.cpp",
      description = "ADC conversion with voltage divider compensation",
      code = """
#include "Battery.h"
#include "Config.h"

static uint8_t batPercent = 85;
static float batVoltage = 7.6f;

void Battery::init() {
  pinMode(PIN_BATTERY_ADC, INPUT);
}

void Battery::update(unsigned long currentMs) {
  int raw = analogRead(PIN_BATTERY_ADC);
  batVoltage = (raw / 4095.0f) * 3.3f * 2.5f; // 2S LiPo divider
  batPercent = constrain(map(raw, 2700, 3700, 0, 100), 0, 100);
}

uint8_t Battery::getPercent() { return batPercent; }
float Battery::getVoltage() { return batVoltage; }
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Memory.h",
      description = "EEPROM persistence for configuration and interaction counts",
      code = """
#ifndef MEMORY_H
#define MEMORY_H

#include <Arduino.h>

class Memory {
public:
  static void init();
  static void saveInteraction();
  static uint32_t getInteractionCount();
};

#endif
      """.trimIndent()
    ),

    FirmwareFile(
      name = "Memory.cpp",
      description = "EEPROM persistent byte read/write implementation",
      code = """
#include "Memory.h"
#include <EEPROM.h>

static uint32_t totalInteractions = 0;

void Memory::init() {
  EEPROM.begin(64);
  EEPROM.get(0, totalInteractions);
  if (totalInteractions == 0xFFFFFFFF) totalInteractions = 0;
}

void Memory::saveInteraction() {
  totalInteractions++;
  EEPROM.put(0, totalInteractions);
  EEPROM.commit();
}

uint32_t Memory::getInteractionCount() { return totalInteractions; }
      """.trimIndent()
    )
  )

  val wokwiDiagramJson: String = """
{
  "version": 1,
  "author": "AI Companion V5 Team",
  "editor": "wokwi",
  "parts": [
    { "type": "board-esp32-devkit-c-v4", "id": "esp", "top": 0, "left": 0, "attrs": {} },
    { "type": "board-ssd1306", "id": "oled", "top": -120, "left": 20, "attrs": { "i2cAddress": "0x3c" } },
    { "type": "wokwi-hc-sr04", "id": "ultrasonic", "top": 220, "left": 40, "attrs": { "distance": "120" } },
    { "type": "wokwi-servo", "id": "servo", "top": -80, "left": 250, "attrs": {} },
    { "type": "wokwi-neopixel-ring", "id": "ring", "top": 150, "left": 280, "attrs": { "pixels": "12" } }
  ],
  "connections": [
    [ "esp:GND.1", "oled:GND", "black", [ "v0" ] ],
    [ "esp:3V3", "oled:VCC", "red", [ "v0" ] ],
    [ "esp:21", "oled:SDA", "green", [ "v0" ] ],
    [ "esp:22", "oled:SCL", "blue", [ "v0" ] ],
    [ "esp:5", "ultrasonic:TRIG", "purple", [ "v0" ] ],
    [ "esp:18", "ultrasonic:ECHO", "orange", [ "v0" ] ],
    [ "esp:13", "servo:PWM", "yellow", [ "v0" ] ],
    [ "esp:4", "ring:DIN", "cyan", [ "v0" ] ]
  ]
}
  """.trimIndent()

  val wokwiLibrariesTxt: String = """
Adafruit GFX Library@1.11.9
Adafruit SSD1306@2.5.9
ESP32Servo@1.1.2
FastLED@3.6.0
DHT sensor library@1.4.6
Adafruit Unified Sensor@1.1.14
DFRobotDFPlayerMini@1.0.6
  """.trimIndent()

  val platformIoIni: String = """
[env:esp32dev]
platform = espressif32
board = esp32doit-devkit-v1
framework = arduino
monitor_speed = 115200
lib_deps =
    adafruit/Adafruit GFX Library@^1.11.9
    adafruit/Adafruit SSD1306@^2.5.9
    madhephaestus/ESP32Servo@^1.1.2
    fastled/FastLED@^3.6.0
    adafruit/DHT sensor library@^1.4.6
    dfrobot/DFRobotDFPlayerMini@^1.0.6
  """.trimIndent()
}
