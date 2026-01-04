package com.example.todolist.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.todolist.api.*
import com.example.todolist.data.Todo
import com.example.todolist.data.TodoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Todo API Repository - 使用 API 进行数据操作，同时同步到本地数据库
 */
class TodoApiRepository(private val todoDao: TodoDao) {

    private val apiService = RetrofitClient.todoApiService

    // LiveData 用于 UI 观察 - 从本地数据库获取
    val allTodos: LiveData<List<Todo>> = todoDao.getAllTodos()
    val activeTodos: LiveData<List<Todo>> = todoDao.getActiveTodos()
    val completedTodos: LiveData<List<Todo>> = todoDao.getCompletedTodos()
    val activeTodoCount: LiveData<Int> = todoDao.getActiveTodoCount()
    val completedTodoCount: LiveData<Int> = todoDao.getCompletedTodoCount()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * 从服务器刷新所有数据并同步到本地数据库
     */
    suspend fun refreshAllData() = withContext(Dispatchers.IO) {
        _isLoading.postValue(true)
        try {
            // 从服务器获取所有数据
            val response = apiService.getAllTodos()
            if (response.isSuccessful && response.body()?.success == true) {
                val serverTodos = response.body()?.data?.map { it.toTodo() } ?: emptyList()

                // 同步到本地数据库
                syncTodosToDatabase(serverTodos)
            }
            _error.postValue(null)
        } catch (e: Exception) {
            _error.postValue(e.message ?: "Unknown error")
        } finally {
            _isLoading.postValue(false)
        }
    }

    /**
     * 同步服务器数据到本地数据库
     */
    private suspend fun syncTodosToDatabase(serverTodos: List<Todo>) {
        // 获取本地所有 Todo
        val localTodos = todoDao.getAllTodosSync()

        // 创建本地 Todo ID 映射
        val localTodoMap = localTodos.associateBy { it.id }
        val serverTodoMap = serverTodos.associateBy { it.id }

        // 找出需要新增的 Todo (服务器有，本地没有)
        val toInsert = serverTodos.filter { !localTodoMap.containsKey(it.id) }

        // 找出需要更新的 Todo (两边都有，但内容不同)
        val toUpdate = serverTodos.filter { localTodoMap.containsKey(it.id) }

        // 找出需要删除的 Todo (本地有，服务器没有)
        val toDelete = localTodos.filter { !serverTodoMap.containsKey(it.id) }

        // 执行数据库操作
        toInsert.forEach { todoDao.insertTodo(it) }
        toUpdate.forEach { todoDao.updateTodo(it) }
        toDelete.forEach { todoDao.deleteTodo(it) }
    }

