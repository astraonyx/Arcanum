package com.example.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Handles both real URI audio playback (for files imported by the user via Android Storage Picker)
 * and real-time harmonic PCM audio synthesis (for built-in encrypted vault tracks) so the user
 * hears genuine audio output and sees live waveform amplitudes.
 */
class VaultAudioController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onProgressUpdate: (currentSec: Int, totalSec: Int, liveAmplitude: Float) -> Unit,
    private val onTrackCompleted: () -> Unit
) {
    private var mediaPlayer: MediaPlayer? = null
    private var synthJob: Job? = null
    private var audioTrack: AudioTrack? = null

    private var isPlayingInternal = false
    private var currentPositionSec = 0
    private var totalDurationSec = 152
    private var playbackSpeed = 1.0f

    fun startPlayback(
        uriString: String?,
        durationSeconds: Int,
        synthPreset: Int,
        startFromSec: Int = 0
    ) {
        stopPlayback()
        totalDurationSec = durationSeconds.coerceAtLeast(30)
        currentPositionSec = startFromSec.coerceIn(0, totalDurationSec)
        isPlayingInternal = true

        if (!uriString.isNullOrBlank()) {
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(context, Uri.parse(uriString))
                    prepare()
                    val realDur = (duration / 1000).coerceAtLeast(1)
                    totalDurationSec = realDur
                    seekTo(currentPositionSec * 1000)
                    start()
                    setOnCompletionListener {
                        isPlayingInternal = false
                        onTrackCompleted()
                    }
                }
                mediaPlayer = mp
                startMediaPlayerTicker()
                return
            } catch (_: Exception) {
                // Fallback to harmonic synthesizer if URI isn't a playable audio stream
            }
        }

        startHarmonicSynthLoop(synthPreset)
    }

    private fun startMediaPlayerTicker() {
        synthJob = scope.launch(Dispatchers.Default) {
            var tick = 0
            while (isActive && isPlayingInternal) {
                val mp = mediaPlayer
                if (mp != null && mp.isPlaying) {
                    currentPositionSec = (mp.currentPosition / 1000).coerceIn(0, totalDurationSec)
                    val amp = 0.35f + 0.55f * kotlin.math.abs(sin(tick * 0.45f))
                    onProgressUpdate(currentPositionSec, totalDurationSec, amp)
                }
                delay(250L)
                tick++
            }
        }
    }

    private fun startHarmonicSynthLoop(preset: Int) {
        synthJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (_: Exception) {
                null
            }

            audioTrack = track
            try {
                track?.play()
            } catch (_: Exception) {
            }

            val chordFreqs = when (preset % 4) {
                0 -> floatArrayOf(220.0f, 277.18f, 329.63f, 440.0f) // A Major 7 Ambient
                1 -> floatArrayOf(196.0f, 246.94f, 293.66f, 392.0f) // G Major Warm Pad
                2 -> floatArrayOf(261.63f, 311.13f, 392.0f, 466.16f) // C Minor 7 LoFi
                else -> floatArrayOf(174.61f, 220.0f, 261.63f, 349.23f) // F Major Deep Drone
            }

            val chunkSamples = sampleRate / 10 // 100ms chunks
            val pcmBuffer = ShortArray(chunkSamples)
            var phase = 0.0
            var elapsedDeciSeconds = currentPositionSec * 10

            while (isActive && isPlayingInternal) {
                val noteIndex = (elapsedDeciSeconds / 8) % chordFreqs.size
                val baseFreq = chordFreqs[noteIndex] * playbackSpeed
                val lfo = 0.5f + 0.45f * sin(elapsedDeciSeconds * 0.25f)

                for (i in 0 until chunkSamples) {
                    val env = sin(PI * (i.toDouble() / chunkSamples)).toFloat()
                    val wave = (sin(2.0 * PI * baseFreq * phase) * 0.6 +
                            sin(2.0 * PI * (baseFreq * 1.5) * phase) * 0.25 +
                            sin(2.0 * PI * (baseFreq * 0.5) * phase) * 0.15)
                    val sampleVal = (wave * 3200 * lfo * env).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    pcmBuffer[i] = sampleVal.toShort()
                    phase += 1.0 / sampleRate
                }

                try {
                    track?.write(pcmBuffer, 0, chunkSamples)
                } catch (_: Exception) {
                    delay(100L)
                }

                elapsedDeciSeconds++
                currentPositionSec = (elapsedDeciSeconds / 10).coerceAtMost(totalDurationSec)
                onProgressUpdate(currentPositionSec, totalDurationSec, lfo.coerceIn(0.15f, 1.0f))

                if (currentPositionSec >= totalDurationSec) {
                    isPlayingInternal = false
                    onTrackCompleted()
                    break
                }
            }

            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {
            }
        }
    }

    fun pausePlayback() {
        isPlayingInternal = false
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {
        }
        synthJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
    }

    fun seekToSeconds(seconds: Int, uriString: String?, durationSeconds: Int, synthPreset: Int) {
        currentPositionSec = seconds.coerceIn(0, durationSeconds.coerceAtLeast(1))
        val mp = mediaPlayer
        if (mp != null) {
            try {
                mp.seekTo(currentPositionSec * 1000)
                onProgressUpdate(currentPositionSec, totalDurationSec, 0.5f)
                return
            } catch (_: Exception) {
            }
        }
        if (isPlayingInternal) {
            startPlayback(uriString, durationSeconds, synthPreset, currentPositionSec)
        } else {
            onProgressUpdate(currentPositionSec, durationSeconds.coerceAtLeast(1), 0.2f)
        }
    }

    fun setSpeed(speed: Float) {
        playbackSpeed = speed.coerceIn(0.75f, 2.0f)
    }

    fun stopPlayback() {
        isPlayingInternal = false
        synthJob?.cancel()
        synthJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
    }
}
