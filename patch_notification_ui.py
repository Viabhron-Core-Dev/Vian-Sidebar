filepath = 'app/src/main/res/layout/page_notification.xml'
with open(filepath, 'r') as f:
    content = f.read()

old_header = """        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Notifications"
            android:textColor="#FFFFFF"
            android:textSize="22sp"
            android:paddingBottom="8dp" />"""

new_header = """        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:paddingBottom="8dp">
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Notifications"
                android:textColor="#FFFFFF"
                android:textSize="22sp" />
            <ImageView
                android:id="@+id/btn_history"
                android:layout_width="32dp"
                android:layout_height="32dp"
                android:padding="4dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@android:drawable/ic_menu_recent_history"
                android:contentDescription="History" />
        </LinearLayout>"""

content = content.replace(old_header, new_header)

with open(filepath, 'w') as f:
    f.write(content)
print("page_notification.xml updated")
