package com.example.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.Todo
import com.example.todolist.viewmodel.TodoViewModel

class ChartActivity : AppCompatActivity() {

    private val todoViewModel: TodoViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryStatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        setupToolbar()
        setupRecyclerView()
        observeData()
    }

    private fun setupToolbar() {
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryStatAdapter()
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewCategories).apply {
            layoutManager = LinearLayoutManager(this@ChartActivity)
            adapter = categoryAdapter
        }
    }

    private fun observeData() {
        todoViewModel.allTodos.observe(this) { todos ->
            updateStatistics(todos)
        }
    }

    private fun updateStatistics(todos: List<Todo>) {
        // 总体统计
        val totalCount = todos.size
        val completedCount = todos.count { it.isCompleted }
        val completionRate = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

        findViewById<android.widget.TextView>(R.id.textTotalCount).text = totalCount.toString()
        findViewById<android.widget.TextView>(R.id.textCompletionRate).text = "$completionRate%"

        // 优先级分布
        val highPriorityCount = todos.count { it.priority == 3 }
        val mediumPriorityCount = todos.count { it.priority == 2 }
        val lowPriorityCount = todos.count { it.priority == 1 }

        val maxPriorityCount = maxOf(highPriorityCount, mediumPriorityCount, lowPriorityCount)

        findViewById<android.widget.ProgressBar>(R.id.progressHigh).progress = if (maxPriorityCount > 0) (highPriorityCount * 100 / maxPriorityCount) else 0
        findViewById<android.widget.ProgressBar>(R.id.progressMedium).progress = if (maxPriorityCount > 0) (mediumPriorityCount * 100 / maxPriorityCount) else 0
        findViewById<android.widget.ProgressBar>(R.id.progressLow).progress = if (maxPriorityCount > 0) (lowPriorityCount * 100 / maxPriorityCount) else 0

        findViewById<android.widget.TextView>(R.id.textHighCount).text = highPriorityCount.toString()
        findViewById<android.widget.TextView>(R.id.textMediumCount).text = mediumPriorityCount.toString()
        findViewById<android.widget.TextView>(R.id.textLowCount).text = lowPriorityCount.toString()

        // 分类统计
        val categoryStats = todos.groupBy { it.category }
            .map { (category, categoryTodos) ->
                CategoryStat(category, categoryTodos.size)
            }
            .sortedByDescending { it.count }

        categoryAdapter.submitList(categoryStats)
    }
}

data class CategoryStat(
    val category: String,
    val count: Int
)

class CategoryStatAdapter : RecyclerView.Adapter<CategoryStatAdapter.ViewHolder>() {

    private var categoryStats = listOf<CategoryStat>()

    fun submitList(stats: List<CategoryStat>) {
        categoryStats = stats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categoryStats[position])
    }

    override fun getItemCount(): Int = categoryStats.size

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        fun bind(stat: CategoryStat) {
            itemView.findViewById<android.widget.TextView>(R.id.textCategoryName).text = stat.category
            itemView.findViewById<android.widget.TextView>(R.id.textCategoryCount).text = stat.count.toString()
        }
    }
}