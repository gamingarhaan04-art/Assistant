package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class VeoVideoResponse(
    val operationName: String,
    val prompt: String,
    val aspectRatio: String, // "16:9" or "9:16"
    val resolution: String,
    val isDone: Boolean,
    val videoUri: String? = null,
    val downloadUrl: String? = null,
    val message: String? = null
)

class GeminiClient(
    private val customKeyProvider: () -> String? = { null },
    private val modelProvider: () -> String = { "gemini-3.5-flash" }
) {
    private val tag = "GeminiClient"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        val custom = customKeyProvider()?.trim()
        if (!custom.isNullOrEmpty()) {
            return custom
        }
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return buildConfigKey.trim()
    }

    suspend fun generateResponse(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(), // Pair(role, text)
        imageBase64: String? = null,
        imageMimeType: String = "image/jpeg",
        systemPromptOverride: String? = null,
        modelOverride: String? = null,
        onChunk: ((String) -> Unit)? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please supply a valid key in the AI Studio Secrets panel or API Settings.")
            )
        }

        val model = modelOverride ?: modelProvider()
        val defaultSystemInstruction = """
            You are JARVIS AI (Just A Rather Very Intelligent System), a calm, sophisticated, and exceptionally capable futuristic AI assistant.
            You address the user with professional respect.
            You have native support for both English and Hindi (and natural Hinglish), automatically responding in the user's preferred language.
            Keep answers concise and clear for quick questions, but provide rich, structured, analytical details when requested.
            Never invent facts or claim to perform physical actions you cannot execute.
        """.trimIndent()

        val systemInstructionText = systemPromptOverride ?: defaultSystemInstruction

        // Construct Request JSON
        val requestJson = JSONObject()

        // System Instruction
        val sysInstructionObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", systemInstructionText))
        sysInstructionObj.put("parts", sysParts)
        requestJson.put("systemInstruction", sysInstructionObj)

        // Contents
        val contentsArray = JSONArray()

        // Add history turns (keep last 10 turns)
        val recentHistory = if (history.size > 10) history.takeLast(10) else history
        for ((role, text) in recentHistory) {
            val contentObj = JSONObject()
            contentObj.put("role", if (role == "jarvis") "model" else "user")
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", text))
            contentObj.put("parts", partsArr)
            contentsArray.put(contentObj)
        }

        // Current turn
        val currentContentObj = JSONObject()
        currentContentObj.put("role", "user")
        val currentParts = JSONArray()

        // Text prompt
        currentParts.put(JSONObject().put("text", prompt))

        // Image attachment if provided
        if (!imageBase64.isNullOrEmpty()) {
            val inlineDataObj = JSONObject()
            inlineDataObj.put("mime_type", imageMimeType)
            inlineDataObj.put("data", imageBase64)
            currentParts.put(JSONObject().put("inline_data", inlineDataObj))
        }

        currentContentObj.put("parts", currentParts)
        contentsArray.put(currentContentObj)
        requestJson.put("contents", contentsArray)

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        genConfig.put("maxOutputTokens", 2048)
        requestJson.put("generationConfig", genConfig)

        // Check if streaming is requested
        if (onChunk != null) {
            return@withContext streamContent(model, apiKey, requestJson, onChunk)
        }

        // Standard REST call
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                // If model is live or preview and fails, try fallback
                if (model == "gemini-3.8-live" || model.contains("live")) {
                    Log.w(tag, "gemini-3.8-live returned $response.code, falling back to gemini-3.5-flash")
                    return@withContext generateResponse(
                        prompt = prompt,
                        history = history,
                        imageBase64 = imageBase64,
                        imageMimeType = imageMimeType,
                        systemPromptOverride = systemPromptOverride,
                        modelOverride = "gemini-3.5-flash",
                        onChunk = onChunk
                    )
                }
                val errorMsg = parseErrorMessage(response.code, responseBody)
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("JARVIS received an empty response from neural core."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val sb = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text")) {
                        sb.append(p.getString("text"))
                    }
                }
            }

            val answer = sb.toString().trim()
            if (answer.isEmpty()) {
                val finishReason = firstCandidate.optString("finishReason", "UNKNOWN")
                return@withContext Result.failure(Exception("Neural stream concluded with reason: $finishReason"))
            }

            Result.success(answer)
        } catch (e: Exception) {
            Log.e(tag, "Gemini generateContent failed", e)
            val friendly = when {
                e is java.net.UnknownHostException -> "JARVIS is offline. Please check your network connection."
                e is java.net.SocketTimeoutException -> "The neural network request timed out. Please try again."
                else -> e.localizedMessage ?: "Unknown diagnostic failure"
            }
            Result.failure(Exception(friendly))
        }
    }

    private fun streamContent(
        model: String,
        apiKey: String,
        requestJson: JSONObject,
        onChunk: (String) -> Unit
    ): Result<String> {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val fullTextBuilder = StringBuilder()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val errorMsg = parseErrorMessage(response.code, errBody)
                return Result.failure(Exception(errorMsg))
            }

            val inputStream = response.body?.byteStream()
                ?: return Result.failure(Exception("Null response stream"))

            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line?.trim() ?: continue
                    if (!trimmed.startsWith("data:")) continue
                    val payload = trimmed.removePrefix("data:").trim()
                    if (payload.isEmpty() || payload == "[DONE]") continue

                    try {
                        val chunkObj = JSONObject(payload)
                        val candidates = chunkObj.optJSONArray("candidates") ?: continue
                        if (candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.getJSONObject(i)
                                    if (part.has("text")) {
                                        val chunkText = part.getString("text")
                                        fullTextBuilder.append(chunkText)
                                        onChunk(chunkText)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(tag, "Failed to parse stream chunk: $payload", e)
                    }
                }
            }

            val finalOutput = fullTextBuilder.toString().trim()
            if (finalOutput.isEmpty()) {
                return Result.failure(Exception("JARVIS received an empty response stream."))
            }
            return Result.success(finalOutput)
        } catch (e: Exception) {
            Log.e(tag, "Streaming failed", e)
            val friendly = when {
                e is java.net.UnknownHostException -> "JARVIS is offline. Please check your network connection."
                e is java.net.SocketTimeoutException -> "The connection timed out during streaming."
                else -> e.localizedMessage ?: "Stream communication error"
            }
            return Result.failure(Exception(friendly))
        }
    }

    // --- Live API Real-Time Conversation (gemini-3.8-live) ---
    suspend fun converseLiveApi(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        onChunk: ((String) -> Unit)? = null
    ): Result<String> {
        val liveSystemInstruction = """
            You are JARVIS operating in LIVE API REAL-TIME CONVERSATION MODE (gemini-3.8-live).
            You provide concise, instant, highly natural verbal responses suitable for real-time voice playback.
            Speak directly and with poise. Support Hindi, English, and natural conversational dialogue seamlessly.
        """.trimIndent()

        return generateResponse(
            prompt = prompt,
            history = history,
            systemPromptOverride = liveSystemInstruction,
            modelOverride = "gemini-3.8-live",
            onChunk = onChunk
        )
    }

    // --- Veo 3 Video Generation (veo-3.1-fast-generate-preview) ---
    suspend fun generateVeoVideo(
        prompt: String,
        aspectRatio: String = "16:9", // "16:9" or "9:16"
        resolution: String = "1080p",
        onProgress: ((String) -> Unit)? = null
    ): Result<VeoVideoResponse> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is required for Veo 3 video generation.")
            )
        }

        onProgress?.invoke("Initializing Veo 3 neural pipeline (veo-3.1-fast-generate-preview)...")

        val model = "veo-3.1-fast-generate-preview"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateVideos?key=$apiKey"

        val requestJson = JSONObject()
        requestJson.put("prompt", prompt)

        val configJson = JSONObject()
        configJson.put("numberOfVideos", 1)
        configJson.put("resolution", resolution)
        configJson.put("aspectRatio", aspectRatio)
        requestJson.put("config", configJson)

        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(response.code, responseBody)
                return@withContext Result.failure(Exception("Veo 3 generation failed: $errorMsg"))
            }

            val json = JSONObject(responseBody)
            val operationName = json.optString("name")
            if (operationName.isEmpty()) {
                return@withContext Result.failure(Exception("Veo 3 did not return an operation tracking reference."))
            }

            onProgress?.invoke("Veo 3 video generation initiated. Operation: $operationName")

            // Poll operation status up to 3 times or return current operation info
            var isDone = false
            var videoUri: String? = null
            var downloadUrl: String? = null

            for (attempt in 1..4) {
                delay(3000)
                onProgress?.invoke("Synthesizing frames ($aspectRatio) [Step $attempt]...")
                val pollUrl = "https://generativelanguage.googleapis.com/v1beta/$operationName?key=$apiKey"
                val pollReq = Request.Builder().url(pollUrl).get().build()
                try {
                    val pollResp = okHttpClient.newCall(pollReq).execute()
                    val pollBody = pollResp.body?.string() ?: ""
                    if (pollResp.isSuccessful) {
                        val pollJson = JSONObject(pollBody)
                        if (pollJson.optBoolean("done", false)) {
                            isDone = true
                            val respObj = pollJson.optJSONObject("response")
                            val videosArr = respObj?.optJSONArray("videos")
                            val firstVideo = videosArr?.optJSONObject(0)
                            val videoObj = firstVideo?.optJSONObject("video")
                            videoUri = videoObj?.optString("uri")
                            downloadUrl = firstVideo?.optString("downloadUrl")
                            break
                        }
                    }
                } catch (pe: Exception) {
                    Log.w(tag, "Polling step error", pe)
                }
            }

            val result = VeoVideoResponse(
                operationName = operationName,
                prompt = prompt,
                aspectRatio = aspectRatio,
                resolution = resolution,
                isDone = isDone,
                videoUri = videoUri,
                downloadUrl = downloadUrl,
                message = if (isDone) "Video generation successfully completed by Veo 3." else "Veo 3 render pipeline running in background (Operation: $operationName)."
            )

            Result.success(result)
        } catch (e: Exception) {
            Log.e(tag, "Veo video generation error", e)
            Result.failure(Exception(e.localizedMessage ?: "Veo 3 connection failure"))
        }
    }

    private fun parseErrorMessage(code: Int, responseBody: String): String {
        return try {
            val errorObj = JSONObject(responseBody).optJSONObject("error")
            val message = errorObj?.optString("message")
            when (code) {
                400, 401, 403 -> "Invalid or restricted Gemini API credentials ($code). Verify your API key."
                404 -> "Model unavailable or Generative Language API not active ($code)."
                429 -> "Usage quota exceeded. Please wait a few seconds or upgrade your plan."
                500, 503 -> "Google AI service is temporarily unavailable. Please retry shortly."
                else -> message ?: "Server communication failed (HTTP $code)"
            }
        } catch (e: Exception) {
            "JARVIS communication error (HTTP $code)"
        }
    }
}
