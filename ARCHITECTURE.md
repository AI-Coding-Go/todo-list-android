# TodoList 架构说明

## 整体架构

项目采用 **MVVM + Repository + API + 本地数据库** 的混合架构。

```
┌─────────────────────────────────────────────────────────────┐
│                        Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ MainActivity │  │ ChartActivity│  │  Adapter     │     │
│  └──────┬───────┘  └──────────────┘  └──────────────┘     │
└─────────┼────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                   TodoViewModel                     │   │
│  │  - LiveData: allTodos, activeTodos, completedTodos │   │
│  │  - LiveData: activeTodoCount, completedTodoCount   │   │
│  │  - LiveData: isLoading, error                      │   │
│  │  - Methods: insertTodo, updateTodo, deleteTodo...   │   │
│  └──────────────────────┬───────────────────────────────┘   │
└─────────────────────────┼───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      Repository Layer                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                 TodoApiRepository                     │   │
│  │  ┌──────────────────────────────────────────────┐  │   │
│  │  │           API Service (Retrofit)             │  │   │
│  │  │  - 与服务器通信                                │  │   │
│  │  │  - 处理网络请求                                │  │   │
│  │  └──────────────────┬───────────────────────────┘  │   │
│  │                     │                               │   │
│  │  ┌──────────────────▼───────────────────────────┐  │   │
│  │  │           TodoDao (Room)                     │  │   │
│  │  │  - 本地数据库缓存                              │  │   │
│  │  │  - 离线数据访问                                │  │   │
│  │  └───────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   TodoDao    │  │  AppDatabase │  │    Todo      │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## 数据流说明

### 1. 创建 Todo (Create)

```
用户点击添加按钮
    ↓
MainActivity.showAddEditDialog()
    ↓
TodoViewModel.insertTodo(todo, onSuccess, onError)
    ↓
TodoApiRepository.insertTodo(todo)
    ├─→ API Service.createTodo(request)
    │   └─→ POST http://121.4.203.60:8081/api/todos
    │       └─→ 服务器返回创建的 Todo (包含 ID)
    └─→ TodoDao.insertTodo(todo)
        └─→ 保存到本地数据库
    ↓
LiveData 自动更新 UI
```

### 2. 更新 Todo (Update)

```
用户编辑 Todo
    ↓
MainActivity.showAddEditDialog(todo)
    ↓
TodoViewModel.updateTodo(todo, onSuccess, onError)
    ↓
TodoApiRepository.updateTodo(todo)
    ├─→ API Service.updateTodo(id, request)
    │   └─→ PUT http://121.4.203.60:8081/api/todos/{id}
    └─→ TodoDao.updateTodo(todo)
        └─→ 更新本地数据库
    ↓
LiveData 自动更新 UI
```

### 3. 删除 Todo (Delete)

```
用户删除 Todo
    ↓
MainActivity.showDeleteConfirmDialog(todo)
    ↓
TodoViewModel.deleteTodo(todo, onSuccess, onError)
    ↓
TodoApiRepository.deleteTodo(todo)
    ├─→ API Service.deleteTodo(id)
    │   └─→ DELETE http://121.4.203.60:8081/api/todos/{id}
    └─→ TodoDao.deleteTodo(todo)
        └─→ 从本地数据库删除
    ↓
LiveData 自动更新 UI
```

### 4. 查询 Todo (Read)

```
用户打开应用
    ↓
MainActivity.setupObservers()
    ↓
TodoViewModel.allTodos.observe(this) { todos -> ... }
    ↓
TodoApiRepository.allTodos (LiveData)
    ↓
TodoDao.getAllTodos() (直接从本地数据库读取)
    ↓
LiveData 自动更新 UI
```

### 5. 刷新数据 (Sync)

```
用户下拉刷新 / 应用启动
    ↓
TodoViewModel.refreshData()
    ↓
TodoApiRepository.refreshAllData()
    ├─→ API Service.getAllTodos()
    │   └─→ GET http://121.4.203.60:8081/api/todos
    │       └─→ 获取服务器所有数据
    └─→ syncTodosToDatabase(serverTodos)
        ├─→ 对比本地和服务器数据
        ├─→ 新增: 服务器有，本地没有
        ├─→ 更新: 两边都有
        └─→ 删除: 本地有，服务器没有
    ↓
LiveData 自动更新 UI
```

## 核心类说明

### TodoViewModel
- **职责**: 协调 UI 和 Repository，管理 LiveData
- **LiveData**:
  - `allTodos`: 所有 Todo
  - `activeTodos`: 未完成的 Todo
  - `completedTodos`: 已完成的 Todo
  - `activeTodoCount`: 未完成数量
  - `completedTodoCount`: 已完成数量
  - `isLoading`: 加载状态
  - `error`: 错误信息

### TodoApiRepository
- **职责**: 协调 API 和本地数据库，实现数据同步
- **API 操作**: 所有写操作先调用 API，成功后同步到本地
- **数据库操作**: 所有读操作直接从本地数据库读取

### TodoDao
- **职责**: Room 数据库访问接口
- **新增方法**:
  - `getAllTodosSync()`: 同步获取所有 Todo
  - `getTodosByCategory()`: 按分类获取
  - `getTodosByPriority()`: 按优先级获取

### API Service
- **职责**: Retrofit API 接口定义
- **Base URL**: http://121.4.203.60:8081

## 离线支持

应用支持离线使用：

1. **离线读取**: 所有查询操作直接从本地数据库读取，无需网络
2. **离线写入**: 当前版本需要网络才能写入（可扩展为离线队列）
3. **自动同步**: 每次启动时自动从服务器同步数据

## 错误处理

所有 API 操作都有错误处理：

```kotlin
todoViewModel.insertTodo(todo,
    onSuccess = { id ->
        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show()
    },
    onError = { error ->
        Toast.makeText(this, "添加失败: $error", Toast.LENGTH_SHORT).show()
    }
)
```

## 依赖关系

```
MainActivity
    └─→ TodoViewModel
        └─→ TodoApiRepository
            ├─→ API Service (Retrofit)
            └─→ TodoDao (Room)
                └─→ AppDatabase
```
