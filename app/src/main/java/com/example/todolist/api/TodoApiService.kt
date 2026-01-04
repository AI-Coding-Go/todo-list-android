package com.example.todolist.api

import retrofit2.Response
import retrofit2.http.*

/**
 * Todo API 接口定义
 * API 文档: http://121.4.203.60:8081/doc.html#/public/任务管理/
 */
interface TodoApiService {

    /**
     * 获取所有任务列表
     * API: GET /api/tasks
     */
    @GET("/api/tasks")
    suspend fun getAllTodos(): Response<ApiResponse<List<TodoResponse>>>

    /**
     * 根据 ID 获取任务
     * API: GET /api/tasks/{id}
     */
    @GET("/api/tasks/{id}")
    suspend fun getTodoById(@Path("id") id: Long): Response<ApiResponse<TodoResponse>>

    /**
     * 创建任务
     * API: POST /api/tasks
     */
    @POST("/api/tasks")
    suspend fun createTodo(@Body request: CreateTodoRequest): Response<ApiResponse<TodoResponse>>

    /**
     * 更新任务
     * API: PUT /api/tasks/{id}
     */
    @PUT("/api/tasks/{id}")
    suspend fun updateTodo(
        @Path("id") id: Long,
        @Body request: UpdateTodoRequest
    ): Response<ApiResponse<TodoResponse>>

    /**
     * 删除任务
     * API: DELETE /api/tasks/{id}
     */
    @DELETE("/api/tasks/{id}")
    suspend fun deleteTodo(@Path("id") id: Long): Response<ApiResponse<Unit>>

    /**
     * 获取待完成任务
     * API: GET /api/tasks/pending
     */
    @GET("/api/tasks/pending")
    suspend fun getActiveTodos(): Response<ApiResponse<List<TodoResponse>>>

    /**
     * 获取已完成任务
     * API: GET /api/tasks/completed
     */
    @GET("/api/tasks/completed")
    suspend fun getCompletedTodos(): Response<ApiResponse<List<TodoResponse>>>

    /**
     * 标记任务为已完成
     * API: PATCH /api/tasks/{id}/complete
     */
    @PATCH("/api/tasks/{id}/complete")
    suspend fun completeTask(@Path("id") id: Long): Response<ApiResponse<TodoResponse>>

    /**
     * 标记任务为待完成
     * API: PATCH /api/tasks/{id}/pending
     */
    @PATCH("/api/tasks/{id}/pending")
    suspend fun pendingTask(@Path("id") id: Long): Response<ApiResponse<TodoResponse>>
}
