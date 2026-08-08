import re
with open("app/src/main/res/layout/layout_dictionary.xml", "r") as f:
    content = f.read()

new_topbar = """        <!-- Top Bar -->
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
                android:layout_width="32dp"
                android:layout_height="32dp"
                android:padding="6dp"
                android:src="@android:drawable/ic_menu_manage"
                android:tint="#FFFFFF"
                android:background="?android:attr/selectableItemBackgroundBorderless"
                android:contentDescription="Settings" />
        </LinearLayout>"""

old_topbar = """        <!-- Top Bar -->
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

content = content.replace(new_topbar, old_topbar)

with open("app/src/main/res/layout/layout_dictionary.xml", "w") as f:
    f.write(content)
