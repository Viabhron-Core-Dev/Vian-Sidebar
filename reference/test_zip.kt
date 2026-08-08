import java.util.zip.ZipInputStream
import java.util.zip.GZIPInputStream
import java.io.FileInputStream

fun test() {
    val z = ZipInputStream(FileInputStream("test.zip"))
}
