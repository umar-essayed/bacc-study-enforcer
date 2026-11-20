package com.baccalaureate.studyenforcer.data.remote

import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class SupabaseApi(
    private val client: OkHttpClient,
    private val gson: Gson,
    private val config: SupabaseConfig
) {

    suspend fun fetchRemoteQuestions(): Result<List<QuestionEntity>> = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Supabase credentials not configured in Settings.")
            )
        }

        val baseUrl = config.url.trimEnd('/')
        val endpoint = "$baseUrl/rest/v1/questions?select=*"

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("apikey", config.anonKey)
            .addHeader("Authorization", "Bearer ${config.anonKey}")
            .addHeader("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Supabase server returned HTTP error: ${response.code}")
                    )
                }
                val body = response.body?.string() ?: "[]"
                val type = object : TypeToken<List<QuestionEntity>>() {}.type
                val questions: List<QuestionEntity> = gson.fromJson(body, type)
                Result.success(questions)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Supabase credentials are empty.")
            )
        }

        val baseUrl = config.url.trimEnd('/')
        val endpoint = "$baseUrl/rest/v1/questions?select=id&limit=1"

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("apikey", config.anonKey)
            .addHeader("Authorization", "Bearer ${config.anonKey}")
            .addHeader("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(IOException("Server responded with code ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
