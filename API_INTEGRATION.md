# TodoList API 集成说明

## 概述

项目采用 **API + 本地数据库** 的混合架构：
- **API**: 用于与服务器同步数据
- **本地数据库 (Room)**: 用于缓存和离线使用

所有 Todo 相关的 CRUD 操作都会：
1. 先调用 API 与服务器交互
2. 成功后将数据同步到本地数据库
3. UI 从本地数据库读取数据（保证离线可用）

## API 基础信息

- **Base URL**: `http://121.4.203.60:8081`
- **API 文档**: http://121.4.203.60:8081/doc.html#/home
- **认证方式**: 无需认证（当前版本）

## 架构说明

```
┌─────────────┐
│   UI Layer  │
│ (Activity)  │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  TodoViewModel  │
└──────┬──────────┘
       │
       ▼
┌─────────────────────────────┐
│   TodoApiRepository         │
│  ┌─────────────────────┐   │
│  │  API Service        │   │
│  │  (Retrofit)         │   │
│  └──────────┬──────────┘   │
│             │               │
│  ┌──────────▼──────────┐   │
│  │  TodoDao (Room)     │   │
│  │  (本地数据库)        │   │
│  └─────────────────────┘   │
└─────────────────────────────┘
```

## 数据流程

### 创建 Todo
```
UI → ViewModel → API Repository
  ├─→ API (POST /api/todos)
  └─→ TodoDao (insertTodo)
```

### 更新 Todo
```
UI → ViewModel → API Repository
  ├─→ API (PUT /api/todos/{id})
  └─→ TodoDao (updateTodo)
```

### 删除 Todo
```
UI → ViewModel → API Repository
  ├─→ API (DELETE /api/todos/{id})
  └─→ TodoDao (deleteTodo)
```

### 查询 Todo
```
UI → ViewModel → TodoDao (直接从本地数据库读取)
```

### 刷新数据
```
UI → ViewModel → API Repository
  ├─→ API (GET /api/todos)
  └─→ TodoDao (同步数据)
```

## 项目结构变更

### 新增文件

```
app/src/main/java/com/example/todolist/
├── api/
│   ├── TodoApiModels.kt      # API 数据模型定义
│   ├── TodoApiService.kt     # Retrofit API 接口定义
│   └── RetrofitClient.kt    # Retrofit 客户端单例
└── repository/
    └── TodoApiRepository.kt  # API + 本地数据库 Repository
```

### 修改文件

- `app/build.gradle` - 添加网络请求依赖
- `app/src/main/AndroidManifest.xml` - 添加网络权限
- `app/src/main/java/com/example/todolist/data/TodoDao.kt` - 添加同步方法
- `app/src/main/java/com/example/todolist/viewmodel/TodoViewModel.kt` - 使用 API Repository
- `app/src/main/java/com/example/todolist/MainActivity.kt` - 更新回调处理

## API 接口列表

### 1. 获取所有任务
```
GET /api/tasks
Response: ApiResponse<List<TodoResponse>>
```

### 2. 根据 ID 获取任务
```
GET /api/tasks/{id}
Response: ApiResponse<TodoResponse>
```

### 3. 创建任务
```
POST /api/tasks
Body: CreateTodoRequest
Response: ApiResponse<TodoResponse>
```

### 4. 更新任务
```
PUT /api/tasks/{id}
Body: UpdateTodoRequest
Response: ApiResponse<TodoResponse>
```

### 5. 删除任务
```
DELETE /api/tasks/{id}
Response: ApiResponse<Unit>
```

### 6. 获取待完成任务
```
GET /api/tasks/pending
Response: ApiResponse<List<TodoResponse>>
```

### 7. 获取已完成任务
```
GET /api/tasks/completed
Response: ApiResponse<List<TodoResponse>>
```

### 8. 标记任务为已完成
```
PUT /api/tasks/{id}/complete
Response: ApiResponse<TodoResponse>
```

### 9. 标记任务为待完成
```
PUT /api/tasks/{id}/pending
Response: ApiResponse<TodoResponse>
```

## 数据模型

