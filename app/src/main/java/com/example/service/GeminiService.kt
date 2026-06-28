package com.example.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    // We use the recommended 'gemini-3.5-flash' model
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is not configured.")
            return@withContext "API Key not configured. Please add your GEMINI_API_KEY to the Secrets panel in AI Studio."
        }

        try {
            // Build request JSON safely using JSONObject
            val requestJson = JSONObject()
            
            // Contents
            val contentsArray = org.json.JSONArray()
            val contentObj = JSONObject()
            val partsArray = org.json.JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // System Instruction if provided
            if (systemInstruction != null) {
                val systemInstructionObj = JSONObject()
                val systemPartsArray = org.json.JSONArray()
                val systemPartObj = JSONObject()
                systemPartObj.put("text", systemInstruction)
                systemPartsArray.put(systemPartObj)
                systemInstructionObj.put("parts", systemPartsArray)
                requestJson.put("systemInstruction", systemInstructionObj)
            }

            val body = requestJson.toString().toRequestBody(mediaType)
            val url = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "API call failed: ${response.code} -> $responseBodyStr")
                    return@withContext "Error calling FitForge AI Coach: ${response.message}"
                }

                // Parse response
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "I'm not sure how to answer that.")
                        }
                    }
                }
                "I'm here to support your fitness journey. Can you repeat that?"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            "Error: Unable to connect to FitForge AI Coach. ${e.localizedMessage ?: "Please try again later."}"
        }
    }
}