    /**
     * 创建 Todo - 先调用 API，成功后保存到本地数据库
     */
    suspend fun insertTodo(todo: Todo): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val request = CreateTodoRequest(
                title = todo.title,
                description = todo.description,
                priority = mapPriorityToString(todo.priority),
                deadline = todo.reminderTime?.let { formatTimestamp(it) }
            )
            val response = apiService.createTodo(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val serverTodo = response.body()?.data
                if (serverTodo != null) {
                    // 保存到本地数据库
                    val localTodo = serverTodo.toTodo()
                    val id = todoDao.insertTodo(localTodo)
                    Result.success(id)
                } else {
                    Result.failure(Exception("Server returned null data"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create todo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新 Todo - 先调用 API，成功后更新本地数据库
     */
    suspend fun updateTodo(todo: Todo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateTodoRequest(
                title = todo.title,
                description = todo.description,
                priority = mapPriorityToString(todo.priority),
                deadline = todo.reminderTime?.let { formatTimestamp(it) }
            )
            val response = apiService.updateTodo(todo.id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                // 更新本地数据库
                todoDao.updateTodo(todo)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update todo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除 Todo - 先调用 API，成功后从本地数据库删除
     */
    suspend fun deleteTodo(todo: Todo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteTodo(todo.id)
            if (response.isSuccessful && response.body()?.success == true) {
                // 从本地数据库删除
                todoDao.deleteTodo(todo)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete todo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 切换 Todo 状态 - 先调用 API，成功后更新本地数据库
     */
    suspend fun toggleTodoStatus(id: Long, completed: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 根据目标状态调用不同的 API
            val response = if (completed) {
                apiService.completeTask(id)
            } else {
                apiService.pendingTask(id)
            }

            if (response.isSuccessful && response.body()?.success == true) {
                // 更新本地数据库
                val completedAt = if (completed) System.currentTimeMillis() else null
                todoDao.updateTodoStatus(id, completed, completedAt)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to toggle status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 根据 ID 获取 Todo - 先从本地数据库获取，如果不存在则从服务器获取
     */
    suspend fun getTodoById(id: Long): Result<Todo?> = withContext(Dispatchers.IO) {
        try {
            // 先从本地数据库获取
            val localTodo = todoDao.getTodoById(id)
            if (localTodo != null) {
                Result.success(localTodo)
            } else {
                // 从服务器获取
                val response = apiService.getTodoById(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    val serverTodo = response.body()?.data?.toTodo()
                    if (serverTodo != null) {
                        // 保存到本地数据库
                        todoDao.insertTodo(serverTodo)
                    }
                    Result.success(serverTodo)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to get todo"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 根据 category 获取 Todo - 从本地数据库获取
     */
    suspend fun getTodosByCategory(category: String): Result<List<Todo>> = withContext(Dispatchers.IO) {
        try {
            val todos = todoDao.getTodosByCategory(category)
            Result.success(todos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 根据 priority 获取 Todo - 从本地数据库获取
     */
    suspend fun getTodosByPriority(priority: Int): Result<List<Todo>> = withContext(Dispatchers.IO) {
        try {
            val todos = todoDao.getTodosByPriority(priority)
            Result.success(todos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取有提醒的 Todo - 从本地数据库获取
     */
    suspend fun getTodosWithReminders(currentTime: Long, endTime: Long): List<Todo> {
        return todoDao.getTodosWithReminders(currentTime, endTime)
    }
}

/**
 * TodoResponse 转换为 Todo
 */
fun TodoResponse.toTodo(): Todo {
    return Todo(
        id = this.id ?: 0,
        title = this.title,
        description = this.description ?: "",
        isCompleted = this.status == "COMPLETED",
        priority = mapPriorityToInt(this.priority),
        createdAt = parseTimestamp(this.createdAt) ?: System.currentTimeMillis(),
        completedAt = parseTimestamp(this.completedAt),
        reminderTime = parseTimestamp(this.deadline),
        category = "默认" // API 没有返回 category，使用默认值
    )
}

/**
 * 将优先级字符串转换为整数
 */
private fun mapPriorityToInt(priority: String?): Int {
    return when (priority?.uppercase()) {
        "HIGH" -> 3
        "MEDIUM" -> 2
        "LOW" -> 1
        else -> 2
    }
}

/**
 * 将优先级整数转换为字符串
 */
private fun mapPriorityToString(priority: Int): String {
    return when (priority) {
        3 -> "HIGH"
        2 -> "MEDIUM"
        1 -> "LOW"
        else -> "MEDIUM"
    }
}

/**
 * 解析时间戳字符串为 Long
 */
private fun parseTimestamp(timestamp: String?): Long? {
    if (timestamp == null) return null
    return try {
        // 尝试解析 ISO 8601 格式
        java.time.Instant.parse(timestamp).toEpochMilli()
    } catch (e: Exception) {
        null
    }
}

/**
 * 格式化时间戳为 LocalDateTime 格式字符串
 * 服务器期望格式: yyyy-MM-dd'T'HH:mm:ss (不带毫秒)
 */
private fun formatTimestamp(timestamp: Long): String {
    val instant = java.time.Instant.ofEpochMilli(timestamp)
    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    return formatter.format(java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()))
}
