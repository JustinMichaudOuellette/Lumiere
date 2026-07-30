package ca.justinmo.lumiere

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import ca.justinmo.lumiere.ui.AboutActivity
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class LumiereActivity : Activity(), SensorEventListener, SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var btnClose: ImageButton
    private lateinit var btnInfo: ImageButton
    private lateinit var txtMode: TextView
    private var surfaceHolder: SurfaceHolder? = null
    
    private var touchDownX = 0.0f
    private var touchDownY = 0.0f

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var sensorThread: HandlerThread? = null
    private var sensorHandler: Handler? = null

    private var renderThread: RenderThread? = null

    @Volatile private var sensorX = 0f
    @Volatile private var sensorY = 0f
    @Volatile private var sensorZ = 0f

    private var filterX = 0f
    private var filterY = 0f
    private var filterZ = 0f

    private var currentModeIndex = 0
    private var isFirstFrameDrawn = false

    enum class Mode {
        RGB, GRB, GBR, BGR, BRG, RBG;
        companion object {
            val COUNT = entries.size
            fun fromInt(value: Int) = entries[value % COUNT]
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.main_activity)

        surfaceView = findViewById(R.id.surface_view)
        btnClose = findViewById(R.id.btn_close)
        btnInfo = findViewById(R.id.btn_info)
        txtMode = findViewById(R.id.txt_mode)

        currentModeIndex = Random.nextInt(Mode.COUNT)

        surfaceView.holder.addCallback(this)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorThread = HandlerThread("SensorThread").apply { start() }
        sensorHandler = Handler(sensorThread!!.looper)

        btnClose.setOnClickListener { finish() }
        btnInfo.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java).apply {
                putExtra(AboutActivity.EXTRA_APP_NAME, getString(R.string.activity_name))
                putExtra(AboutActivity.EXTRA_APP_DESCRIPTION, getString(R.string.description))
            }
            startActivity(intent)
        }

        surfaceView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownX = event.x
                    touchDownY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (sqrt(
                            abs(touchDownX - event.x).pow(2.0f)
                                    + abs(touchDownY - event.y).pow(2.0f),
                        )
                        < (TOUCH_SLOP_DP * Resources.getSystem().displayMetrics.density)
                    ) {
                        currentModeIndex = (currentModeIndex + 1) % Mode.COUNT
                        animateModeChange()
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun animateModeChange() {
        txtMode.text = getString(R.string.mode_template, currentModeIndex + 1)
        txtMode.alpha = 0.75f
        txtMode.scaleX = 1f
        txtMode.scaleY = 1f
        
        txtMode.animate()
            .alpha(0f)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(400)
            .start()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceHolder = holder
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceHolder = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val a = SENSOR_FILTER_ALPHA
            filterX = (a * event.values[0]) + ((1.0f - a) * filterX)
            filterY = (a * event.values[1]) + ((1.0f - a) * filterY)
            filterZ = (a * event.values[2]) + ((1.0f - a) * filterZ)

            sensorX = sensorAbsColor(filterX)
            sensorY = sensorAbsColor(filterY)
            sensorZ = sensorAbsColor(filterZ)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun sensorAbsColor(sensorVal: Float): Float {
        return (abs(sensorVal).coerceAtMost(GRAVITY)) / GRAVITY
    }

    private inner class RenderThread : Thread() {
        @Volatile var isRunning = true

        override fun run() {
            while (isRunning) {
                val holder = surfaceHolder ?: continue

                val currentMode = currentModeIndex
                val sX = sensorX
                val sY = sensorY
                val sZ = sensorZ

                val r: Float
                val g: Float
                val b: Float

                when (Mode.fromInt(currentMode)) {
                    Mode.RGB -> { r = sX; g = sY; b = sZ }
                    Mode.RBG -> { r = sX; b = sY; g = sZ }
                    Mode.GBR -> { g = sX; b = sY; r = sZ }
                    Mode.GRB -> { g = sX; r = sY; b = sZ }
                    Mode.BGR -> { b = sX; g = sY; r = sZ }
                    Mode.BRG -> { b = sX; r = sY; g = sZ }
                }

                val color = Color.rgb(
                    (r * 255).toInt(),
                    (g * 255).toInt(),
                    (b * 255).toInt()
                )

                holder.lockCanvas()?.let { canvas ->
                    canvas.drawColor(color)
                    holder.unlockCanvasAndPost(canvas)
                    if (!isFirstFrameDrawn) {
                        isFirstFrameDrawn = true
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        renderThread?.isRunning = false
        renderThread = null
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorThread?.quitSafely()
    }

    @Suppress("DEPRECATION")
    override fun onResume() {
        isFirstFrameDrawn = false
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        window.attributes = window.attributes.apply { screenBrightness = 1f }
        super.onResume()
        renderThread = RenderThread().apply { start() }
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, sensorHandler)
        }
    }

    companion object {
        const val TOUCH_SLOP_DP = 8
        const val SENSOR_FILTER_ALPHA = 0.05f
        const val GRAVITY = 9.81f
    }
}
