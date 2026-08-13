with open('app/src/main/java/com/example/feature/sidebar/SchedulerPageView.kt', 'r') as f:
    text = f.read()

text = text.replace('class SchedulerPageView(context: Context, private val scope: CoroutineScope) : FrameLayout(context) {',
'''class SchedulerPageView(context: Context, private val scope: CoroutineScope) : FrameLayout(context), SidebarPageControllable {''')

old_load_tasks = """    private fun loadTasks() {
        scope.launch(Dispatchers.Main) {
            db.schedulerTaskDao().getAllTasks().collect { tasks ->
                val activeTasks = tasks.filter { it.status == "PENDING" }.sortedBy { it.timeMillis }
                adapter.submitList(activeTasks)
                
                // Automatically mark past tasks as unfinished
                val now = System.currentTimeMillis()
                tasks.filter { it.status == "PENDING" && it.timeMillis < now }.forEach { pastTask ->
                    scope.launch(Dispatchers.IO) {
                        db.schedulerTaskDao().update(pastTask.copy(status = "UNFINISHED"))
                    }
                }
                
                if (activeTasks.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }"""

new_load_tasks = """    private var taskJob: Job? = null

    override fun onPageSelected() {
        if (taskJob?.isActive == true) return
        loadTasks()
    }

    override fun onPageUnselected() {
        taskJob?.cancel()
        taskJob = null
    }

    private fun loadTasks() {
        taskJob = scope.launch(Dispatchers.Main) {
            db.schedulerTaskDao().getAllTasks().collect { tasks ->
                val activeTasks = tasks.filter { it.status == "PENDING" }.sortedBy { it.timeMillis }
                adapter.submitList(activeTasks)
                
                // Automatically mark past tasks as unfinished
                val now = System.currentTimeMillis()
                tasks.filter { it.status == "PENDING" && it.timeMillis < now }.forEach { pastTask ->
                    scope.launch(Dispatchers.IO) {
                        db.schedulerTaskDao().update(pastTask.copy(status = "UNFINISHED"))
                    }
                }
                
                if (activeTasks.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }"""

# Don't start loading right on init if we want to defer it until onPageSelected
text = text.replace('        loadTasks()\n    }', '        // loadTasks() called in onPageSelected now\n    }')
text = text.replace(old_load_tasks, new_load_tasks)

with open('app/src/main/java/com/example/feature/sidebar/SchedulerPageView.kt', 'w') as f:
    f.write(text)
