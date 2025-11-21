package com.example.todolist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.data.Todo

import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(
    private val onTodoClick: (Todo) -> Unit,
    private val onTodoStatusChange: (Todo, Boolean) -> Unit,
    private val onTodoEdit: (Todo) -> Unit,
    private val onTodoDelete: (Todo) -> Unit,
    private val onSetReminder: (Todo) -> Unit
) : ListAdapter<Todo, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TodoViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

        fun bind(todo: Todo) {
            val checkboxCompleted = itemView.findViewById<android.widget.CheckBox>(R.id.checkboxCompleted)
            val textTitle = itemView.findViewById<android.widget.TextView>(R.id.textTitle)
            val textDescription = itemView.findViewById<android.widget.TextView>(R.id.textDescription)
            val textCategory = itemView.findViewById<android.widget.TextView>(R.id.textCategory)
            val textDate = itemView.findViewById<android.widget.TextView>(R.id.textDate)
            val textPriority = itemView.findViewById<android.widget.TextView>(R.id.textPriority)
            val btnMore = itemView.findViewById<android.widget.ImageButton>(R.id.btnMore)
            
            checkboxCompleted.isChecked = todo.isCompleted
            textTitle.text = todo.title
            textDescription.text = todo.description
            textCategory.text = todo.category
            textDate.text = dateFormat.format(Date(todo.createdAt))

            // 设置优先级显示
            textPriority.text = when (todo.priority) {
                3 -> "高优先级"
                2 -> "中优先级"
                else -> "低优先级"
            }
            textPriority.setTextColor(
                when (todo.priority) {
                    3 -> itemView.context.getColor(android.R.color.holo_red_dark)
                    2 -> itemView.context.getColor(android.R.color.holo_orange_dark)
                    else -> itemView.context.getColor(android.R.color.holo_green_dark)
                }
            )

            // 设置完成状态的视觉效果
            if (todo.isCompleted) {
                textTitle.alpha = 0.6f
                textDescription.alpha = 0.6f
                textTitle.paintFlags = textTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textTitle.alpha = 1.0f
                textDescription.alpha = 1.0f
                textTitle.paintFlags = textTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // 点击事件
            itemView.setOnClickListener {
                onTodoClick(todo)
            }

            checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                onTodoStatusChange(todo, isChecked)
            }

            btnMore.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.context_menu, popup.menu)
                
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onTodoEdit(todo)
                            true
                        }
                        R.id.action_delete -> {
                            onTodoDelete(todo)
                            true
                        }
                        R.id.action_set_reminder -> {
                            onSetReminder(todo)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }
}

class TodoDiffCallback : DiffUtil.ItemCallback<Todo>() {
    override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem == newItem
    }
}