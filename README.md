# 待办清单应用

一款功能完整的Android待办事项管理应用，采用传统XML布局开发。

## 功能特性

### 核心功能
- ✅ **新增待办事项** - 支持标题、描述、分类、优先级设置
- ✏️ **编辑待办事项** - 修改已有任务的所有属性
- 🗑️ **删除待办事项** - 删除不需要的任务
- ☑️ **标记完成状态** - 一键切换任务完成/未完成状态
- 📋 **列表查看** - 支持全部、待完成、已完成三种视图切换

### 高级功能
- 📊 **统计图表** - 可视化展示任务完成情况和优先级分布
- ⏰ **待办提醒** - 为重要任务设置提醒通知
- 🏷️ **分类管理** - 支持自定义任务分类
- ⭐ **优先级设置** - 高、中、低三级优先级
- 📈 **实时统计** - 显示待完成和已完成任务数量

## 技术架构

### 架构模式
- **MVVM架构** - 使用ViewModel管理UI状态
- **Repository模式** - 数据访问层抽象
- **Room数据库** - 本地数据持久化

### 主要组件
- **MainActivity** - 主界面，展示待办列表
- **ChartActivity** - 统计图表界面
- **TodoViewModel** - 业务逻辑处理
- **TodoRepository** - 数据仓库
- **TodoDao** - 数据访问对象
- **TodoAdapter** - RecyclerView适配器
- **ReminderReceiver** - 提醒广播接收器

### 技术栈
- **Kotlin** - 主要开发语言
- **XML布局** - 传统Android布局方式
- **Material Design** - 现代化UI设计
- **Room** - 本地数据库
- **RecyclerView** - 列表展示
- **LiveData** - 响应式数据观察

## 使用说明

### 基本操作
1. **添加任务** - 点击右下角"+"按钮
2. **编辑任务** - 点击任务卡片或菜单中的"编辑"
3. **删除任务** - 长按任务选择"删除"
4. **完成任务** - 勾选任务前的复选框
5. **查看统计** - 点击顶部菜单的"统计图表"

### 任务属性
- **标题** - 必填，任务的主要描述
- **描述** - 可选，任务的详细信息
- **分类** - 可选，默认为"默认"
- **优先级** - 低、中、高三个级别
- **提醒** - 可选，设置任务提醒时间

## 权限说明
- `SCHEDULE_EXACT_ALARM` - 精确闹钟权限，用于任务提醒
- `POST_NOTIFICATIONS` - 通知权限，用于显示提醒通知

## 构建说明
项目使用Gradle构建，支持Android SDK 30+。

```bash
# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease
```

## 项目结构
```
app/src/main/java/com/example/todolist/
├── data/           # 数据层
│   ├── Todo.kt     # 数据实体
│   ├── TodoDao.kt  # 数据访问对象
│   └── AppDatabase.kt # 数据库
├── repository/     # 仓库层
│   └── TodoRepository.kt
├── viewmodel/      # 视图模型层
│   └── TodoViewModel.kt
├── adapter/        # 适配器
│   └── TodoAdapter.kt
├── MainActivity.kt # 主活动
├── ChartActivity.kt # 图表活动
└── ReminderReceiver.kt # 提醒接收器
```

## 开发特点
- 采用传统XML布局，符合用户要求
- 完整的MVVM架构实现
- 响应式数据绑定
- Material Design设计规范
- 本地数据持久化
- 系统级提醒功能