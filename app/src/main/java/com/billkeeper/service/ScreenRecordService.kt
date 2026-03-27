package com.billkeeper.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.view.Surface
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * 录屏服务 - 录制支付过程，结束后提取关键帧
 */
class ScreenRecordService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputFile: File? = null

    companion object {
        const val CHANNEL_ID = "screen_record_channel"
        const val NOTIFICATION_ID = 1002

        private const val VIDEO_WIDTH = 720
        private const val VIDEO_HEIGHT = 1280
        private const val VIDEO_BITRATE = 2_000_000
        private const val VIDEO_FPS = 30

        fun start(context: Context) {
            val intent = Intent(context, ScreenRecordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreenRecordService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", -1) ?: -1
        val data: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("data", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("data")
        }

        if (resultCode != -1 && data != null) {
            startRecording(resultCode, data)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        scope.cancel()
    }

    @SuppressLint("WrongConstant")
    private fun startRecording(resultCode: Int, data: Intent) {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        // 创建输出文件
        val outputDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        outputDir?.mkdirs()
        outputFile = File(outputDir, "record_${System.currentTimeMillis()}.mp4")

        setupMediaRecorder()
        startForeground(NOTIFICATION_ID, createRecordingNotification())
        isRecording = true

        // 录制120秒后自动停止
        scope.launch {
            delay(120_000)
            stopRecording()
        }

        // 通知UI层开始录屏
        sendRecordingStateBroadcast(true)
    }

    @Suppress("DEPRECATION")
    private fun setupMediaRecorder() {
        val file = outputFile ?: return

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.absolutePath)
            setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT)
            setVideoFrameRate(VIDEO_FPS)
            setVideoEncodingBitRate(VIDEO_BITRATE)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            val surface = createVirtualDisplay()
            setSurface(surface)

            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createVirtualDisplay(): Surface {
        val density = resources.displayMetrics.densityDpi
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenRecord",
            VIDEO_WIDTH,
            VIDEO_HEIGHT,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            null,
            null,
            null
        )
        return virtualDisplay!!.surface
    }

    private fun stopRecording() {
        if (!isRecording) return
        isRecording = false

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}

        try {
            virtualDisplay?.release()
        } catch (_: Exception) {}

        try {
            mediaProjection?.stop()
        } catch (_: Exception) {}

        // 通知UI层录屏结束，文件路径
        outputFile?.let { file ->
            sendRecordingCompleteBroadcast(file.absolutePath)
        }

        stopForeground(true)
        stopSelf()
        sendRecordingStateBroadcast(false)
    }

    private fun sendRecordingStateBroadcast(isRecording: Boolean) {
        val intent = Intent(ACTION_RECORDING_STATE).apply {
            putExtra("is_recording", isRecording)
            `package` = packageName
        }
        sendBroadcast(intent)
    }

    private fun sendRecordingCompleteBroadcast(filePath: String) {
        val intent = Intent(ACTION_RECORDING_COMPLETE).apply {
            putExtra("file_path", filePath)
            `package` = packageName
        }
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "录屏记账",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "录屏记账服务"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createRecordingNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("正在录屏...")
            .setContentText("请在支付App中完成支付操作")
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val ACTION_RECORDING_STATE = "com.billkeeper.ACTION_RECORDING_STATE"
        const val ACTION_RECORDING_COMPLETE = "com.billkeeper.ACTION_RECORDING_COMPLETE"
    }
}
