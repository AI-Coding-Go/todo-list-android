package com.example.todolist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.AppDatabase
import com.example.todolist.data.Todo
import com.example.todolist.repository.TodoRepository
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: TodoRepository
    val allTodos: LiveData<List<Todo>>
    val activeTodos: LiveData<List<Todo>>
    val completedTodos: LiveData<List<Todo>>
    val activeTodoCount: LiveData<Int>
    val completedTodoCount: LiveData<Int>

    private val _selectedTodo = MutableLiveData<Todo?>()
    val selectedTodo: LiveData<Todo?> = _selectedTodo

    init {
        val todoDao = AppDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)
        allTodos = repository.allTodos
        activeTodos = repository.activeTodos
        completedTodos = repository.completedTodos
        activeTodoCount = repository.activeTodoCount
        completedTodoCount = repository.completedTodoCount
    }

    fun insertTodo(todo: Todo) = viewModelScope.launch {
        repository.insertTodo(todo)
    }

    fun updateTodo(todo: Todo) = viewModelScope.launch {
        repository.updateTodo(todo)
    }

    fun deleteTodo(todo: Todo) = viewModelScope.launch {
        repository.deleteTodo(todo)
    }

    fun toggleTodoStatus(todo: Todo) = viewModelScope.launch {
        repository.updateTodoStatus(todo.id, !todo.isCompleted)
    }

    fun selectTodo(todo: Todo) {
        _selectedTodo.value = todo
    }

    fun clearSelectedTodo() {
        _selectedTodo.value = null
    }

    fun getTodoById(id: Long) = viewModelScope.launch {
        val todo = repository.getTodoById(id)
        _selectedTodo.postValue(todo)
    }
}