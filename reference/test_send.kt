import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.app.ActivityOptions

fun sendIntentSafe(context: Context, pi: PendingIntent?) {
    if (pi == null) return
    if (Build.VERSION.SDK_INT >= 34) {
        val options = ActivityOptions.makeBasic()
        options.pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
        pi.send(context, 0, Intent(), null, null, null, options.toBundle())
    } else {
        pi.send()
    }
}
