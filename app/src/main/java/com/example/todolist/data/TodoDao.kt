package com.example.todolist.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY priority DESC, createdAt DESC")
    fun getAllTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun getActiveTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): Todo?

    @Insert
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Query("UPDATE todos SET isCompleted = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTodoStatus(id: Long, completed: Boolean, completedAt: Long?)

    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0")
    fun getActiveTodoCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 1")
    fun getCompletedTodoCount(): LiveData<Int>

    @Query("SELECT * FROM todos WHERE reminderTime > :currentTime AND reminderTime <= :endTime AND isCompleted = 0")
    suspend fun getTodosWithReminders(currentTime: Long, endTime: Long): List<Todo>
}