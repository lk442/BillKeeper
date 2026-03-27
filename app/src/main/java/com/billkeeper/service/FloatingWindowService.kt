package com.billkeeper.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.nio.ByteBuffer

/**
 * 悬浮窗服务 - 在支付页面悬浮截屏按钮
 */
class FloatingWindowService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    // 拖动相关
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    companion object {
        const val CHANNEL_ID = "floating_window_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, FloatingWindowService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingWindowService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        showFloatingWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
        scope.cancel()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingWindow() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 200
        }
        layoutParams = params

        floatingView = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
            val size = (48 * resources.displayMetrics.density).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size)

            // 使用编程方式创建简单按钮
            val btn = Button(this@FloatingWindowService).apply {
                text = "截"
                textSize = 16f
                setTextColor(android.graphics.Color.WHITE)
                setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                setPadding(0, 0, 0, 0)
                setOnClickListener {
                    captureScreen()
                }
            }
            addView(btn)

            setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View, event: android.view.MotionEvent): Boolean {
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            isDragging = false
                            return true
                        }
                        android.view.MotionEvent.ACTION_MOVE -> {
                            val dx = (event.rawX - initialTouchX).toInt()
                            val dy = (event.rawY - initialTouchY).toInt()
                            if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                                isDragging = true
                                params.x = initialX - dx
                                params.y = initialY + dy
                                windowManager.updateViewLayout(floatingView, params)
                            }
                            return true
                        }
                        android.view.MotionEvent.ACTION_UP -> {
                            if (!isDragging) {
                                captureScreen()
                            }
                            return true
                        }
                    }
                    return false
                }
            })
        }

        windowManager.addView(floatingView, params)
    }

    private fun captureScreen() {
        // 截屏操作需要MediaProjection，通过Activity请求权限后启动
        // 这里发送广播让MainActivity处理
        val intent = Intent(ACTION_CAPTURE_REQUEST)
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "悬浮窗记账",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "悬浮窗截屏记账服务"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("记账本 - 悬浮窗服务")
            .setContentText("悬浮窗已开启，点击截屏记账")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val ACTION_CAPTURE_REQUEST = "com.billkeeper.ACTION_CAPTURE_REQUEST"
        const val ACTION_SCREEN_CAPTURED = "com.billkeeper.ACTION_SCREEN_CAPTURED"
        const val EXTRA_IMAGE_DATA = "extra_image_data"
    }
}
