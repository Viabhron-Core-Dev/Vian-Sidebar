package com.example.feature.sidebar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.utils.SidebarPage
import com.example.util.AppLogger
import kotlin.math.max

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

    private val wrapContent = prefs.getBoolean("handle_${containerId}_sidebar_wrap_content", prefs.getBoolean("sidebar_wrap_content", true))
    private val layoutParams: WindowManager.LayoutParams
    private val viewPager: ViewPager2
    private lateinit var container: FrameLayout
    private lateinit var dotsLayout: LinearLayout
    private val dots = mutableListOf<View>()
    private lateinit var editButton: ImageView
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
            y = 0
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
            val colorHex = prefs.getString("handle_${containerId}_sidebar_color", prefs.getString("sidebar_color", "#1E1E2E")) ?: "#1E1E2E"
            val baseColor = try { Color.parseColor(colorHex) } catch(e:Exception){ Color.parseColor("#1E1E2E") }
            val r = Color.red(baseColor)
            val g = Color.green(baseColor)
            val b = Color.blue(baseColor)
            setColor(Color.argb(alphaInt, r, g, b))

            setStroke((1 * density).toInt(), Color.argb(80, 255, 255, 255))

            val radius = 16f * density
            cornerRadii = if (isRight) {
                floatArrayOf(radius, radius, 0f, 0f, 0f, 0f, radius, radius)
            } else {
                floatArrayOf(0f, 0f, radius, radius, radius, radius, 0f, 0f)
            }
        }
        background = drawable

        val isLooping = pageConfigs.size > 2
        val startingIndex = if (isLooping) {
            val half = Int.MAX_VALUE / 2
            half - (half % pageConfigs.size) + max(0, defaultPageIndex)
        } else {
            max(0, defaultPageIndex)
        }

        val headerHeight = (36 * density).toInt()
        val edgeMargin = (16 * density).toInt()

        val header = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, headerHeight)
        }

        val closeText = TextView(context).apply {
            text = "✕"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(headerHeight, headerHeight).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                marginEnd = edgeMargin
            }
            setOnClickListener { onClose() }
        }
        header.addView(closeText)

        val settingsBtn = android.widget.ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_preferences)
            setColorFilter(Color.WHITE)
            setPadding((8*density).toInt(), (8*density).toInt(), (8*density).toInt(), (8*density).toInt())
            layoutParams = FrameLayout.LayoutParams(headerHeight, headerHeight).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                marginEnd = edgeMargin + headerHeight
            }
            setOnClickListener {
                val intent = Intent(context, com.example.feature.settings.SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(intent)
                onClose()
            }
        }
        header.addView(settingsBtn)

        editButton = android.widget.ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_edit)
            setColorFilter(Color.WHITE)
            val pad = (8 * density).toInt()
            setPadding(pad, pad, pad, pad)
            layoutParams = FrameLayout.LayoutParams(headerHeight, headerHeight).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                marginStart = edgeMargin
            }
            setOnClickListener {
                if (pageConfigs.isNotEmpty()) {
                    val currentItem = viewPager.currentItem
                    val actualPosition = if (pageConfigs.size > 2) currentItem % pageConfigs.size else currentItem
                    
                    val recyclerView = viewPager.getChildAt(0) as RecyclerView
                    val holder = recyclerView.findViewHolderForAdapterPosition(currentItem) as? SidebarPageViewHolder
                    var handledLocally = false
                    if (holder != null) {
                        val frame = holder.itemView as FrameLayout
                        if (frame.childCount > 0) {
                            val child = frame.getChildAt(0)
                            val pageView = if (child is android.widget.ScrollView) (child as android.view.ViewGroup).getChildAt(0) else child
                            if (pageView is SidebarPageControllable) {
                                pageView.onEditClicked()
                                handledLocally = true
                            } else if (child is SidebarPageControllable) {
                                child.onEditClicked()
                                handledLocally = true
                            }
                        }
                    }
                    
                    if (!handledLocally) {
                        // Fallback to Settings if the view doesn't implement its own editor
                        val intent = Intent(context, com.example.feature.settings.SettingsActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        context.startActivity(intent)
                        onClose()
                    }
                }
            }
        }
        header.addView(editButton)

        container = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = headerHeight
            }
        }

        viewPager = ViewPager2(context).apply {
            layoutParams = if (wrapContent) {
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            } else {
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
            offscreenPageLimit = if (containerId == "sidebar") 1 else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
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
        
        dotsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
                gravity = Gravity.CENTER
            }
        }
        
        setupDots(pageConfigs.size)

        container.addView(viewPager)
        header.addView(dotsLayout)

        addView(header)
        addView(container)

        viewPager.setCurrentItem(startingIndex, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (pageConfigs.isNotEmpty()) {
                    val actualPosition = if (isLooping) position % pageConfigs.size else position
                    
                }
                
                // Notify lifecycle
                val rcv = viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
                rcv?.let {
                    for (i in 0 until it.childCount) {
                        val child = it.getChildAt(i)
                        val holder = it.getChildViewHolder(child) as? SidebarPageViewHolder
                        if (holder?.bindingAdapterPosition == position) {
                            (holder.pageView as? SidebarPageControllable)?.onPageSelected()
                        } else {
                            (holder?.pageView as? SidebarPageControllable)?.onPageUnselected()
                        }
                    }
                }
            }
        })
    }
    
    private fun setupDots(count: Int) {
        dots.clear()
        dotsLayout.removeAllViews()
        if (count <= 1) {
            dotsLayout.visibility = View.GONE
            return
        }
        dotsLayout.visibility = View.VISIBLE
        val density = context.resources.displayMetrics.density
        val size = (8 * density).toInt()
        val margin = (4 * density).toInt()
        
        for (i in 0 until count) {
            val dot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(Color.WHITE)
                }
            }
            dots.add(dot)
            dotsLayout.addView(dot)
        }
        updateDots(0)
    }
    
    private fun updateDots(position: Int) {
        if (dots.isEmpty()) return
        val density = context.resources.displayMetrics.density
        
        for (i in dots.indices) {
            val bg = dots[i].background as? android.graphics.drawable.GradientDrawable
            if (i == position) {
                bg?.setStroke(0, Color.TRANSPARENT)
                bg?.setColor(Color.WHITE)
                dots[i].layoutParams = (dots[i].layoutParams as LinearLayout.LayoutParams).apply {
                    width = (8 * density).toInt()
                    height = (8 * density).toInt()
                }
            } else {
                bg?.setStroke((1 * density).toInt(), Color.WHITE)
                bg?.setColor(Color.TRANSPARENT)
                dots[i].layoutParams = (dots[i].layoutParams as LinearLayout.LayoutParams).apply {
                    width = (6 * density).toInt()
                    height = (6 * density).toInt()
                }
            }
        }
    }

    fun attach() {
        if (!isAttached) {
            windowManager.addView(this, layoutParams)
            isAttached = true
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            onClose()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            onClose()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    fun detach() {
        if (isAttached) {
            // Notify unselected before removing
            val rcv = viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
            rcv?.let {
                for (i in 0 until it.childCount) {
                    val child = it.getChildAt(i)
                    val holder = it.getChildViewHolder(child) as? SidebarPageViewHolder
                    (holder?.pageView as? SidebarPageControllable)?.onPageUnselected()
                }
            }
            windowManager.removeView(this)
            isAttached = false
            viewScope.cancel()
        }
    }
    
    inner class SidebarPageViewHolder(private val frame: FrameLayout) : RecyclerView.ViewHolder(frame) {
        var pageView: View? = null

        fun bind(config: SidebarPage) {
            frame.removeAllViews()
            val context = frame.context
            
            pageView = when (config.type) {
                "calculator" -> CalculatorPageView(context)
                "compass" -> CompassPageView(context)
                "apps" -> {
                    val prefKey = "sidebar_apps_${physicalHandleId}_${config.id}"
                    val manager = appsManagers.getOrPut(prefKey) {
                        SidebarAppsManager(context, prefs, viewScope, prefKey) {}
                    }
                    manager.ensureLoaded()
                    val p = AppsPageView(context, physicalHandleId, config, manager, viewScope,
                        onCloseSidebar = { onClose() },
                        onHeightChanged = { newHeight ->
                            if (wrapContent && viewPager.currentItem == bindingAdapterPosition) {
                                val params = viewPager.layoutParams
                                if (params.height != newHeight) {
                                    params.height = newHeight
                                    viewPager.layoutParams = params
                                    windowManager.updateViewLayout(this@SidebarView, layoutParams)
                                }
                            }
                        }
                    )
                    p.updateData(manager.activeItems)
                    p
                }
                "hybrid_grid" -> {
                    HybridGridPageView(context, config.id) { newHeight ->
                        if (wrapContent && viewPager.currentItem == bindingAdapterPosition) {
                            val params = viewPager.layoutParams
                            if (params.height != newHeight) {
                                params.height = newHeight
                                viewPager.layoutParams = params
                                windowManager.updateViewLayout(this@SidebarView, layoutParams)
                            }
                        }
                    }
                }
                "widgets_grid" -> {
                    WidgetsGridPageView(context, config.id) { newHeight ->
                        if (wrapContent && viewPager.currentItem == bindingAdapterPosition) {
                            val params = viewPager.layoutParams
                            if (params.height != newHeight) {
                                params.height = newHeight
                                viewPager.layoutParams = params
                                windowManager.updateViewLayout(this@SidebarView, layoutParams)
                            }
                        }
                    }
                }
                "app_tracker" -> {
                    AppTrackerPageView(context, onClose, { _ -> onClose() })
                }
                "media_player" -> {
                    MediaPlayerPageView(context, onClose) { newHeight ->
                        if (wrapContent && viewPager.currentItem == bindingAdapterPosition) {
                            val params = viewPager.layoutParams
                            if (params.height != newHeight) {
                                params.height = newHeight
                                viewPager.layoutParams = params
                                windowManager.updateViewLayout(this@SidebarView, layoutParams)
                            }
                        }
                    }
                }
                "widget" -> {
                    WidgetPageView(context, config.id) { newHeight ->
                        if (wrapContent && viewPager.currentItem == bindingAdapterPosition) {
                            val params = viewPager.layoutParams
                            if (params.height != newHeight) {
                                params.height = newHeight
                                viewPager.layoutParams = params
                                windowManager.updateViewLayout(this@SidebarView, layoutParams)
                            }
                        }
                    }
                }
                "scheduler" -> SchedulerPageView(context, viewScope)
                "notifications" -> NotificationPageView(context, { onClose() }, { /* TODO: onHideApp */ })
                "resources_tracker" -> ResourcesTrackerPageView(context, viewScope)
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
