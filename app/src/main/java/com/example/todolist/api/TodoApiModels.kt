package com.example.todolist.api

import com.google.gson.annotations.SerializedName

/**
 * Todo 数据模型 (API 响应)
 */
data class TodoResponse(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = "",

    @SerializedName("status")
    val status: String? = "",

    @SerializedName("statusDescription")
    val statusDescription: String? = "",

    @SerializedName("priority")
    val priority: String? = "",

    @SerializedName("priorityDescription")
    val priorityDescription: String? = "",

    @SerializedName("priorityColor")
    val priorityColor: String? = "",

    @SerializedName("deadline")
    val deadline: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("completedAt")
    val completedAt: String? = null,

    @SerializedName("overdue")
    val overdue: Boolean? = false,

    @SerializedName("remainingMinutes")
    val remainingMinutes: Int? = 0,

    @SerializedName("deadlineFormatted")
    val deadlineFormatted: String? = null,

    @SerializedName("completedAtFormatted")
    val completedAtFormatted: String? = null
)

/**
 * 创建 Todo 请求
 */
data class CreateTodoRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = "",

    @SerializedName("priority")
    val priority: String? = "MEDIUM",

    @SerializedName("deadline")
    val deadline: String? = null
)

/**
 * 更新 Todo 请求
 */
data class UpdateTodoRequest(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("priority")
    val priority: String? = null,

    @SerializedName("deadline")
    val deadline: String? = null
)

/**
 * 通用 API 响应
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T?,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: ErrorResponse? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

/**
 * 错误响应
 */
data class ErrorResponse(
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("details")
    val details: String? = null
)

/**
 * 分页响应
 */
data class PageResponse<T>(
    @SerializedName("records")
    val records: List<T>,

    @SerializedName("total")
    val total: Long,

    @SerializedName("size")
    val size: Int,

    @SerializedName("current")
    val current: Int,

    @SerializedName("pages")
    val pages: Int
)
