package com.example.feature.sidebar
import android.view.ViewGroup
import android.view.View

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.utils.SidebarPage
import com.example.util.AppLogger
import kotlin.math.max

import com.example.feature.sidebar.AppsPageView
import com.example.feature.sidebar.SidebarAppsManager
import com.example.feature.sidebar.HybridGridPageView
import com.example.feature.sidebar.WidgetsGridPageView
import com.example.feature.sidebar.AppTrackerPageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

@SuppressLint("ViewConstructor")
class SidebarView(
    context: Context,
    private val prefs: SharedPreferences,
    private val windowManager: WindowManager,
    val physicalHandleId: String,
    val containerId: String,
    private val pageConfigs: List<SidebarPage>,
    private val defaultPageIndex: Int,
    private val onClose: () -> Unit
) : FrameLayout(context) {

    private val layoutParams: WindowManager.LayoutParams
    private val viewPager: ViewPager2
    private var isAttached = false
    private val viewScope = CoroutineScope(Dispatchers.Main + Job())
    private val appsManagers = mutableMapOf<String, SidebarAppsManager>()
    
    init {
        AppLogger.d("SidebarView", "Init sidebar for containerId: $containerId")
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val density = context.resources.displayMetrics.density
        val widthDp = prefs.getInt("handle_${containerId}_sidebar_width", prefs.getInt("sidebar_width", 216))
        val widthPx = (widthDp * density).toInt()
        
        val wrapContent = prefs.getBoolean("handle_${containerId}_sidebar_wrap_content", prefs.getBoolean("sidebar_wrap_content", true))
        val heightPx = if (wrapContent) WindowManager.LayoutParams.WRAP_CONTENT else (prefs.getInt("handle_${containerId}_sidebar_height", prefs.getInt("sidebar_height", 360)) * density).toInt()
        
        val legacyEdge = if (prefs.getBoolean("sidebar_position_left", false)) "left" else "right"
        val isRight = prefs.getString("handle_${physicalHandleId}_edge", if (physicalHandleId == "sidebar") legacyEdge else "right") == "right"
        val gravityEdge = if (isRight) Gravity.END else Gravity.START
        
        layoutParams = WindowManager.LayoutParams(
            widthPx,
            heightPx,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = gravityEdge or Gravity.BOTTOM
            x = 0
            y = 0 // Wait, shouldn't y match handle position? We can just use 0 for now
        }
        
        isFocusableInTouchMode = true
        setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                onClose()
                true
            } else {
                false
            }
        }
        
        val opacity = prefs.getFloat("handle_${containerId}_sidebar_transparency", prefs.getFloat("sidebar_transparency", 0.9f))
        val alphaInt = (opacity * 255).toInt().coerceIn(0, 255)
        
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            val colorHex = prefs.getString("handle_${containerId}_sidebar_color", prefs.getString("sidebar_color", "#000000")) ?: "#000000"
            val baseColor = try { Color.parseColor(colorHex) } catch(e:Exception){ Color.BLACK }
            val r = Color.red(baseColor)
            val g = Color.green(baseColor)
            val b = Color.blue(baseColor)
            setColor(Color.argb(alphaInt, r, g, b))
            
            cornerRadii = if (isRight) {
                floatArrayOf(32f, 32f, 0f, 0f, 0f, 0f, 32f, 32f)
            } else {
                floatArrayOf(0f, 0f, 32f, 32f, 32f, 32f, 0f, 0f)
            }
        }
        background = drawable
        
        val marginDp = 8f
        val marginPx = (marginDp * density).toInt()
        setPadding(0, marginPx, 0, marginPx)
        
        viewPager = ViewPager2(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            offscreenPageLimit = if (containerId == "sidebar") 1 else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT // Semi-loaded for Handle 1
        }
        
        val isLooping = pageConfigs.size > 2
        val startingIndex = if (isLooping) {
            val half = Int.MAX_VALUE / 2
            half - (half % pageConfigs.size) + max(0, defaultPageIndex)
        } else {
            max(0, defaultPageIndex)
        }
        
        viewPager.adapter = object : RecyclerView.Adapter<SidebarPageViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SidebarPageViewHolder {
                val frame = FrameLayout(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
                return SidebarPageViewHolder(frame)
            }

            override fun onBindViewHolder(holder: SidebarPageViewHolder, position: Int) {
                if (pageConfigs.isEmpty()) return
                val actualPosition = if (isLooping) position % pageConfigs.size else position
                val config = pageConfigs[actualPosition]
                
                holder.bind(config)
            }
            
            override fun getItemCount(): Int = if (isLooping) Int.MAX_VALUE else pageConfigs.size
        }
        
        viewPager.setCurrentItem(startingIndex, false)
        addView(viewPager)
    }
    
    fun attach() {
        if (!isAttached) {
            windowManager.addView(this, layoutParams)
            isAttached = true
        }
    }
    
    fun detach() {
        if (isAttached) {
            windowManager.removeView(this)
            isAttached = false
            viewScope.cancel()
        }
    }
    
    inner class SidebarPageViewHolder(private val frame: FrameLayout) : RecyclerView.ViewHolder(frame) {
        fun bind(config: SidebarPage) {
            frame.removeAllViews()
            // Here we instantiate the actual views lazily based on config type
            val context = frame.context
            
            val pageView: View = when (config.type) {
                "apps" -> {
                    val prefKey = "sidebar_apps_${physicalHandleId}_${config.id}"
                    val manager = appsManagers.getOrPut(prefKey) {
                        SidebarAppsManager(context, prefs, viewScope, prefKey) {}
                    }
                    manager.ensureLoaded()
                    val p = AppsPageView(context, physicalHandleId, config, manager, viewScope,
                        onCloseSidebar = { onClose() },
                        onHeightChanged = { newHeight ->
                            // Optional: adjust height if wrap_content
                        }
                    )
                    p.updateData(manager.activeItems)
                    p
                }
                "hybrid_grid" -> {
                    HybridGridPageView(context, config.id) { newHeight ->
                        // Optional: adjust height
                    }
                }
                "widgets_grid" -> {
                    WidgetsGridPageView(context, config.id) { newHeight ->
                        // Optional: adjust height
                    }
                }
                "app_tracker" -> {
                    AppTrackerPageView(context, onClose, { _ -> onClose() })
                }
                else -> {
                    TextView(context).apply {
                        text = "Page: ${config.title}\nType: ${config.type}\n(Not Implemented)"
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        gravity = Gravity.CENTER
                        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    }
                }
            }
            frame.addView(pageView)
        }
    }
}
