package com.example.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceSpeaker(context: Context) : TextToSpeech.OnInitListener {
  private var tts: TextToSpeech? = null
  private var isInitialized = false
  var isEnabled = true
  var volume = 0.85f

  init {
    try {
      tts = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      isInitialized = false
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val result = tts?.setLanguage(Locale.US)
      if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.setPitch(1.35f) // Robotic/friendly youthful companion pitch
        tts?.setSpeechRate(1.05f)
        isInitialized = true
      }
    }
  }

  fun speak(phrase: String) {
    if (!isEnabled) return
    if (isInitialized && tts != null) {
      try {
        tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "AI_ROBOT_${System.currentTimeMillis()}")
      } catch (e: Exception) {
        // Fallback gracefully without crash
      }
    }
  }

  fun shutdown() {
    try {
      tts?.stop()
      tts?.shutdown()
    } catch (e: Exception) {
      // Ignored
    }
  }
}
