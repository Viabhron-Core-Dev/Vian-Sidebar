import re

with open("app/src/main/res/layout/layout_dictionary.xml", "r") as f:
    content = f.read()

old_top_bar = """        <!-- Top Bar -->
        <LinearLayout
            android:id="@+id/top_drag_bar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:minHeight="28dp"
            android:background="#2A2A3C"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:paddingHorizontal="8dp"
            android:paddingVertical="6dp">
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Dictionary"
                android:textColor="#FFFFFF"
                android:textStyle="bold"
                android:textSize="14sp" />
        </LinearLayout>"""

new_top_bar = """        <!-- Top Bar -->
        <LinearLayout
            android:id="@+id/top_drag_bar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:minHeight="28dp"
            android:background="#2A2A3C"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:paddingHorizontal="8dp"
            android:paddingVertical="6dp">
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Dictionary"
                android:textColor="#FFFFFF"
                android:textStyle="bold"
                android:textSize="14sp" />
            <ImageView
                android:id="@+id/btn_settings"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@android:drawable/ic_menu_preferences"
                android:tint="#FFFFFF"
                android:background="?android:attr/selectableItemBackgroundBorderless"
                android:contentDescription="Settings" />
        </LinearLayout>"""

content = content.replace(old_top_bar, new_top_bar)

old_speak_def = """                        <com.google.android.material.floatingactionbutton.FloatingActionButton
                            android:id="@+id/btn_speak_def"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_gravity="bottom|end"
                            android:layout_margin="16dp"
                            android:src="@android:drawable/ic_lock_silent_mode_off"
                            android:tint="#FFFFFF"
                            android:contentDescription="Speak Definition" />"""

content = content.replace(old_speak_def, "")

with open("app/src/main/res/layout/layout_dictionary.xml", "w") as f:
    f.write(content)
