package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.data.GameProfileEntity
import com.example.engine.SensitivityEngine
import java.util.Locale

class FloatingOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var isShowing = false

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    fun show(profile: GameProfileEntity) {
        if (isShowing) {
            updateValues(profile)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) {
            return
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 160
        }

        // Create a sleek, lightweight native HUD to minimize CPU/RAM overhead
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            setBackgroundColor(Color.parseColor("#E60D121D"))
            elevation = 12f
        }

        val titleView = TextView(context).apply {
            text = "⚡ GAME TOOLS"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 12f
            paint.isFakeBoldText = true
        }

        val infoView = TextView(context).apply {
            tag = "info_text"
            text = "X: ${String.format(Locale.US, "%.2f", profile.sensX)}x | Y: ${String.format(Locale.US, "%.2f", profile.sensY)}x"
            setTextColor(Color.WHITE)
            textSize = 11f
        }

        val telemetryView = TextView(context).apply {
            tag = "telemetry_text"
            text = "Touch to test delta"
            setTextColor(Color.parseColor("#A0AAB8"))
            textSize = 10f
        }

        container.addView(titleView)
        container.addView(infoView)
        container.addView(telemetryView)

        // Drag & interactive touch delta calculation
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var lastTouchX = 0f
        var lastTouchY = 0f

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY

                    // Real-time delta calculation
                    val rawStepDx = event.rawX - lastTouchX
                    val rawStepDy = event.rawY - lastTouchY
                    val (txDx, txDy) = SensitivityEngine.transformDelta(
                        rawStepDx,
                        rawStepDy,
                        profile.sensX,
                        profile.sensY
                    )

                    lastTouchX = event.rawX
                    lastTouchY = event.rawY

                    telemetryView.text = "ΔRaw: [${rawStepDx.toInt()}, ${rawStepDy.toInt()}] → ΔTx: [${txDx.toInt()}, ${txDy.toInt()}]"

                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    try {
                        windowManager.updateViewLayout(container, params)
                    } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(container, params)
            overlayView = container
            isShowing = true
        } catch (_: Exception) {
            isShowing = false
        }
    }

    fun updateValues(profile: GameProfileEntity) {
        overlayView?.let { view ->
            val infoView = view.findViewWithTag<TextView>("info_text")
            infoView?.text = "X: ${String.format(Locale.US, "%.2f", profile.sensX)}x | Y: ${String.format(Locale.US, "%.2f", profile.sensY)}x"
        }
    }

    fun hide() {
        if (!isShowing) return
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {}
        }
        overlayView = null
        isShowing = false
    }
}
