package com.example.todolist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.AppDatabase
import com.example.todolist.data.Todo
import com.example.todolist.data.TodoDao
import com.example.todolist.repository.TodoApiRepository
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val todoDao: TodoDao = AppDatabase.getDatabase(application).todoDao()
    private val apiRepository = TodoApiRepository(todoDao)

    // LiveData 从 API Repository 获取（内部从本地数据库获取）
    val allTodos: LiveData<List<Todo>> = apiRepository.allTodos
    val activeTodos: LiveData<List<Todo>> = apiRepository.activeTodos
    val completedTodos: LiveData<List<Todo>> = apiRepository.completedTodos
    val activeTodoCount: LiveData<Int> = apiRepository.activeTodoCount
    val completedTodoCount: LiveData<Int> = apiRepository.completedTodoCount
    val isLoading: LiveData<Boolean> = apiRepository.isLoading
    val error: LiveData<String?> = apiRepository.error

    private val _selectedTodo = MutableLiveData<Todo?>()
    val selectedTodo: LiveData<Todo?> = _selectedTodo

    init {
        // 初始化时从服务器刷新数据
        refreshData()
    }

    /**
     * 从服务器刷新所有数据并同步到本地数据库
     */
    fun refreshData() = viewModelScope.launch {
        apiRepository.refreshAllData()
    }

    /**
     * 插入 Todo - 先调用 API，成功后保存到本地数据库
     */
    fun insertTodo(todo: Todo, onSuccess: ((Long) -> Unit)? = null, onError: ((String) -> Unit)? = null) = viewModelScope.launch {
        apiRepository.insertTodo(todo)
            .onSuccess { id -> onSuccess?.invoke(id) }
            .onFailure { e -> onError?.invoke(e.message ?: "Failed to insert todo") }
    }

    /**
     * 更新 Todo - 先调用 API，成功后更新本地数据库
     */
    fun updateTodo(todo: Todo, onSuccess: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) = viewModelScope.launch {
        apiRepository.updateTodo(todo)
            .onSuccess { onSuccess?.invoke() }
            .onFailure { e -> onError?.invoke(e.message ?: "Failed to update todo") }
    }

    /**
     * 删除 Todo - 先调用 API，成功后从本地数据库删除
     */
    fun deleteTodo(todo: Todo, onSuccess: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) = viewModelScope.launch {
        apiRepository.deleteTodo(todo)
            .onSuccess { onSuccess?.invoke() }
            .onFailure { e -> onError?.invoke(e.message ?: "Failed to delete todo") }
    }

    /**
     * 切换 Todo 状态 - 先调用 API，成功后更新本地数据库
     */
    fun toggleTodoStatus(todo: Todo, onSuccess: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) = viewModelScope.launch {
        apiRepository.toggleTodoStatus(todo.id, !todo.isCompleted)
            .onSuccess { onSuccess?.invoke() }
            .onFailure { e -> onError?.invoke(e.message ?: "Failed to toggle status") }
    }

    /**
     * 选择 Todo
     */
    fun selectTodo(todo: Todo) {
        _selectedTodo.value = todo
    }

    /**
     * 清除选中的 Todo
     */
    fun clearSelectedTodo() {
        _selectedTodo.value = null
    }

    /**
     * 根据 ID 获取 Todo - 先从本地数据库获取，如果不存在则从服务器获取
     */
    fun getTodoById(id: Long, onSuccess: ((Todo?) -> Unit)? = null, onError: ((String) -> Unit)? = null) = viewModelScope.launch {
        apiRepository.getTodoById(id)
            .onSuccess { todo ->
                _selectedTodo.postValue(todo)
                onSuccess?.invoke(todo)
            }
            .onFailure { e -> onError?.invoke(e.message ?: "Failed to get todo") }
    }

    /**
     * 根据 category 获取 Todo - 从本地数据库获取
     */
    fun getTodosByCategory(category: String, onSuccess: ((List<Todo>) -> Unit)? = null, onError: ((String) -> Unit)? = null) = viewModelScope.launch {
        apiRepository.getTodosByCategory(category)
            .onSuccess { todos -> onSuccess?.invoke(todos) }
            .onFailure { e -> onError?.invoke(e.message ?: "Failed to get todos by category") }
    }

    /**
     * 根据 priority 获取 Todo - 从本地数据库获取
     */
    fun getTodosByPriority(priority: Int, onSuccess: ((List<Todo>) -> Unit)? = null, onError: ((String) -> Unit)? = null) = viewModelScope.launch {
        apiRepository.getTodosByPriority(priority)
            .onSuccess { todos -> onSuccess?.invoke(todos) }
            .onFailure { e -> onError?.invoke(e.message ?: "Failed to get todos by priority") }
    }

    /**
     * 获取有提醒的 Todo - 从本地数据库获取
     */
    suspend fun getTodosWithReminders(currentTime: Long, endTime: Long): List<Todo> {
        return apiRepository.getTodosWithReminders(currentTime, endTime)
    }
}