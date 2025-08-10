package com.example.appusageoverlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

class OverlayManager(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    fun showOverlay(appName: String) {
        try {
            if (overlayView != null) return
            
            val inflater = LayoutInflater.from(context)
            overlayView = inflater.inflate(R.layout.overlay_layout, null)

            // Safely find and set text
            overlayView?.findViewById<TextView>(R.id.overlayText)?.let { textView ->
                textView.text = "Time limit exceeded for $appName"
            }
            
            // Safely find and set click listeners
            overlayView?.findViewById<Button>(R.id.btnClose)?.setOnClickListener { hideOverlay() }
            overlayView?.findViewById<Button>(R.id.btnSnooze)?.setOnClickListener { hideOverlay() }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            // Handle overlay creation error
        }
    }

    fun hideOverlay() {
        try {
            if (overlayView != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
            }
        } catch (e: Exception) {
            // Handle overlay removal error
        }
    }
}
