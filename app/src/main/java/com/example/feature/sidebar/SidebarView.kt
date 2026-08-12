package com.example.feature.sidebar
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.View

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
) : LinearLayout(context) {

    private val wrapContent = prefs.getBoolean("handle_${containerId}_sidebar_wrap_content", prefs.getBoolean("sidebar_wrap_content", true))
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
        
        orientation = VERTICAL

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
        
        val marginDp = 8f
        val marginPx = (marginDp * density).toInt()
        setPadding(0, marginPx, 0, marginPx)
        

        // Topbar
                val isLooping = pageConfigs.size > 2
        val startingIndex = if (isLooping) {
            val half = Int.MAX_VALUE / 2
            half - (half % pageConfigs.size) + max(0, defaultPageIndex)
        } else {
            max(0, defaultPageIndex)
        }

        val topBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#2A2A3C"))
            setPadding((12 * density).toInt(), (8 * density).toInt(), (12 * density).toInt(), (8 * density).toInt())
        }

        // Edit Button
        val editBtn = android.widget.ImageView(context).apply {
            layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt())
            setImageResource(android.R.drawable.ic_menu_edit)
            setColorFilter(Color.WHITE)
            setOnClickListener {
                if (pageConfigs.isNotEmpty()) {
                    val currentItem = viewPager.currentItem
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
                        val actualPosition = if (pageConfigs.size > 2) currentItem % pageConfigs.size else currentItem
                        val currentPage = pageConfigs[actualPosition]
                        val intent = Intent(context, com.example.feature.settings.SettingsActivity::class.java)
                        intent.putExtra("sidebar_edit_page_id", currentPage.id)
                        intent.putExtra("sidebar_container_id", containerId)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        onClose()
                    }
                }
            }
        }
        topBar.addView(editBtn)

        // Title / Page Indicator
        val titleText = TextView(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 14f
            text = if (pageConfigs.isNotEmpty()) pageConfigs[startingIndex].title else "Sidebar"
        }
        topBar.addView(titleText)

        // Settings Button
        val settingsBtn = android.widget.ImageView(context).apply {
            layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt())
            setImageResource(android.R.drawable.ic_menu_preferences)
            setColorFilter(Color.WHITE)
            setOnClickListener {
                val intent = Intent(context, com.example.feature.settings.SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                onClose()
            }
        }
        topBar.addView(settingsBtn)

        addView(topBar)

        // ViewPager
        viewPager = ViewPager2(context).apply {
            layoutParams = if (wrapContent) {
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            } else {
                LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
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
        
        viewPager.setCurrentItem(startingIndex, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (pageConfigs.isNotEmpty()) {
                    val actualPosition = if (isLooping) position % pageConfigs.size else position
                    titleText.text = pageConfigs[actualPosition].title
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
        
        addView(viewPager)

        addView(viewPager)
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
            // Here we instantiate the actual views lazily based on config type
            val context = frame.context
            
            pageView = when (config.type) {
                "calculator" -> com.example.feature.sidebar.CalculatorPageView(context)
                "compass" -> com.example.feature.sidebar.CompassPageView(context)
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
