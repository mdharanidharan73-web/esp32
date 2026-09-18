package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI Companion Robot", appName)
  }

  @Test
  fun `test robot simulation engine initialization`() {
    val engine = com.example.engine.RobotSimulationEngine()
    assertEquals(88f, engine.batteryPercent, 0.1f)
    assertEquals(com.example.model.RobotMood.HAPPY, engine.currentMood)
    assertEquals(com.example.model.RobotMode.AI, engine.mode)
    
    // Test touch interaction
    val phrase = engine.touchRobot()
    assertEquals(1, engine.touchCount)
    assert(phrase.isNotEmpty())
  }
}
