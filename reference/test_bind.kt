import android.appwidget.AppWidgetManager
import android.content.Intent

fun test() {
    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 123)
    // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, componentName)
}
