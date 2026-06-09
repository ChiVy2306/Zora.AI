package com.yozora.aichat.data.remote

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.yozora.aichat.MainActivity
import com.yozora.aichat.R
import java.io.ByteArrayOutputStream
import kotlin.math.max

class ScreenShareForegroundService : Service() {
    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureThread: HandlerThread? = null
    private var captureHandler: Handler? = null
    private var lastFrameAt = 0L

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopSelf()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> startProjection(intent)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        projection?.unregisterCallback(projectionCallback)
        projection?.stop()
        projection = null
        captureThread?.quitSafely()
        captureThread = null
        captureHandler = null
        stoppedConsumer?.invoke()
        frameConsumer = null
        errorConsumer = null
        stoppedConsumer = null
        super.onDestroy()
    }

    private fun startProjection(intent: Intent) {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            startForeground(
                NOTIFICATION_ID,
                notification,
                serviceType
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
        val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_RESULT_DATA)
        }
        if (resultCode != Activity.RESULT_OK || resultData == null) {
            errorConsumer?.invoke("Screen sharing permission was not granted.")
            stopSelf()
            return
        }

        runCatching {
            val manager = getSystemService(MediaProjectionManager::class.java)
            projection = manager.getMediaProjection(resultCode, resultData).also {
                it.registerCallback(projectionCallback, Handler(mainLooper))
            }

            val metrics = resources.displayMetrics
            val scale = 720f / max(metrics.widthPixels, metrics.heightPixels).coerceAtLeast(720)
            val width = (metrics.widthPixels * scale).toInt().coerceAtLeast(2)
            val height = (metrics.heightPixels * scale).toInt().coerceAtLeast(2)
            captureThread = HandlerThread("zora-screen-share").also { it.start() }
            captureHandler = Handler(captureThread!!.looper)
            imageReader = ImageReader.newInstance(
                width,
                height,
                PixelFormat.RGBA_8888,
                2
            ).also { reader ->
                reader.setOnImageAvailableListener({ source ->
                    val now = System.currentTimeMillis()
                    val image = source.acquireLatestImage() ?: return@setOnImageAvailableListener
                    try {
                        if (now - lastFrameAt >= FRAME_INTERVAL_MS) {
                            lastFrameAt = now
                            imageToJpeg(image, width, height)?.let { frameConsumer?.invoke(it) }
                        }
                    } finally {
                        image.close()
                    }
                }, captureHandler)
            }
            virtualDisplay = projection?.createVirtualDisplay(
                "ZoraScreenShare",
                width,
                height,
                metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                captureHandler
            )
        }.onFailure { error ->
            errorConsumer?.invoke("Screen sharing failed: ${error.message ?: error.javaClass.simpleName}")
            stopSelf()
        }
    }

    private fun imageToJpeg(
        image: android.media.Image,
        width: Int,
        height: Int
    ): ByteArray? {
        val plane = image.planes.firstOrNull() ?: return null
        val rowPadding = plane.rowStride - plane.pixelStride * width
        val paddedWidth = width + rowPadding / plane.pixelStride
        val padded = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888)
        padded.copyPixelsFromBuffer(plane.buffer)
        val cropped = Bitmap.createBitmap(padded, 0, 0, width, height)
        if (cropped !== padded) padded.recycle()
        return ByteArrayOutputStream().use { output ->
            cropped.compress(Bitmap.CompressFormat.JPEG, 68, output)
            cropped.recycle()
            output.toByteArray()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Screen sharing",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_minimalist)
            .setContentTitle("Zora.AI screen sharing")
            .setContentText("Your screen is being shared with the active Gemini Live call.")
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val ACTION_START = "com.yozora.aichat.START_SCREEN_SHARE"
        private const val ACTION_STOP = "com.yozora.aichat.STOP_SCREEN_SHARE"
        private const val EXTRA_RESULT_CODE = "result_code"
        private const val EXTRA_RESULT_DATA = "result_data"
        private const val CHANNEL_ID = "zora_screen_share"
        private const val NOTIFICATION_ID = 4102
        private const val FRAME_INTERVAL_MS = 1_000L

        @Volatile
        private var frameConsumer: ((ByteArray) -> Unit)? = null

        @Volatile
        private var errorConsumer: ((String) -> Unit)? = null

        @Volatile
        private var stoppedConsumer: (() -> Unit)? = null

        fun start(
            context: Context,
            resultCode: Int,
            resultData: Intent,
            onFrame: (ByteArray) -> Unit,
            onError: (String) -> Unit,
            onStopped: () -> Unit
        ) {
            frameConsumer = onFrame
            errorConsumer = onError
            stoppedConsumer = onStopped
            val intent = Intent(context, ScreenShareForegroundService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_RESULT_CODE, resultCode)
                .putExtra(EXTRA_RESULT_DATA, resultData)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, ScreenShareForegroundService::class.java)
                    .setAction(ACTION_STOP)
            )
        }
    }
}
