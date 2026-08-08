import android.provider.Settings

fun main() {
    println(Settings.Panel::class.java.declaredFields.map { it.name })
}
