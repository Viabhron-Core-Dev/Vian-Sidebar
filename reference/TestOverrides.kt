open class Base {
    open var id: String = ""
}
data class Folder(val uuid: String) : Base() {
    override var id = "folder:$uuid"
}
fun main() {
    val f = Folder("123")
    val newId = "folder:123:json"
    f.apply { this.id = newId }
    println(f.id)
}
