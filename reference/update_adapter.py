import re

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'r') as f:
    content = f.read()

# Change `private inner class AppsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()` to `private inner class AppsAdapter(var items: List<SidebarItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>()`
content = content.replace('private inner class AppsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {', 'private inner class AppsAdapter(var items: List<SidebarItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {')

# Change `displayedItems[position]` to `items[position]` inside `AppsAdapter`
# But only inside AppsAdapter, not outside. Wait, `displayedItems.size` -> `items.size`
content = re.sub(r'override fun getItemViewType\(position: Int\): Int \{\s*return if \(displayedItems\[position\] is SidebarItem\.Spacer\) 1 else 0\s*\}', 'override fun getItemViewType(position: Int): Int {\n            return if (items[position] is SidebarItem.Spacer) 1 else 0\n        }', content)
content = content.replace('override fun getItemCount() = displayedItems.size', 'override fun getItemCount() = items.size')
content = re.sub(r'override fun onBindViewHolder\(holder: RecyclerView\.ViewHolder, position: Int\) \{\s*val item = displayedItems\[position\]', 'override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {\n            val item = items[position]', content)

# Change `updateItems` to modify `items`
update_items = """
        fun updateItems(newItems: List<SidebarItem>) {
            items = newItems
            notifyDataSetChanged()
        }
"""
content = re.sub(r'fun updateItems\(newItems: List<SidebarItem>\) \{\s*notifyDataSetChanged\(\)\s*\}', update_items.strip(), content)

# In `init`, `adapter = AppsAdapter()` -> `adapter = AppsAdapter(displayedItems)`
content = content.replace('adapter = AppsAdapter()', 'adapter = AppsAdapter(displayedItems)')

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'w') as f:
    f.write(content)

print("Fixed AppsAdapter.")
