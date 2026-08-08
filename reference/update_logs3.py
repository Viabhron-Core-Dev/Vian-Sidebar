import re

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'r') as f:
    content = f.read()

old_touch = """            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)"""
new_touch = """            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                com.example.LogKeeper.writeLog("SidebarEdit", "Item moved to position: ${viewHolder.adapterPosition}")
            }
        }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)"""
content = content.replace(old_touch, new_touch)

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'w') as f:
    f.write(content)
