import re

with open('app/src/main/res/layout/layout_dictionary.xml', 'r') as f:
    content = f.read()

# Replace the top_drag_bar and add tab bar
new_top_bar = """        <LinearLayout
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
        </LinearLayout>"""

# Find top_drag_bar block
content = re.sub(r'<LinearLayout\s+android:id="@+id/top_drag_bar".*?</LinearLayout>', new_top_bar, content, flags=re.DOTALL)

# Add dict_content_area ID to the linear layout inside FrameLayout
content = re.sub(
    r'(<FrameLayout[^>]*>\s*)<LinearLayout\s+android:layout_width="match_parent"\s+android:layout_height="match_parent"\s+android:orientation="vertical">',
    r'\1<LinearLayout\n                android:id="@+id/dict_content_area"\n                android:layout_width="match_parent"\n                android:layout_height="match_parent"\n                android:orientation="vertical">',
    content, count=1)

# Add translate_content_area right after dict_content_area ends
# dict_content_area ends before <!-- Bottom Window Controls -->
translate_area = """                <!-- Translate Area -->
                <LinearLayout
                    android:id="@+id/translate_content_area"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:visibility="gone">
                    <!-- Source Section -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="center_vertical"
                        android:padding="4dp">
                        
                        <Spinner
                            android:id="@+id/spinner_source"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:theme="@style/Theme.AppCompat" />
                            
                        <ImageView
                            android:id="@+id/btn_read_source"
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:src="@android:drawable/ic_media_play"
                            android:tint="#FFFFFF"
                            android:background="?android:attr/selectableItemBackgroundBorderless"
                            android:contentDescription="Read Aloud" />
                    </LinearLayout>
                    
                    <EditText
                        android:id="@+id/edit_source"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:minHeight="80dp"
                        android:gravity="top|start"
                        android:hint="Enter text"
                        android:background="#333344"
                        android:padding="8dp"
                        android:layout_margin="8dp"
                        android:textColorHint="#888888"
                        android:textColor="#FFFFFF" />
                        
                    <View
                        android:layout_width="match_parent"
                        android:layout_height="1dp"
                        android:background="#444455"
                        android:layout_marginHorizontal="8dp"
                        android:layout_marginBottom="8dp" />
            
                    <!-- Target Section -->
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="center_vertical"
                        android:padding="4dp">
                        
                        <Spinner
                            android:id="@+id/spinner_target"
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:theme="@style/Theme.AppCompat" />
                            
                        <ImageView
                            android:id="@+id/btn_read_target"
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:src="@android:drawable/ic_media_play"
                            android:tint="#FFFFFF"
                            android:background="?android:attr/selectableItemBackgroundBorderless"
                            android:contentDescription="Read Aloud" />
                    </LinearLayout>
                    
                    <TextView
                        android:id="@+id/text_target"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:minHeight="80dp"
                        android:gravity="top|start"
                        android:background="#2A2A3C"
                        android:padding="8dp"
                        android:layout_margin="8dp"
                        android:textColor="#FFFFFF"
                        android:textIsSelectable="true" />
                        
                </LinearLayout>
"""

# Insert before bottom window controls
content = content.replace('            <!-- Bottom Window Controls -->', translate_area + '\n            <!-- Bottom Window Controls -->')

with open('app/src/main/res/layout/layout_dictionary.xml', 'w') as f:
    f.write(content)
