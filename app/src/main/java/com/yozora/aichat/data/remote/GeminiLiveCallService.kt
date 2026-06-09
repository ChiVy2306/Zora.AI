package com.yozora.aichat.data.remote

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.sqrt

data class GeminiLiveCallConfig(
    val apiKey: String,
    val modelId: String,
    val systemInstruction: String,
    val voiceName: String,
    val priorConversationContext: String?
)

class GeminiLiveCallService {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val running = AtomicBoolean(false)
    private val muted = AtomicBoolean(false)
    private val setupComplete = AtomicBoolean(false)
    private val captureStarted = AtomicBoolean(false)
    private var webSocket: WebSocket? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var automaticGainControl: AutomaticGainControl? = null
    private var captureExecutor: ExecutorService? = null
    private val timeoutExecutor = Executors.newSingleThreadScheduledExecutor()
    private var setupTimeoutTask: ScheduledFuture<*>? = null
    private var onStatus: ((String) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null
    private var onUserTranscript: ((String) -> Unit)? = null
    private var onModelTranscriptDelta: ((String) -> Unit)? = null
    private var onModelTurnComplete: (() -> Unit)? = null
    private var priorConversationContext: String? = null
    private var ambientRms = INITIAL_AMBIENT_RMS
    private var speechHangoverChunks = 0

    fun start(
        config: GeminiLiveCallConfig,
        onStatus: (String) -> Unit,
        onError: (String) -> Unit,
        onUserTranscript: (String) -> Unit,
        onModelTranscriptDelta: (String) -> Unit,
        onModelTurnComplete: () -> Unit
    ) {
        stop()
        this.onStatus = onStatus
        this.onError = onError
        this.onUserTranscript = onUserTranscript
        this.onModelTranscriptDelta = onModelTranscriptDelta
        this.onModelTurnComplete = onModelTurnComplete
        this.priorConversationContext = config.priorConversationContext
        running.set(true)
        muted.set(false)
        setupComplete.set(false)
        captureStarted.set(false)
        onStatus("Connecting...")

        val encodedKey = URLEncoder.encode(config.apiKey, StandardCharsets.UTF_8.name())
        val request = Request.Builder()
            .url("$LIVE_WEBSOCKET_URL?key=$encodedKey")
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (this@GeminiLiveCallService.webSocket !== webSocket) return
                    onStatus("Setting up...")
                    webSocket.send(setupMessage(config).toString())
                    setupTimeoutTask?.cancel(false)
                    setupTimeoutTask = timeoutExecutor.schedule(
                        {
                            if (
                                this@GeminiLiveCallService.webSocket === webSocket &&
                                running.get() &&
                                !setupComplete.get()
                            ) {
                                onError("Gemini Live setup timed out after 12 seconds.")
                                webSocket.cancel()
                                running.set(false)
                                releaseAudio()
                            }
                        },
                        12,
                        TimeUnit.SECONDS
                    )
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (this@GeminiLiveCallService.webSocket !== webSocket) return
                    handleServerMessage(text)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    if (this@GeminiLiveCallService.webSocket !== webSocket) return
                    handleServerMessage(bytes.utf8())
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (this@GeminiLiveCallService.webSocket !== webSocket) return
                    running.set(false)
                    onError(
                        "Gemini Live failed: ${
                            t.message ?: response?.let { "${it.code} ${it.message}" } ?: "unknown error"
                        }"
                    )
                    releaseAudio()
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    if (this@GeminiLiveCallService.webSocket !== webSocket) return
                    if (running.get()) {
                        val closeReason = reason.ifBlank { "no reason provided" }
                        if (code == 1000 && setupComplete.get()) {
                            onStatus("Call ended")
                        } else {
                            onError("Gemini Live closed ($code): $closeReason")
                        }
                    }
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (this@GeminiLiveCallService.webSocket !== webSocket) return
                    running.set(false)
                    if (code == 1000) {
                        onStatus("Call ended")
                    }
                    releaseAudio()
                }
            }
        )
    }

    fun setMuted(value: Boolean) {
        muted.set(value)
        if (value) {
            sendAudioStreamEnd()
            onStatus?.invoke("Muted")
        } else if (running.get()) {
            onStatus?.invoke("Listening...")
        }
    }

    fun sendVideoFrame(jpegBytes: ByteArray) {
        if (!running.get() || !setupComplete.get() || jpegBytes.isEmpty()) return
        val encoded = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        val message = JSONObject()
            .put(
                "realtimeInput",
                JSONObject().put(
                    "video",
                    JSONObject()
                        .put("mimeType", "image/jpeg")
                        .put("data", encoded)
                )
            )
        webSocket?.send(message.toString())
    }

    fun stop() {
        setupTimeoutTask?.cancel(false)
        setupTimeoutTask = null
        if (!running.getAndSet(false)) {
            releaseAudio()
            webSocket = null
            return
        }
        sendAudioStreamEnd()
        webSocket?.close(1000, "Call ended")
        webSocket = null
        releaseAudio()
    }

    private fun handleServerMessage(raw: String) {
        val json = runCatching { JSONObject(raw) }.getOrElse {
            onError?.invoke("Gemini Live returned non-JSON data.")
            return
        }

        json.optJSONObject("error")?.let { error ->
            val message = error.optString("message").ifBlank { "Gemini Live server error." }
            onError?.invoke(message)
            stop()
            return
        }

        if (json.has("setupComplete")) {
            setupComplete.set(true)
            setupTimeoutTask?.cancel(false)
            setupTimeoutTask = null
            onStatus?.invoke("Connected - starting microphone...")
            startAudioCapture()
            sendPriorConversationContext()
            return
        }

        val serverContent = json.optJSONObject("serverContent") ?: return
        if (serverContent.optBoolean("interrupted")) {
            audioTrack?.flush()
        }

        serverContent.optJSONObject("inputTranscription")
            ?.optString("text")
            ?.takeIf { it.isNotBlank() }
            ?.let { chunk ->
                onUserTranscript?.invoke(chunk.trim())
            }

        serverContent.optJSONObject("outputTranscription")
            ?.optString("text")
            ?.takeIf { it.isNotBlank() }
            ?.let { chunk ->
                onModelTranscriptDelta?.invoke(chunk)
            }

        val parts = serverContent
            .optJSONObject("modelTurn")
            ?.optJSONArray("parts")
        if (parts != null) {
            writeAudioParts(parts)
        }

        if (serverContent.optBoolean("turnComplete") && running.get()) {
            onModelTurnComplete?.invoke()
            onStatus?.invoke(if (muted.get()) "Muted" else "Listening...")
        }
    }

    private fun writeAudioParts(parts: JSONArray) {
        for (index in 0 until parts.length()) {
            val part = parts.optJSONObject(index) ?: continue
            part.optString("text").takeIf { it.isNotBlank() }?.let { text ->
                onModelTranscriptDelta?.invoke(text)
            }

            val inlineData = part.optJSONObject("inlineData") ?: continue
            val encoded = inlineData.optString("data")
            if (encoded.isNotBlank()) {
                val audioBytes = runCatching { Base64.decode(encoded, Base64.DEFAULT) }.getOrNull()
                    ?: continue
                val track = runCatching { ensurePlayback() }.getOrElse { error ->
                    failAudio("Audio output could not start: ${error.message ?: error.javaClass.simpleName}")
                    return
                }
                onStatus?.invoke("Speaking...")
                val written = track.write(audioBytes, 0, audioBytes.size)
                if (written < 0) {
                    failAudio("Audio output failed with code $written.")
                    return
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAudioCapture() {
        if (captureExecutor != null || !running.get() || !setupComplete.get()) return

        val minBuffer = AudioRecord.getMinBufferSize(
            INPUT_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) {
            failAudio("Microphone does not support 16 kHz PCM input (code $minBuffer).")
            return
        }

        val recorder = runCatching {
            AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                INPUT_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuffer.coerceAtLeast(INPUT_CHUNK_BYTES)
            ).also { candidate ->
                check(candidate.state == AudioRecord.STATE_INITIALIZED) {
                    "microphone initialization failed"
                }
                candidate.startRecording()
                check(candidate.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    "microphone did not enter recording state"
                }
            }
        }.getOrElse { error ->
            failAudio("Microphone could not start: ${error.message ?: error.javaClass.simpleName}")
            return
        }

        audioRecord = recorder
        configureAudioEffects(recorder.audioSessionId)
        ambientRms = INITIAL_AMBIENT_RMS
        speechHangoverChunks = 0
        captureStarted.set(true)
        onStatus?.invoke(if (muted.get()) "Muted" else "Listening...")

        captureExecutor = Executors.newSingleThreadExecutor()
        captureExecutor?.execute {
            val buffer = ByteArray(INPUT_CHUNK_BYTES)
            while (running.get()) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0 && !muted.get()) {
                    sendAudioChunk(applyNoiseGate(buffer, read))
                } else if (read == 0 || muted.get()) {
                    Thread.sleep(24L)
                } else if (running.get()) {
                    failAudio("Microphone read failed with code $read.")
                    break
                }
            }
        }
    }

    private fun ensurePlayback(): AudioTrack {
        audioTrack?.let { return it }
        val platformBuffer = AudioTrack.getMinBufferSize(
            OUTPUT_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        check(platformBuffer > 0) {
            "24 kHz PCM output is unsupported (code $platformBuffer)"
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(OUTPUT_SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(platformBuffer.coerceAtLeast(OUTPUT_BUFFER_BYTES))
            .build()
        check(track.state == AudioTrack.STATE_INITIALIZED) {
            "audio track initialization failed"
        }
        track.play()
        check(track.playState == AudioTrack.PLAYSTATE_PLAYING) {
            "audio track did not enter playback state"
        }
        audioTrack = track
        return track
    }

    private fun configureAudioEffects(audioSessionId: Int) {
        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = runCatching {
                NoiseSuppressor.create(audioSessionId)?.apply { enabled = true }
            }.getOrNull()
        }
        if (AcousticEchoCanceler.isAvailable()) {
            echoCanceler = runCatching {
                AcousticEchoCanceler.create(audioSessionId)?.apply { enabled = true }
            }.getOrNull()
        }
        if (AutomaticGainControl.isAvailable()) {
            automaticGainControl = runCatching {
                AutomaticGainControl.create(audioSessionId)?.apply { enabled = false }
            }.getOrNull()
        }
    }

    private fun applyNoiseGate(buffer: ByteArray, length: Int): ByteArray {
        val copy = buffer.copyOf(length)
        val rms = pcmRms(copy)
        val gateThreshold = max(MIN_GATE_RMS, ambientRms * GATE_MULTIPLIER)
        val speechDetected = rms >= gateThreshold
        val keepGateOpen: Boolean

        if (speechDetected) {
            speechHangoverChunks = SPEECH_HANGOVER_CHUNKS
            keepGateOpen = true
        } else {
            if (rms < gateThreshold * AMBIENT_UPDATE_CEILING) {
                ambientRms = (ambientRms * AMBIENT_HISTORY_WEIGHT) +
                    (rms * (1.0 - AMBIENT_HISTORY_WEIGHT))
            }
            keepGateOpen = speechHangoverChunks > 0
            if (speechHangoverChunks > 0) {
                speechHangoverChunks--
            }
        }

        if (!keepGateOpen) {
            copy.fill(0)
        }
        return copy
    }

    private fun pcmRms(buffer: ByteArray): Double {
        val sampleCount = buffer.size / 2
        if (sampleCount == 0) return 0.0

        var sumSquares = 0.0
        var index = 0
        while (index + 1 < buffer.size) {
            val low = buffer[index].toInt() and 0xFF
            val high = buffer[index + 1].toInt()
            val sample = (high shl 8) or low
            sumSquares += sample.toDouble() * sample.toDouble()
            index += 2
        }
        return sqrt(sumSquares / sampleCount)
    }

    private fun sendAudioChunk(buffer: ByteArray) {
        val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
        val message = JSONObject()
            .put(
                "realtimeInput",
                JSONObject().put(
                    "audio",
                    JSONObject()
                        .put("mimeType", "audio/pcm;rate=$INPUT_SAMPLE_RATE")
                        .put("data", encoded)
                )
            )
        webSocket?.send(message.toString())
    }

    private fun sendAudioStreamEnd() {
        if (!setupComplete.get()) return
        val message = JSONObject()
            .put("realtimeInput", JSONObject().put("audioStreamEnd", true))
        webSocket?.send(message.toString())
    }

    private fun sendPriorConversationContext() {
        val context = priorConversationContext?.trim()?.takeIf { it.isNotBlank() } ?: return
        val message = JSONObject()
            .put(
                "clientContent",
                JSONObject()
                    .put(
                        "turns",
                        JSONArray().put(
                            JSONObject()
                                .put("role", "user")
                                .put(
                                    "parts",
                                    JSONArray().put(JSONObject().put("text", context))
                                )
                        )
                    )
                    .put("turnComplete", true)
            )
        webSocket?.send(message.toString())
    }

    private fun failAudio(message: String) {
        if (!running.getAndSet(false)) return
        onError?.invoke(message)
        webSocket?.close(1000, "Audio device unavailable")
        webSocket = null
        releaseAudio()
    }

    private fun releaseAudio() {
        captureExecutor?.shutdownNow()
        captureExecutor = null
        runCatching { noiseSuppressor?.release() }
        noiseSuppressor = null
        runCatching { echoCanceler?.release() }
        echoCanceler = null
        runCatching { automaticGainControl?.release() }
        automaticGainControl = null
        runCatching { audioRecord?.stop() }
        runCatching { audioRecord?.release() }
        audioRecord = null
        captureStarted.set(false)
        ambientRms = INITIAL_AMBIENT_RMS
        speechHangoverChunks = 0
        runCatching { audioTrack?.stop() }
        runCatching { audioTrack?.release() }
        audioTrack = null
        priorConversationContext = null
    }

    private fun setupMessage(config: GeminiLiveCallConfig): JSONObject {
        val generationConfig = JSONObject()
            .put("responseModalities", org.json.JSONArray().put("AUDIO"))
            .put(
                "speechConfig",
                JSONObject().put(
                    "voiceConfig",
                    JSONObject().put(
                        "prebuiltVoiceConfig",
                        JSONObject().put("voice_name", config.voiceName)
                    )
                )
            )

        val setup = JSONObject()
            .put("model", "models/${config.modelId}")
            .put("generationConfig", generationConfig)
            .put("inputAudioTranscription", JSONObject())
            .put("outputAudioTranscription", JSONObject())
            .put(
                "realtimeInputConfig",
                JSONObject().put(
                    "turnCoverage",
                    "TURN_INCLUDES_AUDIO_ACTIVITY_AND_ALL_VIDEO"
                )
            )
        if (!config.priorConversationContext.isNullOrBlank()) {
            setup.put(
                "historyConfig",
                JSONObject().put("initialHistoryInClientContent", true)
            )
        }
        if (config.systemInstruction.isNotBlank()) {
            setup.put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    org.json.JSONArray().put(JSONObject().put("text", config.systemInstruction))
                )
            )
        }

        return JSONObject().put(
            "setup",
            setup
        )
    }

    companion object {
        const val DEFAULT_MODEL_ID = "gemini-3.1-flash-live-preview"
        private const val LIVE_WEBSOCKET_URL =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"
        private const val INPUT_SAMPLE_RATE = 16000
        private const val OUTPUT_SAMPLE_RATE = 24000
        private const val INPUT_CHUNK_BYTES = 4096
        private const val OUTPUT_BUFFER_BYTES = 8192
        private const val INITIAL_AMBIENT_RMS = 250.0
        private const val MIN_GATE_RMS = 700.0
        private const val GATE_MULTIPLIER = 2.35
        private const val AMBIENT_HISTORY_WEIGHT = 0.94
        private const val AMBIENT_UPDATE_CEILING = 1.15
        private const val SPEECH_HANGOVER_CHUNKS = 3
    }
}
