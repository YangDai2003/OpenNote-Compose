package com.yangdai.opennote.ui.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.yangdai.opennote.R
import java.util.Locale

class TextToSpeechHelper(
    context: Context
) {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            when (status) {
                TextToSpeech.SUCCESS -> {
                    tts?.let {
                        val result = it.setLanguage(Locale.getDefault())
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "The Language specified is not supported!")
                            tts = null
                            Toast.makeText(
                                context,
                                context.getString(R.string.failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                else -> {
                    Log.e("TTS", "Initialization Failed!")
                    tts = null
                    Toast.makeText(context, context.getString(R.string.failed), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun speakOut(text: String) {
        tts?.let {
            // if text is too long, do not speak
            if (text.length > 4000) {
                return
            }
            // if it is speaking, stop it and return
            if (it.isSpeaking) {
                it.stop()
                return
            }
            it.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    fun release() {
        tts?.let {
            it.stop()
            it.shutdown()
        }
    }
}