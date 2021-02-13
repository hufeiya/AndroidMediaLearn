package cn.neetu.projection_module.push

import android.hardware.display.DisplayManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat.*
import android.media.projection.MediaProjection
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.Exception
import java.nio.ByteBuffer
import kotlin.experimental.and

class ProjectionCodec(private val mediaProjection: MediaProjection) {
    var width = 1080
    var height = 1920
    var callback: ((ByteArray) -> Unit)? = null
    var mediaCodec: MediaCodec? = null
    private var job: Job? = null
    private var vpsBuffer: ByteArray? = null

    fun start() {
        cancelJob()
        val format = createVideoFormat(MIMETYPE_VIDEO_HEVC, width, height).apply {
            setInteger(KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(KEY_BIT_RATE, width * height)
            setInteger(KEY_FRAME_RATE, 20)
            setInteger(KEY_I_FRAME_INTERVAL, 1)
        }
        //H.265, if it's not supported by your device, you can change it to video/avc
        mediaCodec = MediaCodec.createEncoderByType("video/hevc")
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = mediaCodec?.createInputSurface()
        val display = mediaProjection.createVirtualDisplay(
            "ProjectionDisplay", width, height, 1,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null
        )

        job = GlobalScope.launch {
            mediaCodec?.start()
            val bufferInfo = MediaCodec.BufferInfo()
            while (isActive) {
                mediaCodec?.apply {
                    try {
                        val outputBufferId = dequeueOutputBuffer(bufferInfo, 1000)
                        if (outputBufferId >= 0) {
                            getOutputBuffer(outputBufferId)?.apply {
                                sendFrame(this, bufferInfo)
                            }
                            releaseOutputBuffer(outputBufferId, false)
                        }
                    } catch (t: Exception) {
                        t.printStackTrace()
                    }
                }

            }
        }
    }


    private fun sendFrame(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        var offset = 4
        if (buffer.get(2).toInt() == 1) {
            offset = 3
        }
        when ((buffer.get(offset) and 0x7E).toInt() shr 1) {
            NAL_VPS -> {
                vpsBuffer = ByteArray(bufferInfo.size)
                buffer.get(vpsBuffer)
            }
            NAL_I -> {
                vpsBuffer?.let {vps ->
                    val bytes = ByteArray(bufferInfo.size)
                    buffer.get(bytes)
                    val bytesWithVps = ByteArray(vps.size + bytes.size)
                    vps.copyInto(bytesWithVps)
                    bytes.copyInto(bytesWithVps, vps.size)
                    callback?.invoke(bytesWithVps)
                    Log.i(TAG, "Sending I frame with vps...")
                }
            }
            else -> {
                val bytes = ByteArray(bufferInfo.size)
                buffer.get(bytes)
                callback?.invoke(bytes)
            }
        }

    }

    fun stop() {
        cancelJob()
    }

    private fun cancelJob() {
        job?.cancel()
    }


    companion object {
        private const val NAL_I = 19
        private const val NAL_VPS = 32
        private const val TAG = "ProjectionCodec"
    }
}