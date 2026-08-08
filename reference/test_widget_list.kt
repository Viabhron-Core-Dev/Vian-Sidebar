import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Process
import android.os.UserManager

fun getAllProviders(context: Context): List<android.appwidget.AppWidgetProviderInfo> {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val providers = mutableListOf<android.appwidget.AppWidgetProviderInfo>()
    
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        for (profile in userManager.userProfiles) {
            providers.addAll(appWidgetManager.getInstalledProvidersForProfile(profile))
        }
    } else {
        providers.addAll(appWidgetManager.installedProviders)
    }
    return providers
}
