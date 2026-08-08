import org.json.JSONArray
fun main() {
    val arr = JSONArray()
    val str = """folder:123:{"name":"Test"}"""
    arr.put(str)
    println(arr.toString())
    
    val arr2 = JSONArray(arr.toString())
    println(arr2.getString(0))
}