### TodoResponse
```kotlin
data class TodoResponse(
    val id: Long?,
    val title: String,
    val description: String?,
    val status: String?,              // "PENDING", "COMPLETED"
    val statusDescription: String?,
    val priority: String?,            // "HIGH", "MEDIUM", "LOW"
    val priorityDescription: String?,
    val priorityColor: String?,
    val deadline: String?,            // ISO 8601 格式
    val createdAt: String?,           // ISO 8601 格式
    val updatedAt: String?,
    val completedAt: String?,         // ISO 8601 格式
    val overdue: Boolean?,
    val remainingMinutes: Int?,
    val deadlineFormatted: String?,
    val completedAtFormatted: String?
)
```

### CreateTodoRequest
```kotlin
data class CreateTodoRequest(
    val title: String,
    val description: String?,
    val priority: String?,            // "HIGH", "MEDIUM", "LOW"
    val deadline: String?             // ISO 8601 格式
)
```

### UpdateTodoRequest
```kotlin
data class UpdateTodoRequest(
    val title: String?,
    val description: String?,
    val priority: String?,            // "HIGH", "MEDIUM", "LOW"
    val deadline: String?             // ISO 8601 格式
)
```

### ApiResponse
```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: ErrorResponse?,
    val timestamp: String?
)
```

### ErrorResponse
```kotlin
data class ErrorResponse(
    val code: String?,
    val message: String?,
    val details: String?
)
```

## 使用示例

### 创建 Todo
```kotlin
val newTodo = Todo(
    title = "完成 API 集成",
    description = "将本地数据库迁移到 API",
    priority = 2,
    category = "工作"
)

todoViewModel.insertTodo(newTodo,
    onSuccess = { id ->
        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show()
    },
    onError = { error ->
        Toast.makeText(this, "添加失败: $error", Toast.LENGTH_SHORT).show()
    }
)
```

### 更新 Todo
```kotlin
todoViewModel.updateTodo(updatedTodo,
    onSuccess = {
        Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show()
    },
    onError = { error ->
        Toast.makeText(this, "更新失败: $error", Toast.LENGTH_SHORT).show()
    }
)
```

### 删除 Todo
```kotlin
todoViewModel.deleteTodo(todo,
    onSuccess = {
        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show()
    },
    onError = { error ->
        Toast.makeText(this, "删除失败: $error", Toast.LENGTH_SHORT).show()
    }
)
```

### 切换状态
```kotlin
todoViewModel.toggleTodoStatus(todo,
    onSuccess = {
        // 状态切换成功
    },
    onError = { error ->
        Toast.makeText(this, "状态切换失败: $error", Toast.LENGTH_SHORT).show()
    }
)
```

## ViewModel 变更

### 新增 LiveData
- `isLoading: LiveData<Boolean>` - 加载状态
- `error: LiveData<String?>` - 错误信息

### 方法签名变更
所有 CRUD 方法现在都支持成功和失败回调：

```kotlin
fun insertTodo(todo: Todo, onSuccess: ((Long) -> Unit)? = null, onError: ((String) -> Unit)? = null)
fun updateTodo(todo: Todo, onSuccess: (() -> Unit)? = null, onError: ((String) -> Unit)? = null)
fun deleteTodo(todo: Todo, onSuccess: (() -> Unit)? = null, onError: ((String) -> Unit)? = null)
fun toggleTodoStatus(todo: Todo, onSuccess: (() -> Unit)? = null, onError: ((String) -> Unit)? = null)
```

## 依赖项

```gradle
// 网络请求
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

// Gson
implementation 'com.google.code.gson:gson:2.10.1'
```

## 权限

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 注意事项

1. **网络连接**: 应用需要网络连接才能与服务器同步数据
2. **离线可用**: 即使没有网络，应用仍可从本地数据库读取数据
3. **错误处理**: 所有 API 调用都有错误处理，建议在 UI 中显示错误信息
4. **加载状态**: 可以通过 `isLoading` LiveData 显示加载指示器
5. **数据同步**: 每次数据变更后会自动同步到本地数据库
6. **本地数据库**: Room 数据库用于缓存，保证离线可用

## 后续优化建议

1. 添加离线队列 - 网络不可用时将操作排队，恢复后自动同步
2. 实现增量同步 - 只同步变更的数据，减少网络流量
3. 添加请求重试机制 - 网络失败时自动重试
4. 实现分页加载 - 大数据量时使用分页
5. 添加用户认证 - 保护 API 接口
6. 优化网络请求性能 - 使用缓存、压缩等
7. 添加数据冲突解决策略 - 处理多端同时编辑的情况
