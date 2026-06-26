package com.example.data.network

import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val username: String,
    val password: String,
    val device: String = "android"
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val user: NetworkUser?
)

@JsonClass(generateAdapter = true)
data class NetworkUser(
    val id: Int?,
    val name: String?,
    val email: String?
)

@JsonClass(generateAdapter = true)
data class NetworkJob(
    val id: Long? = null,
    val client_name: String? = null,
    val clientName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val company: String? = null,
    val deal_value: Double? = null,
    val dealValue: Double? = null,
    val value: Double? = null,
    val amount: Double? = null,
    val status: String? = null,
    val detailed_status: String? = null,
    val detailedStatus: String? = null,
    val complaint: String? = null,
    val diagnostics: String? = null,
    val solution: String? = null,
    val visit_date: String? = null,
    val visitDate: String? = null,
    val remind_date: String? = null,
    val remindDate: String? = null,
    val labor_minutes: Int? = null,
    val laborMinutes: Int? = null,
    val my_share: Int? = null,
    val myShare: Int? = null,
    val alex_share: Int? = null,
    val alexShare: Int? = null,
    val car_make: String? = null,
    val carMake: String? = null,
    val car_model: String? = null,
    val carModel: String? = null,
    val car_license: String? = null,
    val carLicense: String? = null,
    val car_vin: String? = null,
    val carVin: String? = null,
    val car_year: String? = null,
    val carYear: String? = null,
    val car_engine: String? = null,
    val carEngine: String? = null,
    val timestamp: Long? = null
)

@JsonClass(generateAdapter = true)
data class JobResponseWrapper(
    val data: List<NetworkJob>? = null
)

@JsonClass(generateAdapter = true)
data class JobDetailResponseWrapper(
    val data: NetworkJob? = null
)

@JsonClass(generateAdapter = true)
data class StatusUpdateRequest(
    val status: String,
    val detailed_status: String? = null,
    val detailedStatus: String? = null
)

@JsonClass(generateAdapter = true)
data class DashboardStats(
    val total_deals: Int? = null,
    val totalDeals: Int? = null,
    val total_pipeline_value: Double? = null,
    val totalPipelineValue: Double? = null,
    val won_deals_count: Int? = null,
    val wonDealsCount: Int? = null,
    val won_value: Double? = null,
    val wonValue: Double? = null,
    val active_deals_count: Int? = null,
    val activeDealsCount: Int? = null,
    val active_value: Double? = null,
    val activeValue: Double? = null,
    val total_tasks_count: Int? = null,
    val totalTasksCount: Int? = null,
    val pending_tasks_count: Int? = null,
    val pendingTasksCount: Int? = null
)

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<ResponseBody>

    @GET("api/dashboard")
    suspend fun getDashboardStats(): Response<DashboardStats>

    @GET("api/jobs")
    suspend fun getJobs(
        @Query("status") status: String? = null,
        @Query("q") query: String? = null
    ): Response<ResponseBody>

    @POST("api/jobs")
    suspend fun createJob(
        @Body job: NetworkJob
    ): Response<ResponseBody>

    @GET("api/jobs/{id}")
    suspend fun getJobById(
        @Path("id") id: Long
    ): Response<ResponseBody>

    @PATCH("api/jobs/{id}/status")
    suspend fun updateJobStatus(
        @Path("id") id: Long,
        @Body request: StatusUpdateRequest
    ): Response<ResponseBody>

    @PATCH("api/jobs/{id}")
    suspend fun updateJobFields(
        @Path("id") id: Long,
        @Body job: NetworkJob
    ): Response<ResponseBody>
}
