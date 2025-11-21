package com.example.todolist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 1: 低, 2: 中, 3: 高
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val reminderTime: Long? = null,
    val category: String = "默认"
)