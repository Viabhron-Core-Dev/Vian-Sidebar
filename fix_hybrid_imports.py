def remove_dup(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()
    
    out = []
    for line in lines:
        if 'import com.example.utils.AppWidgetHelper' in line:
            continue
        out.append(line)
        
    with open(filepath, 'w') as f:
        f.write("".join(out))

remove_dup('app/src/main/java/com/example/feature/sidebar/HybridGridPageView.kt')
remove_dup('app/src/main/java/com/example/feature/sidebar/WidgetsGridPageView.kt')
