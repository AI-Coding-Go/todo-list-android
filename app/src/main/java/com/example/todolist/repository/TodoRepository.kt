package com.example.todolist.repository

import androidx.lifecycle.LiveData
import com.example.todolist.data.Todo
import com.example.todolist.data.TodoDao

class TodoRepository(private val todoDao: TodoDao) {
    
    val allTodos: LiveData<List<Todo>> = todoDao.getAllTodos()
    val activeTodos: LiveData<List<Todo>> = todoDao.getActiveTodos()
    val completedTodos: LiveData<List<Todo>> = todoDao.getCompletedTodos()
    val activeTodoCount: LiveData<Int> = todoDao.getActiveTodoCount()
    val completedTodoCount: LiveData<Int> = todoDao.getCompletedTodoCount()

    suspend fun insertTodo(todo: Todo): Long {
        return todoDao.insertTodo(todo)
    }

    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }

    suspend fun updateTodoStatus(id: Long, completed: Boolean) {
        val completedAt = if (completed) System.currentTimeMillis() else null
        todoDao.updateTodoStatus(id, completed, completedAt)
    }

    suspend fun getTodoById(id: Long): Todo? {
        return todoDao.getTodoById(id)
    }

    suspend fun getTodosWithReminders(currentTime: Long, endTime: Long): List<Todo> {
        return todoDao.getTodosWithReminders(currentTime, endTime)
    }
}