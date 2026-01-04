package com.example.todolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.adapter.TodoAdapter
import com.example.todolist.data.Todo
import com.example.todolist.viewmodel.TodoViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val todoViewModel: TodoViewModel by viewModels()
    private lateinit var todoAdapter: TodoAdapter
    private var currentTab = 0 // 0: 全部, 1: 待完成, 2: 已完成

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupTabs()
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            onTodoClick = { todo ->
                showAddEditDialog(todo)
            },
            onTodoStatusChange = { todo, isCompleted ->
                todoViewModel.toggleTodoStatus(todo,
                    onSuccess = {
                        // 状态切换成功
                    },
                    onError = { error ->
                        Toast.makeText(this, "状态切换失败: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onTodoEdit = { todo ->
                showAddEditDialog(todo)
            },
            onTodoDelete = { todo ->
                showDeleteConfirmDialog(todo)
            },
            onSetReminder = { todo ->
                showReminderDialog(todo)
            }
        )

        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = todoAdapter
        }
    }

    private fun setupObservers() {
        when (currentTab) {
            0 -> todoViewModel.allTodos.observe(this) { todos ->
                todoAdapter.submitList(todos)
                updateStats(todos)
            }
            1 -> todoViewModel.activeTodos.observe(this) { todos ->
                todoAdapter.submitList(todos)
                updateStats(todos)
            }
            2 -> todoViewModel.completedTodos.observe(this) { todos ->
                todoAdapter.submitList(todos)
                updateStats(todos)
            }
        }

        todoViewModel.activeTodoCount.observe(this) { count ->
            findViewById<android.widget.TextView>(R.id.activeCount).text = count.toString()
        }

        todoViewModel.completedTodoCount.observe(this) { count ->
            findViewById<android.widget.TextView>(R.id.completedCount).text = count.toString()
        }

        // 观察加载状态
        todoViewModel.isLoading.observe(this) { isLoading ->
            // 可以在这里显示/隐藏加载指示器
        }

        // 观察错误信息
        todoViewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, "错误: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab).setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun setupTabs() {
        findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout).addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                setupObservers()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateStats(todos: List<Todo>) {
        val activeCount = todos.count { !it.isCompleted }
        val completedCount = todos.count { it.isCompleted }
        
        findViewById<android.widget.TextView>(R.id.activeCount).text = activeCount.toString()
        findViewById<android.widget.TextView>(R.id.completedCount).text = completedCount.toString()
    }

    private fun showAddEditDialog(todo: Todo?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_todo, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(if (todo == null) "添加待办事项" else "编辑待办事项")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                saveTodoFromDialog(dialogView, todo)
            }
            .setNegativeButton("取消", null)
            .create()

        setupDialogViews(dialogView, todo)
        dialog.show()
    }

    private fun setupDialogViews(dialogView: View, todo: Todo?) {
        val editTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTitle)
        val editDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editDescription)
        val editCategory = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editCategory)
        val radioGroupPriority = dialogView.findViewById<android.widget.RadioGroup>(R.id.radioGroupPriority)
        val checkboxReminder = dialogView.findViewById<android.widget.CheckBox>(R.id.checkboxReminder)
        val textReminderTime = dialogView.findViewById<android.widget.TextView>(R.id.textReminderTime)

        if (todo != null) {
            editTitle.setText(todo.title)
            editDescription.setText(todo.description)
            editCategory.setText(todo.category)
            
            when (todo.priority) {
                3 -> radioGroupPriority.check(R.id.radioHigh)
                1 -> radioGroupPriority.check(R.id.radioLow)
                else -> radioGroupPriority.check(R.id.radioMedium)
            }

            if (todo.reminderTime != null) {
                checkboxReminder.isChecked = true
                textReminderTime.text = formatReminderTime(todo.reminderTime)
            }
        }

        var reminderTime = todo?.reminderTime

        textReminderTime.setOnClickListener {
            if (checkboxReminder.isChecked) {
                showDateTimePicker { time ->
                    reminderTime = time
                    textReminderTime.text = formatReminderTime(time)
                }
            }
        }

        checkboxReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && reminderTime == null) {
                showDateTimePicker { time ->
                    reminderTime = time
                    textReminderTime.text = formatReminderTime(time)
                }
            } else if (!isChecked) {
                reminderTime = null
                textReminderTime.text = "点击设置时间"
            }
        }
    }

    private fun saveTodoFromDialog(dialogView: View, todo: Todo?) {
        val editTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTitle)
        val editDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editDescription)
        val editCategory = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editCategory)
        val radioGroupPriority = dialogView.findViewById<android.widget.RadioGroup>(R.id.radioGroupPriority)
        val checkboxReminder = dialogView.findViewById<android.widget.CheckBox>(R.id.checkboxReminder)
        val textReminderTime = dialogView.findViewById<android.widget.TextView>(R.id.textReminderTime)

        val title = editTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
            return
        }

        val priority = when (radioGroupPriority.checkedRadioButtonId) {
            R.id.radioHigh -> 3
            R.id.radioLow -> 1
            else -> 2
        }

        val reminderTime = if (checkboxReminder.isChecked) {
            // 这里简化处理，实际应该保存选择的时间
            System.currentTimeMillis() + 24 * 60 * 60 * 1000 // 明天这个时候
        } else null

        if (todo == null) {
            val newTodo = Todo(
                title = title,
                description = editDescription.text.toString().trim(),
                category = editCategory.text.toString().trim().ifEmpty { "默认" },
                priority = priority,
                reminderTime = reminderTime
            )
            todoViewModel.insertTodo(newTodo,
                onSuccess = { id ->
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show()
                    if (reminderTime != null) {
                        setReminder(newTodo.copy(id = id), reminderTime)
                    }
                },
                onError = { error ->
                    Toast.makeText(this, "添加失败: $error", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            val updatedTodo = todo.copy(
                title = title,
                description = editDescription.text.toString().trim(),
                category = editCategory.text.toString().trim().ifEmpty { "默认" },
                priority = priority,
                reminderTime = reminderTime
            )
            todoViewModel.updateTodo(updatedTodo,
                onSuccess = {
                    Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show()
                    if (reminderTime != null) {
                        setReminder(updatedTodo, reminderTime)
                    }
                },
                onError = { error ->
                    Toast.makeText(this, "更新失败: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showDeleteConfirmDialog(todo: Todo) {
        MaterialAlertDialogBuilder(this)
            .setTitle("删除确认")
            .setMessage("确定要删除「${todo.title}」吗？")
            .setPositiveButton("删除") { _, _ ->
                todoViewModel.deleteTodo(todo,
                    onSuccess = {
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(this, "删除失败: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showReminderDialog(todo: Todo) {
        showDateTimePicker { time ->
            val updatedTodo = todo.copy(reminderTime = time)
            todoViewModel.updateTodo(updatedTodo,
                onSuccess = {
                    setReminder(updatedTodo, time)
                    Toast.makeText(this, "提醒已设置", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "设置提醒失败: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showDateTimePicker(onTimeSelected: (Long) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("选择日期")
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("选择时间")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedDate
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                calendar.set(Calendar.SECOND, 0)
                
                onTimeSelected(calendar.timeInMillis)
            }

            timePicker.show(supportFragmentManager, "time_picker")
        }

        datePicker.show(supportFragmentManager, "date_picker")
    }

    private fun setReminder(todo: Todo, reminderTime: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("todo_id", todo.id)
            putExtra("todo_title", todo.title)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            todo.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }

    private fun formatReminderTime(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        return "${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)} ${calendar.get(Calendar.HOUR_OF_DAY)}:${String.format("%02d", calendar.get(Calendar.MINUTE))}"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_chart -> {
                startActivity(Intent(this, ChartActivity::class.java))
                true
            }
            R.id.action_reminders -> {
                Toast.makeText(this, "提醒功能开发中", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}