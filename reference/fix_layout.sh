#!/bin/bash
# Insert tab bar after top_drag_bar
sed -i '/id="@+id\/top_drag_bar"/,/<\/LinearLayout>/!b;//!d;/<\/LinearLayout>/!d;r /dev/stdin' app/src/main/res/layout/layout_dictionary.xml << 'INNER_EOF'
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
                android:id="@+id/tv_window_title"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Dictionary &amp; Translate"
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
        </LinearLayout>

        <!-- Tab Bar -->
        <LinearLayout
            android:id="@+id/tab_bar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:background="#222233">
            <TextView
                android:id="@+id/tab_dictionary"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="Dictionary"
                android:textColor="#FFFFFF"
                android:gravity="center"
                android:padding="8dp"
                android:background="#333344" />
            <TextView
                android:id="@+id/tab_translate"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="Translate"
                android:textColor="#888888"
                android:gravity="center"
                android:padding="8dp"
                android:background="#222233" />
        </LinearLayout>
INNER_EOF

sed -i 's/<LinearLayout/<LinearLayout\n                android:id="@+id\/dict_content_area"/; 0,/<LinearLayout/!b' app/src/main/res/layout/layout_dictionary.xml
# Wait, the above sed might be tricky. Let's do it differently.
