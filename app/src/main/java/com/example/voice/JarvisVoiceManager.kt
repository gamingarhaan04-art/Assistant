package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class JarvisVoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

class JarvisVoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val tag = "JarvisVoiceManager"

    private val _voiceState = MutableStateFlow(JarvisVoiceState.IDLE)
    val voiceState: StateFlow<JarvisVoiceState> = _voiceState.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    var currentLanguage: String = "en-US"
    var speechRate: Float = 1.0f
    var speechPitch: Float = 0.95f

    var onSpeechRecognized: ((String) -> Unit)? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            applyTtsSettings()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = JarvisVoiceState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    _voiceState.value = JarvisVoiceState.IDLE
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _voiceState.value = JarvisVoiceState.IDLE
                }
            })
        } else {
            Log.e(tag, "TTS init failed with status: $status")
        }
    }

    private fun applyTtsSettings() {
        if (!isTtsReady || tts == null) return
        try {
            val locale = when (currentLanguage) {
                "hi-IN" -> Locale.forLanguageTag("hi-IN")
                else -> Locale.US
            }
            tts?.language = locale
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(speechPitch)
        } catch (e: Exception) {
            Log.w(tag, "Failed to apply TTS settings", e)
        }
    }

    fun startListening() {
        stopSpeaking()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = JarvisVoiceState.ERROR
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = JarvisVoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = JarvisVoiceState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize dB roughly to 0f - 1f
                        val norm = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioAmplitude.value = norm
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = JarvisVoiceState.THINKING
                    }

                    override fun onError(error: Int) {
                        Log.w(tag, "SpeechRecognizer error: $error")
                        _voiceState.value = JarvisVoiceState.IDLE
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _spokenText.value = text
                        _voiceState.value = JarvisVoiceState.IDLE
                        if (text.isNotEmpty()) {
                            onSpeechRecognized?.invoke(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            _spokenText.value = it
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS is listening...")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
            _voiceState.value = JarvisVoiceState.LISTENING
        } catch (e: Exception) {
            Log.e(tag, "Error starting voice recognition", e)
            _voiceState.value = JarvisVoiceState.ERROR
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.w(tag, "Error stopping recognizer", e)
        }
        if (_voiceState.value == JarvisVoiceState.LISTENING) {
            _voiceState.value = JarvisVoiceState.IDLE
        }
    }

    fun speak(text: String) {
        if (!isTtsReady || tts == null) return
        try {
            stopListening()
            applyTtsSettings()
            _voiceState.value = JarvisVoiceState.SPEAKING
            // Clean markdown syntax for natural speech
            val cleanSpeech = text
                .replace(Regex("```[\\s\\S]*?```"), "Code block omitted.")
                .replace(Regex("[*#_`>]"), "")
                .trim()

            val utteranceId = "jarvis_speech_" + System.currentTimeMillis()
            tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(tag, "TTS speak failed", e)
            _voiceState.value = JarvisVoiceState.IDLE
        }
    }

    fun stopSpeaking() {
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.w(tag, "Error stopping TTS", e)
        }
        if (_voiceState.value == JarvisVoiceState.SPEAKING) {
            _voiceState.value = JarvisVoiceState.IDLE
        }
    }

    fun setThinkingState() {
        _voiceState.value = JarvisVoiceState.THINKING
    }

    fun setIdleState() {
        _voiceState.value = JarvisVoiceState.IDLE
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.w(tag, "Destroy error", e)
        }
    }
}
